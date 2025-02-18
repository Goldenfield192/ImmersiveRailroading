package cam72cam.immersiverailroading.script;

import cam72cam.mod.ModCore;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.function.*;

/**
 * Wrapper class to implement Lambda support for lua function registry
 */
public class LuaLibrary extends LuaTable {
    public String namespace;

    public LuaLibrary(String namespace){
        this.namespace = namespace;
    }

    /**
     * Register a function which has no input and output
     * @param name Name of the function
     * @param function Functioning part
     * @return The library itself
     */
    public LuaLibrary register(String name, Runnable function){
        if(!this.get(name).isnil()){
            ModCore.error(String.format("Detected duplicated function registry with name %s under the same namespace %s, the latter one won't be registered!", name, this.namespace));
            return this;
        }
        this.set(name, new LuaFunction() {
            @Override
            public LuaValue call() {
                function.run();
                return LuaValue.NIL;
            }
        });
        return this;
    }

    /**
     * Register a single parameter consumer function, like setter
     * @param name Name of the function
     * @param consumer Functioning part
     * @return The library itself
     */
    public LuaLibrary register(String name, Consumer<LuaValue> consumer){
        if(!this.get(name).isnil()){
            ModCore.error(String.format("Detected duplicated function registry with name %s under the same namespace %s, the latter one won't be registered!", name, this.namespace));
            return this;
        }
        this.set(name, new LuaFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                consumer.accept(arg);
                return LuaValue.NIL;
            }
        });
        return this;
    }

    /**
     * Register a double parameter consumer function, like setter
     * @param name Name of the function
     * @param consumer Functioning part
     * @return The library itself
     */
    public LuaLibrary register(String name, BiConsumer<LuaValue, LuaValue> consumer){
        if(!this.get(name).isnil()){
            ModCore.error(String.format("Detected duplicated function registry with name %s under the same namespace %s, the latter one won't be registered!", name, this.namespace));
            return this;
        }
        this.set(name, new LuaFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2) {
                consumer.accept(arg1, arg2);
                return LuaValue.NIL;
            }
        });
        return this;
    }

    /**
     * Register a Triple parameter consumer function, like setter
     * @param name Name of the function
     * @param consumer Functioning part
     * @return The library itself
     */
    public LuaLibrary register(String name, TriConsumer<LuaValue, LuaValue, LuaValue> consumer){
        if(!this.get(name).isnil()){
            ModCore.error(String.format("Detected duplicated function registry with name %s under the same namespace %s, the latter one won't be registered!", name, this.namespace));
            return this;
        }
        this.set(name, new LuaFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
                consumer.accept(arg1, arg2, arg3);
                return LuaValue.NIL;
            }
        });
        return this;
    }

    /**
     * Register a zero parameter function with a return value, like getter
     * @param name Name of the function
     * @param supplier Functioning part
     * @return The library itself
     */
    public LuaLibrary register(String name, Supplier<LuaValue> supplier){
        if(!this.get(name).isnil()){
            ModCore.error(String.format("Detected duplicated function registry with name %s under the same namespace %s, the latter one won't be registered!", name, this.namespace));
            return this;
        }
        if(!this.get(name).isnil()){
            ModCore.error(String.format("Detected duplicated function registry with name %s under the same namespace %s, the latter one won't be registered!", name, this.namespace));
        }
        this.set(name, new LuaFunction() {
            @Override
            public LuaValue call() {
                return supplier.get();
            }
        });
        return this;
    }

    /**
     * Register a single parameter function with a return value
     * @param name Name of the function
     * @param function Functioning part
     * @return The library itself
     */
    public LuaLibrary register(String name, Function<LuaValue, LuaValue> function){
        if(!this.get(name).isnil()){
            ModCore.error(String.format("Detected duplicated function registry with name %s under the same namespace %s, the latter one won't be registered!", name, this.namespace));
            return this;
        }
        this.set(name, new LuaFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                return function.apply(arg);
            }
        });
        return this;
    }

    /**
     * Register a double parameter function with a return value
     * @param name Name of the function
     * @param function Functioning part
     * @return The library itself
     */
    public LuaLibrary register(String name, BiFunction<LuaValue, LuaValue, LuaValue> function){
        if(!this.get(name).isnil()){
            ModCore.error(String.format("Detected duplicated function registry with name %s under the same namespace %s, the latter one won't be registered!", name, this.namespace));
            return this;
        }
        this.set(name, new LuaFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2) {
                return function.apply(arg1, arg2);
            }
        });
        return this;
    }

    /**
     * Register a triple parameter function with a return value
     * @param name Name of the function
     * @param function Functioning part
     * @return The library itself
     */
    public LuaLibrary register(String name, TriFunction<LuaValue, LuaValue, LuaValue, LuaValue> function){
        if(!this.get(name).isnil()){
            ModCore.error(String.format("Detected duplicated function registry with name %s under the same namespace %s, the latter one won't be registered!", name, this.namespace));
            return this;
        }
        this.set(name, new LuaFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
                return function.apply(arg1, arg2, arg3);
            }
        });
        return this;
    }

    /**
     * A consumer which consumes three parameters
     * @param <I> Parameter No.1's type
     * @param <J> Parameter No.2's type
     * @param <K> Parameter No.3's type
     */
    @FunctionalInterface
    public interface TriConsumer<I,J,K> {
        void accept(I arg1, J arg2, K arg3);
    }

    /**
     * A function which turns three parameters into one return value
     * @param <I> Parameter No.1's type
     * @param <J> Parameter No.2's type
     * @param <K> Parameter No.3's type
     * @param <R> Return value's type
     */
    @FunctionalInterface
    public interface TriFunction<I,J,K,R> {
        R apply(I arg1, J arg2, K arg3);
    }
}
