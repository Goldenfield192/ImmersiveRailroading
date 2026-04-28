package cam72cam.immersiverailroading;

import cam72cam.mod.config.ConfigFile.*;

@Comment("Configuration File")
@Name("Permissions")
@File("immersiverailroading_permissions.cfg")
public class ConfigPermissions {
    @Comment("If op should be required by default for these permissions, set the entry to true")
    public static class Defaults {
        public static final boolean LOCOMOTIVE_CONTROL = false;
        public static final boolean BRAKE_CONTROL = false;
        public static final boolean FREIGHT_INVENTORY = false;
        public static final boolean COUPLING_HOOK = false;
        public static final boolean PAINT_BRUSH = false;
        public static final boolean STOCK_ASSEMBLY = false;

        public static final boolean BOARD_LOCOMOTIVE = false;
        public static final boolean BOARD_STOCK = false;
        public static final boolean BOARD_WITH_LEAD = false;
        public static final boolean CONDUCTOR = false;

        public static final boolean AUGMENT_TRACK = false;
        public static final boolean SWITCH_CONTROL = false;
        public static final boolean EXCHANGE_TRACK = false;
        public static final boolean BUILD_TRACK = false;
        public static final boolean BREAK_TRACK = false;

        public static final boolean MACHINIST = false;
    }
}
