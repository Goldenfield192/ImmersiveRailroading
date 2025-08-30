package cam72cam.immersiverailroading.model;

import cam72cam.immersiverailroading.render.MultiVBO;
import cam72cam.immersiverailroading.track.BuilderBase;
import cam72cam.immersiverailroading.track.BuilderBase.VecYawPitch;
import cam72cam.immersiverailroading.util.RailInfo;
import cam72cam.mod.model.obj.OBJModel;
import cam72cam.mod.render.obj.OBJRender;
import cam72cam.mod.render.opengl.VBO;
import cam72cam.mod.resource.Identifier;
import trackapi.lib.Gauges;
import util.Matrix4;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TrackModel {
    private final Map<Identifier, OBJModel> models;
    private final Map<String, Identifier> mapper;
    private TrackState state;
    private final List<String> random;
    private TrackOrder order;
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
        this.state = TrackState.SINGLE;
        this.random = new ArrayList<>();
        this.mapper = null;
    }

    public TrackModel(String condition, Map<String, Identifier> map, double modelGaugeM, double spacing) throws Exception {
        this.models = new HashMap<>();
        this.mapper = map;

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
        this.random = new ArrayList<>();
    }

    private double calculateRailHeight(OBJModel model) {
        List<String> railGroups = model.groups().stream()
                                       .filter(group -> group.contains("RAIL_LEFT") || group.contains("RAIL_RIGHT"))
                                       .collect(Collectors.toList());
        return model.maxOfGroup(railGroups).y;
    }

    public void setRandomWeight(Function<String, Integer> weightMap) {
        this.state = TrackState.RANDOM;

        // Build weighted order list
        random.clear();
        for (String key : this.mapper.keySet()) {
            int weight = weightMap.apply(key);
            for (int i = 0; i < weight; i++) {
                random.add(key);
            }
        }
    }

    public void setOrder(TrackOrder order) {
        this.state = TrackState.ORDERED;
        this.order = order;
    }

    public boolean canRender(double gauge) {
        switch (compare) {
            case ">": return gauge > size;
            case "<": return gauge < size;
            case "=": return gauge == size;
            default: return true;
        }
    }

    public MultiVBO getModel(RailInfo info, List<BuilderBase.VecYawPitch> data, long seed) {
        // Special track types always use single model
        if (info.settings.type.isTable()) {
            return getSingleModel(info, data);
        }

        switch (state) {
            case SINGLE: return getSingleModel(info, data);
            case RANDOM: return getRandomModel(info, data, seed);
            case ORDERED: return getOrderedModel(info, data);
            default: throw new IllegalArgumentException("?");
        }
    }

    private MultiVBO getOrderedModel(RailInfo info, List<VecYawPitch> data) {
        Map<String, OBJRender.Builder> vboMap = new HashMap<>();

        List<String> names = order.getRenderOrder(data.size());
        for (int i = 0; i < names.size(); i++) {
            String modelKey = names.get(i);
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

    private MultiVBO getRandomModel(RailInfo info, List<BuilderBase.VecYawPitch> data, long seed) {
        Map<String, OBJRender.Builder> vboMap = new HashMap<>();
        Random random = new Random(seed);

        for (VecYawPitch datum : data) {
            String modelKey = this.random.get(random.nextInt(this.random.size()));
            OBJModel model = models.get(mapper.get(modelKey));

            if (!vboMap.containsKey(modelKey)) {
                vboMap.put(modelKey, model.binder().builder());
            }

            renderPiece(info, datum, model, vboMap.get(modelKey));
        }

        Set<VBO> vbos = vboMap.values().stream()
                              .map(OBJRender.Builder::build)
                              .collect(Collectors.toSet());

        return new MultiVBO(vbos);
    }

    private MultiVBO getSingleModel(RailInfo info, List<BuilderBase.VecYawPitch> data) {
        OBJModel model = models.values().iterator().next();
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
                while (true){
                    if(n + f < length){
                        if(n <= near.size()){
                            n++;
                        }
                    } else {
                        break;
                    }

                    if(n + f < length){
                        if(f<=far.size()){
                            f++;
                        }
                    } else {
                        break;
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
                for(int i = 0, j = near.size(); j < length - near.size() - far.size(); i = (i+1) % mid.size(), j++){
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
                    int max = Integer.parseInt(str[1]);
                    for (int i = 1; i < max; i++) {
                        result.add(str[0]);
                    }
                }
            }
            return result;
        }
    }

    private enum TrackState {
        SINGLE,
        RANDOM,
        ORDERED
    }
}