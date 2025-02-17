package cam72cam.immersiverailroading.script.library;


import cam72cam.immersiverailroading.entity.EntityScriptableRollingStock;
import cam72cam.immersiverailroading.script.LuaLibrary;
import org.luaj.vm2.LuaValue;

@LuaLibrary("IR")
public class StockUtils {
    public LuaValue getCG(EntityScriptableRollingStock stock, LuaValue control) {
        float result = stock.getControlGroup(control.tojstring());
        return LuaValue.valueOf(result);
    }

    public LuaValue setCG(EntityScriptableRollingStock stock, LuaValue control, LuaValue val) {
        stock.setControlGroup(control.tojstring(), val.tofloat());
        return LuaValue.NIL;
    }

    public LuaValue getPaint(EntityScriptableRollingStock stock) {
        String result = stock.getCurrentTexture();
        return LuaValue.valueOf(result);
    }

    public LuaValue setPaint(EntityScriptableRollingStock stock, LuaValue newTexture) {
        stock.setNewTexture(newTexture.tojstring());
        return LuaValue.NIL;
    }

    public LuaValue getReadout(EntityScriptableRollingStock stock, LuaValue readout) {
        float result = stock.getReadout(readout.tojstring());
        return LuaValue.valueOf(result);
    }

    public LuaValue setPerformance(EntityScriptableRollingStock stock, LuaValue performanceType, LuaValue newVal) {
        stock.setPerformance(performanceType.tojstring(), newVal.todouble());
        return LuaValue.NIL;
    }

    public LuaValue couplerEngaged(EntityScriptableRollingStock stock, LuaValue position, LuaValue newState) {
        stock.setCouplerEngaged(position.tojstring(), newState.toboolean());
        return LuaValue.NIL;
    }

    public LuaValue setThrottle(EntityScriptableRollingStock stock, LuaValue val) {
        stock.setThrottleLua(val.tofloat());
        return LuaValue.NIL;
    }

    public LuaValue setReverser(EntityScriptableRollingStock stock, LuaValue val) {
        stock.setReverserLua(val.tofloat());
        return LuaValue.NIL;
    }

    public LuaValue setTrainBrake(EntityScriptableRollingStock stock, LuaValue val) {
        stock.setBrakeLua(val.tofloat());
        return LuaValue.NIL;
    }

    public LuaValue setIndependentBrake(EntityScriptableRollingStock stock, LuaValue val) {
        stock.setIndependentBrakeLua(val.tofloat());
        return LuaValue.NIL;
    }

    public LuaValue getThrottle(EntityScriptableRollingStock stock) {
        return LuaValue.valueOf(stock.getThrottleLua());
    }

    public LuaValue getReverser(EntityScriptableRollingStock stock) {
        return LuaValue.valueOf(stock.getReverserLua());
    }

    public LuaValue getTrainBrake(EntityScriptableRollingStock stock) {
        return LuaValue.valueOf(stock.getTrainBrakeLua());
    }

    public LuaValue setSound(EntityScriptableRollingStock stock, LuaValue val) {
        stock.setNewSound(val);
        return LuaValue.NIL;
    }

    public LuaValue setGlobal(EntityScriptableRollingStock stock, LuaValue control, LuaValue val) {
        stock.setGlobalControlGroup(control.tojstring(), val.tofloat());
        return LuaValue.NIL;
    }

    public LuaValue setUnit(EntityScriptableRollingStock stock, LuaValue control, LuaValue val) {
        stock.setUnitControlGroup(control.tojstring(), val.tofloat());
        return LuaValue.NIL;
    }

    public LuaValue setText(EntityScriptableRollingStock stock, LuaValue table) {
        stock.textFieldDef(table);
        return LuaValue.NIL;
    }

    public LuaValue getTag(EntityScriptableRollingStock stock) {
        return LuaValue.valueOf(stock.getTag());
    }

    public LuaValue getTrain(EntityScriptableRollingStock stock) {
        return stock.getTrainConsist();
    }

    public LuaValue setIndividualCG(EntityScriptableRollingStock stock, LuaValue luaValue) {
        stock.setIndividualCG(luaValue);
        return LuaValue.NIL;
    }

    public LuaValue getIndividualCG(EntityScriptableRollingStock stock, LuaValue luaValue) {
        return stock.getIndividualCG(luaValue);
    }

    public LuaValue engineStartStop(EntityScriptableRollingStock stock, LuaValue luaValue) {
        stock.setTurnedOnLua(luaValue.toboolean());
        return LuaValue.NIL;
    }

    public LuaValue isTurnedOn(EntityScriptableRollingStock stock) {
        return LuaValue.valueOf(stock.getEngineState());
    }

    public LuaValue setTag(EntityScriptableRollingStock stock, LuaValue tag) {
        stock.setEntityTag(tag.tojstring());
        return LuaValue.NIL;
    }

    public LuaValue newParticle(EntityScriptableRollingStock stock, LuaValue luaValue) {
        stock.particleDefinition(luaValue);
        return LuaValue.NIL;
    }

    public LuaValue getPerformance(EntityScriptableRollingStock stock, LuaValue luaValue) {
        return stock.getPerformance(luaValue.tojstring());
    }

    public LuaValue setNBTTag(EntityScriptableRollingStock stock, LuaValue key, LuaValue value) {
        stock.setNBTTag(key.tojstring(), value);
        return LuaValue.NIL;
    }

    public LuaValue getNBTTag(EntityScriptableRollingStock stock, LuaValue key) {
        return stock.getNBTTag(key.tojstring());
    }
}
