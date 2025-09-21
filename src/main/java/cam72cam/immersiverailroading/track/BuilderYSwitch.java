package cam72cam.immersiverailroading.track;

import cam72cam.immersiverailroading.library.TrackItems;
import cam72cam.immersiverailroading.util.RailInfo;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class BuilderYSwitch extends BuilderBase implements IIterableTrack {

    private BuilderIterator turnBuilder1;
    private BuilderIterator turnBuilder2;
    private BuilderStraight straightBuilder;
    private BuilderStraight realStraightBuilder;
    private final BuilderStraight straightBuilderReal;

    public BuilderYSwitch(RailInfo info, World world, Vec3i pos) {
        super(info, world, pos);

        RailInfo turnInfo = info.withSettings(b -> b.type = info.customInfo.placementPosition.equals(info.placementInfo.placementPosition) ? TrackItems.TURN : TrackItems.CUSTOM);
        RailInfo straightInfo = info;

        {
            turnBuilder1 = (BuilderIterator) turnInfo.getBuilder(world, pos);
            straightBuilder = new BuilderStraight(straightInfo, world, pos, true);
            realStraightBuilder = new BuilderStraight(straightInfo, world, pos, true);

            straightInfo = straightInfo.withSettings(b -> {
                double maxOverlap = 0;

                straightBuilder.positions.retainAll(turnBuilder1.positions);

                for (Pair<Integer, Integer> straight : straightBuilder.positions) {
                    maxOverlap = Math.max(maxOverlap, new Vec3d(straight.getKey(), 0, straight.getValue()).length());
                }

                maxOverlap *= 1.2;
                b.length = (int) Math.ceil(maxOverlap) + 3;
            });
        }


        straightBuilder = new BuilderStraight(straightInfo, world, pos, true);
        straightBuilderReal = new BuilderStraight(straightInfo.withSettings(b -> b.type = TrackItems.STRAIGHT), world, pos, true);

        turnBuilder1.overrideFlexible = true;

        for(TrackBase turn : turnBuilder1.tracks) {
            if (turn instanceof TrackRail) {
                turn.overrideParent(straightBuilder.getParentPos());
            }
        }
        for (TrackBase straight : straightBuilder.tracks) {
            if (straight instanceof TrackGag) {
                straight.setFlexible();
            }
        }
    }

    @Override
    public List<BuilderBase> getSubBuilders() {
        List<BuilderBase> subTurns = turnBuilder1.getSubBuilders();
        List<BuilderBase> subStraights = straightBuilderReal.getSubBuilders();

        if (subTurns == null && subStraights == null) {
            return null;
        }

        List<BuilderBase> res = new ArrayList<>();
        if (subTurns == null) {
            res.add(turnBuilder1);
        } else {
            res.addAll(subTurns);
        }
        if (subStraights == null) {
            res.add(straightBuilderReal);
        } else {
            res.addAll(subStraights);
        }
        return res;
    }

    @Override
    public int costTies() {
        return straightBuilder.costTies() + turnBuilder1.costTies();
    }

    @Override
    public int costRails() {
        return straightBuilder.costRails() + turnBuilder1.costRails();
    }

    @Override
    public int costBed() {
        return straightBuilder.costBed() + turnBuilder1.costBed();
    }

    @Override
    public int costFill() {
        return straightBuilder.costFill() + turnBuilder1.costFill();
    }

    @Override
    public void setDrops(List<ItemStack> drops) {
        straightBuilder.setDrops(drops);
    }


    @Override
    public boolean canBuild() {
        return straightBuilder.canBuild() && turnBuilder1.canBuild();
    }

    @Override
    public void build() {
        straightBuilder.build();
        turnBuilder1.build();
    }

    @Override
    public void clearArea() {
        straightBuilder.clearArea();
        turnBuilder1.clearArea();
    }

    @Override
    public List<TrackBase> getTracksForRender() {
        List<TrackBase> data = straightBuilder.getTracksForRender();
        data.addAll(turnBuilder1.getTracksForRender());
        return data;
    }

    @Override
    public List<VecYawPitch> getRenderData() {
        List<VecYawPitch> data = straightBuilder.getRenderData();
        data.addAll(turnBuilder1.getRenderData());
        return data;
    }

    @Override
    public List<PosStep> getPath(double stepSize) {
        return realStraightBuilder.getPath(stepSize);
    }
}
