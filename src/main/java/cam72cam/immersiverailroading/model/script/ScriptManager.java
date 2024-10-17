package cam72cam.immersiverailroading.model.script;

import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import org.apache.commons.lang3.tuple.Pair;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ScriptManager {
    public static Globals standardGlobals = JsePlatform.standardGlobals();

    private static HashMap<EntityRollingStockDefinition, Pair<LuaValue, LuaValue>> references = new HashMap<>();

    public static void init(){
        DefinitionManager.getDefinitions().stream().filter(def -> def.renderScript.canLoad()).forEach(def -> {
            try {
                InputStreamReader reader = new InputStreamReader(def.renderScript.getResourceStream());
                LuaValue script = standardGlobals.load(reader, def.defID);
                references.put(def, Pair.of(script.get("render"), script.get("tick")));
            } catch (IOException ignored) {
            }
        });
    }

    public static LuaValue getRenderScript(EntityRollingStockDefinition def){
        if(def == null){
            throw new NullPointerException();
        }
        return references.get(def).getKey();
    }

    public static ScriptAnimator getScriptAnimator(EntityRollingStockDefinition def){
        return new ScriptAnimator(getRenderScript(def));
    }

    public static LuaValue getTickingScript(EntityRollingStockDefinition def){
        if(def == null){
            throw new NullPointerException();
        }
        return references.get(def).getValue();
    }
}
