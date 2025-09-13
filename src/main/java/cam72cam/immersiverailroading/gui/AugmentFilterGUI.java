package cam72cam.immersiverailroading.gui;

import cam72cam.immersiverailroading.tile.TileRailBase;
import cam72cam.mod.gui.helpers.GUIHelpers;
import cam72cam.mod.gui.screen.IScreen;
import cam72cam.mod.gui.screen.IScreenBuilder;
import cam72cam.mod.gui.screen.TextField;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.net.Packet;
import cam72cam.mod.render.opengl.RenderState;
import cam72cam.mod.serialization.TagField;

public class AugmentFilterGUI implements IScreen {
    private final Vec3i pos;
    private TextField textField;
    private String filter;

    public AugmentFilterGUI(TileRailBase tileRailBase) {
        this.pos = tileRailBase.getPos();
        this.filter = tileRailBase.getAugmentFilter();
    }

    @Override
    public void init(IScreenBuilder screen) {
        textField = new TextField(screen, -GUIHelpers.getScreenWidth() / 4, -(GUIHelpers.getScreenHeight() / 8),
                                  200 - 1, 20);
        textField.setText(filter);
        textField.setFocused(true);
        textField.setValidator(s -> {
            filter = s;
            return true;
        });
    }

    @Override
    public void onEnterKey(IScreenBuilder builder) {
        builder.close();
    }

    @Override
    public void onClose() {
        String s = textField.getText();
        new AugmentFilterChangePacket(pos, s).sendToServer();
    }

    @Override
    public void draw(IScreenBuilder builder, RenderState state) {
        IScreen.super.draw(builder, state);
        textField.setText(filter);
        GUIHelpers.drawRect(0, 0, GUIHelpers.getScreenWidth(), GUIHelpers.getScreenHeight(), 0x88000000);
    }

    public static class AugmentFilterChangePacket extends Packet {
        @TagField
        Vec3i pos;

        @TagField
        String filter;

        public AugmentFilterChangePacket() {
        }

        public AugmentFilterChangePacket(Vec3i pos, String filter) {
            this.pos = pos;
            this.filter = filter;
        }

        @Override
        protected void handle() {
            TileRailBase railBase = this.getWorld().getBlockEntity(pos, TileRailBase.class);
            if (railBase != null && railBase.getAugment() != null) {
                railBase.setAugmentFilter(filter);
            }
        }
    }
}
