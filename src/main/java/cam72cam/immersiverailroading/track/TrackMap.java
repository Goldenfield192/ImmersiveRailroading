package cam72cam.immersiverailroading.track;

import cam72cam.immersiverailroading.Config;
import cam72cam.immersiverailroading.items.nbt.RailSettings;
import cam72cam.immersiverailroading.library.TrackDirection;
import cam72cam.immersiverailroading.tile.TileRail;
import cam72cam.immersiverailroading.tile.TileRailBase;
import cam72cam.immersiverailroading.util.PlacementInfo;
import cam72cam.immersiverailroading.util.VecUtil;
import cam72cam.mod.entity.Player;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.world.World;

import java.util.List;

//Still in early work
public class TrackMap {
    public static PlacementInfo getNeighborNode(Player player, World world, Vec3i pos, Vec3d hit, ItemStack stack) {
        RailSettings stackInfo = RailSettings.from(stack);
        Vec3d worldPos = new Vec3d(pos).add(hit);
        Vec3d minPos = worldPos;
        double min = Double.MAX_VALUE;
        int hori = (int) Math.max(stackInfo.gauge.scale() * 2, 1.5);
        int vert = 1;
        float yaw = player.getRotationYaw();

        float rotationYawHead = player.getRotationYawHead();
        rotationYawHead = (rotationYawHead + 360f) % 360f;
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
                        if(renderData.size() > 1){
                            Vec3d p1 = renderData.get(0).add(rail.info.placementInfo.placementPosition).add(
                                    tile.getPos());
                            float yaw1 = VecUtil.toYaw(renderData.get(1).subtract(renderData.get(0)));

                            double v = p1.distanceTo(worldPos);
                            if (v < min) {
                                min = v;
                                minPos = p1;
                                yaw = yaw1;
                            }

                            Vec3d p2 = renderData.get(renderData.size() - 1)
                                                 .add(rail.info.placementInfo.placementPosition).add(tile.getPos());
                            float yaw2 = VecUtil.toYaw(renderData.get(renderData.size() - 2).subtract(renderData.get(renderData.size() - 1)));
                            v = p2.distanceTo(worldPos);
                            if (v < min) {
                                min = v;
                                minPos = p2;
                                yaw = yaw2;
                            }
                        } else {
                            Vec3d p = renderData.get(0).add(rail.info.placementInfo.placementPosition).add(
                                    tile.getPos());
                            float yaw1 = renderData.get(0).getYaw();

                            if (Math.abs(yaw1 - rotationYawHead) > 90) {
                                yaw1 += 180;
                            }

                            double v = p.distanceTo(worldPos);
                            if (v < min) {
                                min = v;
                                minPos = p;
                                yaw = yaw1;
                            }
                        }
                    }
                }
            }
        }

        if(!Config.ConfigDebug.trackSnapAngle) {
            yaw = rotationYawHead;
        }

        yaw = (yaw + 360) % 360;

        if(min <= hori){
            return new PlacementInfo(minPos, TrackDirection.NONE, 180 - yaw, null);
        } else {
            return null;
        }
    }
}
