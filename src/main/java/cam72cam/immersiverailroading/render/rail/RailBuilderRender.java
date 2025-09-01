package cam72cam.immersiverailroading.render.rail;

import cam72cam.immersiverailroading.library.TrackItems;
import cam72cam.immersiverailroading.model.TrackModel;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.render.ExpireableMap;
import cam72cam.immersiverailroading.render.MultiVBO;
import cam72cam.mod.MinecraftClient;
import cam72cam.immersiverailroading.track.BuilderBase.VecYawPitch;
import cam72cam.immersiverailroading.util.RailInfo;
import cam72cam.mod.render.opengl.RenderState;

import java.util.List;

public class RailBuilderRender {
    private static final ExpireableMap<String, MultiVBO> cache = new ExpireableMap<>((k, v) -> v.free());

    public static void renderRailBuilder(RailInfo info, List<VecYawPitch> renderData, RenderState state) {
        TrackModel model = DefinitionManager.getTrack(info.settings.track, info.settings.gauge.value());
        if (model == null) {
            return;
        }

        MultiVBO cached = cache.get(info.uniqueID);
        if (cached == null) {
            cached = model.getModel(info, renderData);
            cache.put(info.uniqueID, cached);
        }

        MinecraftClient.startProfiler("irTrackModel");
        cached.bindAndDraw(state, info.settings.type == TrackItems.TURNTABLE);
        MinecraftClient.endProfiler();
    }
}
