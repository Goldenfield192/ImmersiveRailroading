package cam72cam.immersiverailroading.model.script;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import util.Matrix4;

import java.util.Arrays;
import java.util.HashMap;

public class ScriptAnimator {
    public LuaValue script;

    public ScriptAnimator(LuaValue script) {
        this.script = script;
    }

    public HashMap<String, Matrix4> getMatrices(String uuid){
        HashMap<String, Matrix4> matrices = new HashMap<>();

        LuaValue value = script.call(uuid);
        LuaTable table = new LuaTable(value);
        Arrays.stream(table.keys()).map(LuaValue::toString).forEach(s -> matrices.put(s, constructMatrix(table.get(s))));

        return matrices;
    }

    private static Matrix4 constructMatrix(LuaValue value){
        try{
            return new Matrix4(value.get("d00").todouble(), value.get("d01").todouble(), value.get("d02").todouble(), value.get("d03").todouble(),
                    value.get("d10").todouble(), value.get("d11").todouble(), value.get("d12").todouble(), value.get("d13").todouble(),
                    value.get("d20").todouble(), value.get("d21").todouble(), value.get("d22").todouble(), value.get("d23").todouble(),
                    value.get("d30").todouble(), value.get("d31").todouble(), value.get("d32").todouble(), value.get("d33").todouble());
        }catch (LuaError e){
            return new Matrix4(1,0,0,0,
                               0,1,0,0,
                               0,0,1,0,
                               0,0,0,1);
        }
    }
}
