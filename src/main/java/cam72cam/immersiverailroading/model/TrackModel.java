package cam72cam.immersiverailroading.model;

import cam72cam.immersiverailroading.render.MultiVBO;
import cam72cam.immersiverailroading.track.BuilderBase;
import cam72cam.immersiverailroading.track.BuilderBase.VecYawPitch;
import cam72cam.immersiverailroading.util.DataBlock;
import cam72cam.immersiverailroading.util.MathUtil;
import cam72cam.immersiverailroading.util.RailInfo;
import cam72cam.mod.model.obj.OBJModel;
import cam72cam.mod.render.obj.OBJRender;
import cam72cam.mod.render.opengl.VBO;
import cam72cam.mod.resource.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import trackapi.lib.Gauges;
import util.Matrix4;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TrackModel {
    private Map<String, Supplier<String>> randomMap;
    private TrackOrder order;
    private final Map<String, Identifier> mapper;
    private final Map<Identifier, OBJModel> models;
    private final String compare;
    private final double size;
    private final double height;
    public final double spacing;

    public TrackModel(String condition, Identifier resource, double modelGaugeM, double spacing) throws Exception {
        OBJModel model = new OBJModel(resource, 0, Gauges.STANDARD / modelGaugeM);
        this.models = Collections.singletonMap(resource, model);
        this.compare = condition.substring(0, 1);
        this.size = Double.parseDouble(condition.substring(1));
        this.height = calculateRailHeight(model);
        this.spacing = spacing * (Gauges.STANDARD / modelGaugeM);
        this.mapper = null;
        this.randomMap = null;
    }

    public static TrackModel parse(String condition, DataBlock block, double modelGaugeM, double spacing) throws Exception{
        Map<String, Supplier<String>> mapper = new HashMap<>();
        Map<String, Identifier> models = new HashMap<>();

        List<DataBlock> subModels = block.getBlocks("sub_models");
        if(subModels != null){
            subModels.forEach(b -> {
                String ident = b.getValue("ident").asString();
                models.put(ident, b.getValue("path").asIdentifier());
                mapper.put(ident, () -> ident);
            });
        }

        List<DataBlock> randomized = block.getBlocks("randomized");
        if(randomized != null){
            randomized.forEach(b -> {
                String ident = b.getValue("ident").asString();
                Random ran = new Random(ident.hashCode());
                Map<String, Integer> partWeights = b.getBlock("part_weights").getValueMap().entrySet()
                                                    .stream()
                                                    .map(entry -> Pair.of(entry.getKey(), entry.getValue().asInteger()))
                                                    .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
                List<String> strs = new ArrayList<>();
                AtomicInteger integer = new AtomicInteger(0);

                int overallGcd = partWeights.values().stream()
                                       .reduce(0, MathUtil::gcd);

                partWeights.forEach((s, i) -> {
                    i = i / overallGcd;
                    for(int j = 0; j < i; j++){
                        strs.add(s);
                    }
                    integer.addAndGet(i);
                });

                mapper.put(ident, () -> strs.get(ran.nextInt(integer.get())));
            });
        }

        BiFunction<String, DataBlock, List<String>> getTrackList = (s, b) -> b.getValues(s)
                                                                              .stream()
                                                                              .map(DataBlock.Value::asString)
                                                                              .collect(Collectors.toList());
        DataBlock orderBlock = block.getBlock("order");
        List<DataBlock.Value> orderArray = block.getValues("order");
        TrackOrder trackOrder;
        if(orderBlock != null){
            List<String> near = getTrackList.apply("near", orderBlock);
            List<String> mid = getTrackList.apply("mid", orderBlock);
            List<String> far = getTrackList.apply("far", orderBlock);
            trackOrder = new TrackOrder(mid);
            trackOrder.setNear(near);
            trackOrder.setFar(far);
        } else if(orderArray != null) {
            List<String> mid = getTrackList.apply("order", block);
            trackOrder = new TrackOrder(mid);
        } else {
            throw new IllegalArgumentException("Must contains \"order\" field");
        }

        TrackModel model1 = new TrackModel(condition, models, modelGaugeM, spacing);
        model1.order = trackOrder;
        model1.randomMap = mapper;
        return model1;
    }

    public TrackModel(String condition, Map<String, Identifier> map, double modelGaugeM, double spacing) throws Exception {
        this.models = new HashMap<>();
        this.mapper = map;
        this.randomMap = new HashMap<>();

        double maxHeight = 0;
        for (Map.Entry<String, Identifier> entry : map.entrySet()) {
            OBJModel model = new OBJModel(entry.getValue(), 0, Gauges.STANDARD / modelGaugeM);
            this.models.put(entry.getValue(), model);
            maxHeight = Math.max(maxHeight, calculateRailHeight(model));
        }

        this.compare = condition.substring(0, 1);
        this.size = Double.parseDouble(condition.substring(1));
        this.height = maxHeight;
        this.spacing = spacing * (Gauges.STANDARD / modelGaugeM);
    }

    private double calculateRailHeight(OBJModel model) {
        List<String> railGroups = model.groups().stream()
                                       .filter(group -> group.contains("RAIL_LEFT") || group.contains("RAIL_RIGHT"))
                                       .collect(Collectors.toList());
        return model.maxOfGroup(railGroups).y;
    }

    public boolean canRender(double gauge) {
        switch (compare) {
            case ">": return gauge > size;
            case "<": return gauge < size;
            case "=": return gauge == size;
            default: return true;
        }
    }

    public MultiVBO getModel(RailInfo info, List<BuilderBase.VecYawPitch> data) {
        if(info.settings.type.isTable() || this.models.size() == 1){
            return renderSingle(info, data);
        }

        Map<String, OBJRender.Builder> vboMap = new HashMap<>();
        List<String> names = order.getRenderOrder(data.size());
        for (int i = 0; i < names.size(); i++) {
            String modelKey = randomMap.get(names.get(i)).get();
            OBJModel model = models.get(mapper.get(modelKey));

            if (!vboMap.containsKey(modelKey)) {
                vboMap.put(modelKey, model.binder().builder());
            }

            renderPiece(info, data.get(i), model, vboMap.get(modelKey));
        }

        Set<VBO> vbos = vboMap.values().stream()
                              .map(OBJRender.Builder::build)
                              .collect(Collectors.toSet());

        return new MultiVBO(vbos);
    }

    private MultiVBO renderSingle(RailInfo info, List<BuilderBase.VecYawPitch> data) {
        OBJModel model = getFirstModel();
        OBJRender.Builder builder = model.binder().builder();

        for (BuilderBase.VecYawPitch piece : data) {
            renderPiece(info, piece, model, builder);
        }

        return new MultiVBO(builder.build());
    }

    private void renderPiece(RailInfo info, VecYawPitch piece, OBJModel model, OBJRender.Builder builder) {
        Matrix4 matrix = new Matrix4();
        matrix.translate(piece.x, piece.y, piece.z);
        matrix.rotate(Math.toRadians(piece.getYaw()), 0, 1, 0);
        matrix.rotate(Math.toRadians(piece.getPitch()), 1, 0, 0);
        matrix.rotate(Math.toRadians(-90), 0, 1, 0);

        double scale = info.settings.gauge.scale();
        matrix.scale(scale, scale, scale);

        List<String> tables = new ArrayList<>();
        model.groups().stream().filter(s -> s.contains("TABLE")).forEach(tables::add);

        if(piece.getGroups().contains("RENDERTABLE")){
            builder.draw(tables, matrix);
        }

        if (piece.getLength() != -1) {
            matrix = matrix.copy().scale(piece.getLength() / info.settings.gauge.scale(), 1, 1);
        }

        List<String> groups;
        if (!piece.getGroups().isEmpty()) {
            groups = model.groups().stream()
                          .filter(group -> piece.getGroups().stream().anyMatch(group::contains))
                          .collect(Collectors.toList());
            builder.draw(groups, matrix);
        } else {
            groups = new ArrayList<>(model.groups());
            groups.removeAll(tables);
        }
        builder.draw(groups, matrix);
        if(!piece.children.isEmpty()){
            for(VecYawPitch vec : piece.children){
                renderPiece(info, vec, model, builder);
            }
        }
    }

    public OBJModel getFirstModel() {
        return models.values().iterator().next();
    }

    public double getHeight() {
        return height;
    }

    public void free() {
        for (OBJModel model : this.models.values()) {
            model.free();
        }
    }

    public static class TrackOrder{
        // All directions are Near -> Far
        protected final List<String> near = new ArrayList<>();
        protected final List<String> mid = new ArrayList<>();
        protected final List<String> far = new ArrayList<>();

        public TrackOrder(List<String> mid) {
            this.mid.addAll(parseCounts(mid));
        }

        public void setNear(List<String> near) {
            this.near.addAll(parseCounts(near));
        }

        public void setFar(List<String> far) {
            this.far.addAll(parseCounts(far));
        }

        public List<String> getRenderOrder(int length) {
            List<String> value = new ArrayList<>();

            if(length < near.size() + far.size()){
                int n = 0, f = 0;
                while (n + f < length){
                    if(n <= near.size()){
                        n++;
                    }

                    if(n + f < length && f <= far.size()){
                        f++;
                    }
                }

                for(int i = 0; i < n; i++){
                    value.add(near.get(i));
                }
                for(int i = far.size() - f; i < far.size(); i++){
                    value.add(far.get(i));
                }
            } else {
                value.addAll(near);
                value.addAll(far);
                for(int i = 0, j = near.size(); j < length - far.size(); i = (i+1) % mid.size(), j++){
                    value.add(j, mid.get(i));
                }
            }
            return value;
        }

        private static List<String> parseCounts(List<String> orig) {
            List<String> result = new ArrayList<>();
            for(String s : orig){
                String[] str = s.split("\\*");
                result.add(str[0]);
                if(str.length == 2) {
                    int count = Integer.parseInt(str[1]);
                    for (int i = 1; i < count; i++) {
                        result.add(str[0]);
                    }
                }
            }
            return result;
        }
    }
}