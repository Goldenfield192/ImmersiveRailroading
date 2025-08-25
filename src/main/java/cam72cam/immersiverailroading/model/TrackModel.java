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
    private final List<String> order;
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
        this.order = new ArrayList<>();
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
        this.order = new ArrayList<>();
    }

    private double calculateRailHeight(OBJModel model) {
        List<String> railGroups = model.groups().stream()
                                       .filter(group -> group.contains("RAIL_LEFT") || group.contains("RAIL_RIGHT"))
                                       .collect(Collectors.toList());
        return model.maxOfGroup(railGroups).y;
    }

    public TrackModel setRandomWeight(Function<String, Integer> weightMap) {
        this.state = TrackState.RANDOM;
        int totalWeight = 0;

        // Calculate total weight
        for (String key : this.mapper.keySet()) {
            totalWeight += weightMap.apply(key);
        }

        // Build weighted order list
        order.clear();
        for (String key : this.mapper.keySet()) {
            int weight = weightMap.apply(key);
            for (int i = 0; i < weight; i++) {
                order.add(key);
            }
        }

        return this;
    }

    public TrackModel setOrder(List<String> order) {
        this.state = TrackState.ORDERED;
        this.order.clear();
        this.order.addAll(order);
        return this;
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

        for (int i = 0; i < data.size(); i++) {
            String modelKey = order.get(i % order.size());
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
            String modelKey = order.get(random.nextInt(order.size()));
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
        Matrix4 matrix = createTransformationMatrix(info, piece);

        if (!piece.getGroups().isEmpty()) {
            List<String> groups = model.groups().stream()
                                       .filter(group -> piece.getGroups().stream().anyMatch(group::contains))
                                       .collect(Collectors.toList());
            builder.draw(groups, matrix);
        } else {
            builder.draw(matrix);
        }
    }

    private Matrix4 createTransformationMatrix(RailInfo info, VecYawPitch piece) {
        Matrix4 matrix = new Matrix4();
        matrix.translate(piece.x, piece.y, piece.z);
        matrix.rotate(Math.toRadians(piece.getYaw()), 0, 1, 0);
        matrix.rotate(Math.toRadians(piece.getPitch()), 1, 0, 0);
        matrix.rotate(Math.toRadians(-90), 0, 1, 0);

        if (piece.getLength() != -1) {
            matrix.scale(piece.getLength() / info.settings.gauge.scale(), 1, 1);
        }

        double scale = info.settings.gauge.scale();
        matrix.scale(scale, scale, scale);

        return matrix;
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

    private enum TrackState {
        SINGLE,
        RANDOM,
        ORDERED
    }
}