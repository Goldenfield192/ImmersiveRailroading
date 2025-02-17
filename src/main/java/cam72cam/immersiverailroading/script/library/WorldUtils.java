package cam72cam.immersiverailroading.script.library;

import cam72cam.immersiverailroading.entity.EntityScriptableRollingStock;
import cam72cam.immersiverailroading.script.LuaLibrary;
import cam72cam.mod.math.Vec3i;
import org.luaj.vm2.LuaValue;

@LuaLibrary("World")
public class WorldUtils {
    public LuaValue isRaining(EntityScriptableRollingStock stock){
        return LuaValue.valueOf(stock.getWorld().isRaining(stock.getBlockPosition()));
    }

    public LuaValue isRainingAtPosition(EntityScriptableRollingStock stock, LuaValue x, LuaValue y, LuaValue z){
        return LuaValue.valueOf(stock.getWorld().isRaining(new Vec3i(x.toint(),y.toint(),z.toint())));
    }

    public LuaValue getSkyLightLevelAtPosition(EntityScriptableRollingStock stock, LuaValue x, LuaValue y, LuaValue z){
        return LuaValue.valueOf(stock.getWorld().getSkyLightLevel(new Vec3i(x.toint(),y.toint(),z.toint())));
    }

    public LuaValue getBlockLightLevelAtPosition(EntityScriptableRollingStock stock, LuaValue x, LuaValue y, LuaValue z){
        return LuaValue.valueOf(stock.getWorld().getBlockLightLevel(new Vec3i(x.toint(),y.toint(),z.toint())));
    }
}
