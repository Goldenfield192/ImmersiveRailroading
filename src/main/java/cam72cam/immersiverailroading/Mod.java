package cam72cam.immersiverailroading;

import cam72cam.mod.ModCore;

@net.minecraftforge.fml.common.Mod(modid = Mod.MODID, name = "#NAME#", version = "1.10.0", dependencies = "required-after:trackapi@[1.1,); required-after:universalmodcore@[1.2, 1.3)", acceptedMinecraftVersions = "[1.12,1.13)")
public class Mod {
    public static final String MODID = "immersiverailroading";

    static {
        try {
            ModCore.register(new ImmersiveRailroading());
        } catch (Exception e) {
            throw new RuntimeException("Could not load mod " + MODID, e);
        }
    }
}
