package cam72cam.immersiverailroading.gui;

import cam72cam.immersiverailroading.library.*;
import cam72cam.immersiverailroading.tile.TileRailBase;
import cam72cam.mod.entity.Player;
import cam72cam.mod.gui.helpers.GUIHelpers;
import cam72cam.mod.gui.screen.*;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.net.Packet;
import cam72cam.mod.render.opengl.RenderState;
import cam72cam.mod.serialization.TagField;
import cam72cam.mod.text.TextUtil;

import java.util.function.Function;

import static cam72cam.immersiverailroading.gui.ClickListHelper.next;

public class AugmentFilterGUI implements IScreen {
    private final Vec3i pos;
    private final Augment augment;
    private final Augment.Properties properties;
    private TextField includeTags;
    private TextField excludeTags;
    private Button stockDetectorMode;
    private Button redstoneMode;
    private CheckBox pushpull;
    private Button couplerMode;
    private Button locoControlMode;

    public AugmentFilterGUI(TileRailBase tileRailBase) {
        this.pos = tileRailBase.getPos();
        this.augment = tileRailBase.getAugment();
        this.properties = tileRailBase.getAugmentProperties() == null
                          ? Augment.Properties.EMPTY
                          : tileRailBase.getAugmentProperties();
    }

    @Override
    public void init(IScreenBuilder screen) {
        int xtop = -GUIHelpers.getScreenWidth() / 2;
        int ytop = -GUIHelpers.getScreenHeight() / 4;

        int xOffset = 0;
        int yOffset = 40;

        includeTags = new TextField(screen, xtop + xOffset, ytop + yOffset,
                                    200-1, 20);
        includeTags.setText(properties.positiveFilter);
        includeTags.setValidator(s -> {
            properties.positiveFilter = s;
            return true;
        });
        yOffset += 40;

        excludeTags = new TextField(screen, xtop + xOffset, ytop + yOffset,
                                    200-1, 20);
        excludeTags.setText(properties.negativeFilter);
        excludeTags.setValidator(s -> {
            properties.negativeFilter = s;
            return true;
        });
        yOffset += 25;

        Function<Enum<?>, String> translate = e -> TextUtil.translate(e.toString());

        stockDetectorMode = new Button(screen, xtop + xOffset, ytop + yOffset, "Stock Detect Mode: " + translate.apply(properties.stockDetectorMode)) {
            @Override
            public void onClick(Player.Hand hand) {
                properties.stockDetectorMode = next(properties.stockDetectorMode, Player.Hand.PRIMARY);
                stockDetectorMode.setText("Detecting: " + translate.apply(properties.stockDetectorMode));
            }
        };
        stockDetectorMode.setEnabled(this.augment == Augment.DETECTOR);
        yOffset += 25;

        redstoneMode = new Button(screen, xtop + xOffset, ytop + yOffset, "Redstone Mode: " + translate.apply(properties.redstoneMode)) {
            @Override
            public void onClick(Player.Hand hand) {
                properties.redstoneMode = next(properties.redstoneMode, Player.Hand.PRIMARY);
                redstoneMode.setText("Redstone Mode: " + translate.apply(properties.redstoneMode));
            }
        };
        redstoneMode.setEnabled(this.augment == Augment.COUPLER
                                || this.augment == Augment.ITEM_LOADER
                                || this.augment == Augment.ITEM_UNLOADER
                                || this.augment == Augment.FLUID_LOADER
                                || this.augment == Augment.FLUID_UNLOADER);
        yOffset += 25;

        pushpull = new CheckBox(screen, xtop + xOffset, ytop + yOffset, "Enable Pushpull", properties.pushpull) {
            @Override
            public void onClick(Player.Hand hand) {
                properties.pushpull = !properties.pushpull;
                pushpull.setChecked(properties.pushpull);
            }
        };
        pushpull.setEnabled(this.augment == Augment.COUPLER
                            || this.augment == Augment.ITEM_LOADER
                            || this.augment == Augment.ITEM_UNLOADER
                            || this.augment == Augment.FLUID_LOADER
                            || this.augment == Augment.FLUID_UNLOADER);
        yOffset += 15;

        couplerMode = new Button(screen, xtop + xOffset, ytop + yOffset, "Coupler Mode: " + translate.apply(properties.couplerAugmentMode)) {
            @Override
            public void onClick(Player.Hand hand) {
                properties.couplerAugmentMode = next(properties.couplerAugmentMode, Player.Hand.PRIMARY);
                couplerMode.setText("Coupler Mode: " + translate.apply(properties.couplerAugmentMode));
            }
        };
        couplerMode.setEnabled(this.augment == Augment.COUPLER);
        yOffset += 25;

        locoControlMode = new Button(screen, xtop + xOffset, ytop + yOffset, "Locomotive Control Mode: " + translate.apply(properties.locoControlMode)) {
            @Override
            public void onClick(Player.Hand hand) {
                properties.locoControlMode = next(properties.locoControlMode, Player.Hand.PRIMARY);
                locoControlMode.setText("Locomotive Control Mode: " + translate.apply(properties.locoControlMode));
            }
        };
        locoControlMode.setEnabled(this.augment == Augment.LOCO_CONTROL);
    }

    @Override
    public void onEnterKey(IScreenBuilder builder) {
        builder.close();
    }

    @Override
    public void onClose() {
        if(properties.positiveFilter == null) {
            properties.positiveFilter = "";
        }
        if (properties.negativeFilter == null) {
            properties.negativeFilter = "";
        }
        new AugmentFilterChangePacket(pos, properties).sendToServer();
    }

    @Override
    public void draw(IScreenBuilder builder, RenderState state) {
        IScreen.super.draw(builder, state);

        GUIHelpers.drawRect(0, 0, GUIHelpers.getScreenWidth(), GUIHelpers.getScreenHeight(), 0x88000000);

        GUIHelpers.drawRect(0, 0, 200, GUIHelpers.getScreenHeight(), 0xCC000000);

        int xtop = -GUIHelpers.getScreenWidth() / 2;
        int ytop = -GUIHelpers.getScreenHeight() / 4;

        int xOffset = 100;
        int yOffset = 30;
        GUIHelpers.drawCenteredString("Current Augment: " + TextUtil.translate("item.immersiverailroading:item_augment." + this.augment.toString() + ".name"), xOffset,  10, 0xFFFFFFFF);

        GUIHelpers.drawCenteredString("Included Tags", xOffset,  yOffset, 0xFFFFFFFF);
        includeTags.setText(properties.positiveFilter);
        yOffset+=40;

        GUIHelpers.drawCenteredString("Excluded Tags", xOffset,  yOffset, 0xFFFFFFFF);
        excludeTags.setText(properties.negativeFilter);
    }

    public static class AugmentFilterChangePacket extends Packet {
        @TagField
        Vec3i pos;

        @TagField
        Augment.Properties properties;

        public AugmentFilterChangePacket() {
        }

        public AugmentFilterChangePacket(Vec3i pos, Augment.Properties filter) {
            this.pos = pos;
            this.properties = filter;
        }

        @Override
        protected void handle() {
            TileRailBase railBase = this.getWorld().getBlockEntity(pos, TileRailBase.class);
            if (railBase != null && railBase.getAugment() != null) {
                railBase.setAugmentProperties(properties);
            }
        }
    }
}
