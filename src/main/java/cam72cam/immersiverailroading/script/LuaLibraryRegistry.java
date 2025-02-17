package cam72cam.immersiverailroading.script;

import cam72cam.immersiverailroading.entity.EntityScriptableRollingStock;
import cam72cam.immersiverailroading.script.library.StockUtils;
import cam72cam.immersiverailroading.script.library.WorldUtils;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class LuaLibraryRegistry {
    private static final LinkedList<Class<?>> classes = new LinkedList<>();

    public Map<String, LuaTable> functions = new HashMap<>();
    private final EntityScriptableRollingStock stockReference;

    public static void loadFunctions(EntityScriptableRollingStock stock, Globals globals){
        LuaLibraryRegistry registry = new LuaLibraryRegistry(stock);
        registry.register();
        registry.functions.forEach(globals::set);
    }

    private LuaLibraryRegistry(EntityScriptableRollingStock stock) {
        stockReference = stock;
        //I want to get classes by package name...maybe future
        if (classes.isEmpty()) {
            classes.add(StockUtils.class);
            classes.add(WorldUtils.class);
        }
    }

    private void register() {
        //Use reflection to register dynamically
        classes.forEach(aClass -> {
            LuaTable table = new LuaTable();
            for (Method method : aClass.getDeclaredMethods()) {
                if (method.getParameterTypes()[0] != EntityScriptableRollingStock.class) {
                    break;
                }

                switch (method.getParameterCount()) {
                    //ZeroArgFunction
                    case 1:
                        table.set(method.getName(), new LuaFunction() {
                            @Override
                            public LuaValue call() {
                                try {
                                    return (LuaValue) method.invoke(null, stockReference);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                        break;

                    //OneArgFunction
                    case 2:
                        table.set(method.getName(), new LuaFunction() {
                            @Override
                            public LuaValue call(LuaValue value1) {
                                try {
                                    return (LuaValue) method.invoke(null, stockReference, value1);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                        break;

                    //TwoArgFunction
                    case 3:
                        table.set(method.getName(), new LuaFunction() {
                            @Override
                            public LuaValue call(LuaValue value1, LuaValue value2) {
                                try {
                                    return (LuaValue) method.invoke(null, stockReference, value1, value2);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                        break;

                    //ThreeArgFunction
                    case 4:
                        table.set(method.getName(), new LuaFunction() {
                            @Override
                            public LuaValue call(LuaValue value1, LuaValue value2, LuaValue value3) {
                                try {
                                    return (LuaValue) method.invoke(null, stockReference, value1, value2, value3);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                        break;
                }
            }

            functions.put(aClass.getAnnotation(LuaLibrary.class).value(), table);
        });
    }
}
