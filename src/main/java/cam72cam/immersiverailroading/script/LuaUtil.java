package cam72cam.immersiverailroading.script;

import cam72cam.immersiverailroading.entity.ObjectValue;
import cam72cam.immersiverailroading.util.DataBlock;
import org.luaj.vm2.LuaValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LuaUtil {
    public static DataBlock luaTableToDataBlock(LuaValue luaTable) {
        return new DataBlock() {
            private final Map<String, Value> valueMap = new HashMap<>();
            private final Map<String, List<Value>> valuesMap = new HashMap<>();
            private final Map<String, DataBlock> blockMap = new HashMap<>();
            private final Map<String, List<DataBlock>> blocksMap = new HashMap<>();
            {
                for (LuaValue key : luaTable.checktable().keys()) {
                    String keyString = key.tojstring();
                    LuaValue value = luaTable.get(key);
                    if (value.istable()) {
                        if (isListTable(value)) {
                            List<Value> valueList = new ArrayList<>();
                            for (int i = 1; i <= value.length(); i++) {
                                valueList.add(new ObjectValue(convertLuaValue(value.get(i))));
                            }
                            valuesMap.put(keyString, valueList);
                        } else {
                            blockMap.put(keyString, luaTableToDataBlock(value));
                        }
                    } else {
                        valueMap.put(keyString, new ObjectValue(convertLuaValue(value)));
                    }
                }
            }

            @Override
            public Map<String, Value> getValueMap() {
                return valueMap;
            }

            @Override
            public Map<String, List<Value>> getValuesMap() {
                return valuesMap;
            }

            @Override
            public Map<String, DataBlock> getBlockMap() {
                return blockMap;
            }

            @Override
            public Map<String, List<DataBlock>> getBlocksMap() {
                return blocksMap;
            }
        };
    }

    private static boolean isListTable(LuaValue luaTable) {
        int index = 1;
        for (LuaValue key : luaTable.checktable().keys()) {
            if (!key.isint() || key.toint() != index) {
                return false;
            }
            index++;
        }
        return true;
    }

    public static Object convertLuaValue(LuaValue luaValue) {
        if (luaValue.isboolean()) {
            return luaValue.toboolean();
        } else if (luaValue.isint()) {
            return luaValue.toint();
        } else if (luaValue.isnumber()) {
            return luaValue.todouble();
        } else if (luaValue.isstring()) {
            return luaValue.tojstring();
        } else if (luaValue.istable()) {
            return luaTableToDataBlock(luaValue);
        }
        return null;
    }

    public static Object convertLuaValueText(LuaValue k, LuaValue value) {
        if ("text".equals(k.tojstring())) {
            return value.tojstring();  // Force the value to be a string
        }

        if (value.isboolean()) {
            return value.toboolean();
        } else if (value.isnumber()) {
            return value.todouble();
        } else if (value.isstring()) {
            return value.tojstring();
        } else if (value.istable()) {
            Map<String, Object> nestedMap = new HashMap<>();
            for (LuaValue key : value.checktable().keys()) {
                LuaValue val = value.get(key);
                nestedMap.put(key.tojstring(), convertLuaValue(val));
            }
            return nestedMap;
        } else {
            return value; // Return raw LuaValue if type is unknown
        }
    }
}
