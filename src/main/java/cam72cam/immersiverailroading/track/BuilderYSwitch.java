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

    private final BuilderIterator turnBuilder1;
    private final BuilderIterator turnBuilder2;

    public BuilderYSwitch(RailInfo info, World world, Vec3i pos) {
        super(info, world, pos);

        RailInfo turnInfo1 = info.withSettings(b -> b.type = info.customInfo.placementPosition.equals(info.placementInfo.placementPosition) ? TrackItems.TURN : TrackItems.CUSTOM);
        RailInfo turnInfo2 = info.withSettings(b -> b.type = info.customInfo.placementPosition.equals(info.placementInfo.placementPosition) ? TrackItems.TURN : TrackItems.CUSTOM);

        turnBuilder1 = (BuilderIterator) turnInfo1.getBuilder(world, pos);
        turnBuilder2 = (BuilderIterator) turnInfo2.getBuilder(world, pos);

        turnBuilder1.overrideFlexible = true;
        turnBuilder2.overrideFlexible = true;

        for(TrackBase turn : turnBuilder2.tracks) {
            if (turn instanceof TrackRail) {
                turn.overrideParent(turnBuilder1.getParentPos());
            }
        }
    }

    @Override
    public List<BuilderBase> getSubBuilders() {
        List<BuilderBase> subTurns = turnBuilder1.getSubBuilders();
        List<BuilderBase> subTurnsOther = turnBuilder2.getSubBuilders();

        if (subTurns == null && subTurnsOther == null) {
            return null;
        }

        List<BuilderBase> res = new ArrayList<>();
        if (subTurns == null) {
            res.add(turnBuilder1);
        } else {
            res.addAll(subTurns);
        }
        if (subTurnsOther == null) {
            res.add(turnBuilder2);
        } else {
            res.addAll(subTurnsOther);
        }
        return res;
    }

    @Override
    public int costTies() {
        return turnBuilder2.costTies() + turnBuilder1.costTies();
    }

    @Override
    public int costRails() {
        return turnBuilder2.costRails() + turnBuilder1.costRails();
    }

    @Override
    public int costBed() {
        return turnBuilder2.costBed() + turnBuilder1.costBed();
    }

    @Override
    public int costFill() {
        return turnBuilder2.costFill() + turnBuilder1.costFill();
    }

    @Override
    public void setDrops(List<ItemStack> drops) {
        turnBuilder1.setDrops(drops);
    }


    @Override
    public boolean canBuild() {
        return turnBuilder2.canBuild() && turnBuilder1.canBuild();
    }

    @Override
    public void build() {
        turnBuilder2.build();
        turnBuilder1.build();
    }

    @Override
    public void clearArea() {
        turnBuilder2.clearArea();
        turnBuilder1.clearArea();
    }

    @Override
    public List<TrackBase> getTracksForRender() {
        List<TrackBase> data = turnBuilder2.getTracksForRender();
        data.addAll(turnBuilder1.getTracksForRender());
        return data;
    }

    @Override
    public List<VecYawPitch> getRenderData() {
        List<VecYawPitch> data = turnBuilder2.getRenderData();
        data.addAll(turnBuilder1.getRenderData());
        return data;
    }

    @Override
    public List<PosStep> getPath(double stepSize) {
        return turnBuilder2.getPath(stepSize);
    }
}
