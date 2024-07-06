package cam72cam.immersiverailroading.model.animation;

import cam72cam.immersiverailroading.library.ModelComponentType;
import cam72cam.immersiverailroading.model.part.Control;
import cam72cam.mod.entity.Player;
import cam72cam.mod.serialization.SerializationException;
import cam72cam.mod.serialization.TagCompound;
import cam72cam.mod.serialization.TagField;
import cam72cam.mod.serialization.TagMapper;
import org.apache.commons.lang3.tuple.Pair;
import util.Matrix4;

import java.util.Map;
import java.util.function.Function;

public interface IAnimationProvider {
    void onDragStart(Control<?> control);

    void onDrag(Control<?> control, double newValue);

    void onDragRelease(Control<?> control);

    float defaultControlPosition(Control<?> control);

    Pair<Boolean, Float> getControlData(String control);

    Pair<Boolean, Float> getControlData(Control<?> control);

    boolean getControlPressed(Control<?> control);

    void setControlPressed(Control<?> control, boolean pressed);

    float getControlPosition(Control<?> control);

    float getControlPosition(String control);

    void setControlPosition(Control<?> control, float val);

    void setControlPosition(String control, float val);

    void setControlPositions(ModelComponentType type, float val);

    boolean playerCanDrag(Player player, Control<?> control);

    Matrix4 getModelMatrix();

    class ControlPositionMapper implements TagMapper<Map<String, Pair<Boolean, Float>>> {
        @Override
        public TagAccessor<Map<String, Pair<Boolean, Float>>> apply(
                Class<Map<String, Pair<Boolean, Float>>> type,
                String fieldName,
                TagField tag) throws SerializationException {
            return new TagAccessor<>(
                    (d, o) -> d.setMap(fieldName, o, Function.identity(), x -> new TagCompound().setBoolean("pressed", x.getLeft()).setFloat("pos", x.getRight())),
                    d -> d.getMap(fieldName, Function.identity(), x -> Pair.of(x.hasKey("pressed") && x.getBoolean("pressed"), x.getFloat("pos")))
            );
        }
    }
}
