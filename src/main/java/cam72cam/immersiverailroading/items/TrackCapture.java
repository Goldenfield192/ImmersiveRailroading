package cam72cam.immersiverailroading.items;

import cam72cam.immersiverailroading.Config;
import cam72cam.immersiverailroading.items.nbt.RailSettings;
import cam72cam.immersiverailroading.tile.TileRail;
import cam72cam.immersiverailroading.tile.TileRailBase;
import cam72cam.immersiverailroading.track.BuilderBase;
import cam72cam.immersiverailroading.track.VecYPR;
import cam72cam.immersiverailroading.util.VecUtil;
import cam72cam.mod.MinecraftClient;
import cam72cam.mod.entity.Player;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

public class TrackCapture {
    public static Triple<Pair<Vec3i, Vec3d>, Float, Boolean> getNeighborEnd(Player player, World world, Vec3i pos, Vec3d hit, ItemStack stack) {
        Triple<Pair<Vec3i, Vec3d>, Float, Boolean> defaultTriple = Triple.of(Pair.of(pos, hit), player.getRotationYawHead(), false);
        if(!MinecraftClient.isReady()) {
            return defaultTriple;
        }
        RailSettings stackInfo = RailSettings.from(stack);
        Vec3d worldPos = new Vec3d(pos).add(hit);
        Vec3d minPos = worldPos;
        double min = Double.MAX_VALUE;
        int hori = (int) Math.max(stackInfo.gauge.scale() * 2, 1.5);
        int vert = 1;
        float yaw = player.getRotationYaw();

        for (int x = -hori; x <= hori; x++) {
            for (int y = -vert; y <= vert; y++) {
                for (int z = -hori; z <= hori; z++) {
                    Vec3i offset = pos.add(x, y, z);
                    TileRailBase tile = world.getBlockEntity(offset, TileRailBase.class);
                    if (tile != null) {
                        if(!(tile instanceof TileRail)){
                            tile = tile.getParentTile();
                        }

                        TileRail rail = (TileRail) tile;
                        if(rail == null || rail.info == null || Math.abs(rail.getTrackGauge() - stackInfo.gauge.value()) > 1.0E-6)
                            continue;

                        BuilderBase builder = rail.info.getBuilder(world);
                        if(rail.info == null || Math.abs(rail.getTrackGauge() - stackInfo.gauge.value()) > 1.0E-6)
                            continue;
                        List<VecYPR> renderData = builder.getRenderData();
                        Vec3d p1 = renderData.get(0).add(rail.info.placementInfo.placementPosition).add(tile.getPos());
                        float yaw1 = renderData.get(0).getYaw();

                        if (renderData.size() >= 2) {
                            yaw1 = VecUtil.toYaw(renderData.get(1).subtract(renderData.get(0)));
                        }

                        double v = p1.distanceTo(worldPos);
                        if(v < min) {
                           min = v;
                           minPos = p1;
                           yaw = yaw1;
                        }

                        Vec3d p2 = renderData.get(renderData.size() - 1)
                                             .add(rail.info.placementInfo.placementPosition).add(tile.getPos());
                        float yaw2 = renderData.get(renderData.size() - 1).getYaw();
                        if (renderData.size() >= 2) {
                            yaw2 = VecUtil.toYaw(renderData.get(renderData.size() - 2).subtract(renderData.get(renderData.size() - 1)));
                        }
                        v = p2.distanceTo(worldPos);
                        if(v < min) {
                            min = v;
                            minPos = p2;
                            yaw = yaw2;
                        }
                    }
                }
            }
        }

        if(!Config.ConfigDebug.trackSnapAngle) {
            yaw = player.getRotationYawHead();
        }

        if(min <= hori){
            return Triple.of(Pair.of(new Vec3i(minPos), minPos.subtract(new Vec3i(minPos))), 180 - yaw, minPos != worldPos);
        } else {
            return Triple.of(Pair.of(pos, hit), 180 - yaw, false);
        }
    }
}
