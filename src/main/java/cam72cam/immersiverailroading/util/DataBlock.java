package cam72cam.immersiverailroading.util;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.mod.resource.Identifier;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("unused")
public interface DataBlock {
    Map<String, Value> getValueMap();
    Map<String, List<Value>> getValuesMap();
    Map<String, DataBlock> getBlockMap();
    Map<String, List<DataBlock>> getBlocksMap();

    default DataBlock getBlock(String key) {
        return getBlockMap().get(key);
    }
    default List<DataBlock> getBlocks(String key) {
        return getBlocksMap().get(key);
    }
    default Value getValue(String key) {
        return getValueMap().getOrDefault(key, Value.nullValue(key));
    }
    default List<Value> getValues(String key) {
        return getValuesMap().get(key);
    }

    abstract class Value {
        String name;
        boolean isNull;

        public Value(String name) {
            this(name, false);
        }

        public Value(String name, boolean isNull) {
            this.name = name;
            this.isNull = isNull;
        }

        public static Value nullValue(String name) {
            return new Value(name, true) {
                @Override
                public boolean asBoolean() {
                    throw new NullPointerException(String.format("Attempting to get boolean with key %s", this.name));
                }

                @Override
                public int asInteger() {
                    throw new NullPointerException(String.format("Attempting to get integer with key %s", this.name));
                }

                @Override
                public float asFloat() {
                    throw new NullPointerException(String.format("Attempting to get float with key %s", this.name));
                }

                @Override
                public double asDouble() {
                    throw new NullPointerException(String.format("Attempting to get double with key %s", this.name));
                }

                @Override
                public String asString() {
                    throw new NullPointerException(String.format("Attempting to get String with key %s", this.name));
                }
            };
        }

        public abstract boolean asBoolean();
        public boolean asBoolean(boolean fallback) {
            return isNull ? fallback : asBoolean();
        }
        public boolean isBoolean() {
            try{
                this.asBoolean();
            } catch (Exception ignored) {
                return false;
            }
            return true;
        }

        public abstract int asInteger();
        public int asInteger(int fallback) {
            return isNull ? fallback : asInteger();
        }
        public boolean isInteger() {
            try{
                this.asInteger();
            } catch (Exception ignored) {
                return false;
            }
            return true;
        }

        public abstract float asFloat();
        public float asFloat(float fallback) {
            return isNull ? fallback : asFloat();
        }
        public boolean isFloat() {
            try{
                this.asFloat();
            } catch (Exception ignored) {
                return false;
            }
            return true;
        }

        public abstract double asDouble();
        public double asDouble(double fallback) {
            return isNull ? fallback : asDouble();
        }
        public boolean isDouble() {
            try{
                this.asDouble();
            } catch (Exception ignored) {
                return false;
            }
            return true;
        }

        public abstract String asString();
        public String asString(String fallback) {
            return isNull ? fallback : asString();
        }
        public boolean isString() {
            try{
                this.asString();
            } catch (Exception ignored) {
                return false;
            }
            return true;
        }

        public Identifier asIdentifier() {
            try {
                String value = asString();
                return new Identifier(ImmersiveRailroading.MODID, new Identifier(value).getPath());
            } catch (Exception e) {
                throw new IllegalArgumentException("Attempting to get identifier field "+this.name+" but field isn't identifier", e);
            }
        }
        public Identifier asIdentifier(Identifier fallback) {
            Identifier val = asIdentifier();
            return val != null && val.canLoad() ? val : fallback;
        }
        public boolean isIdentifier() {
            try{
                this.asIdentifier();
            } catch (Exception ignored) {
                return false;
            }
            return true;
        }
    }


    static DataBlock load(Identifier ident) throws IOException {
        return load(ident, false, null);
    }

    static DataBlock load(Identifier ident, DataBlock parameters) throws IOException {
        return load(ident, false, parameters);
    }

    static DataBlock load(Identifier ident, boolean last, DataBlock parameters) throws IOException {
        InputStream stream = last ? ident.getLastResourceStream() : ident.getResourceStream();

        if (parameters != null) {
            String input = IOUtils.toString(stream, Charset.defaultCharset());
            for (String key : parameters.getValueMap().keySet()) {
                input = input.replace(key, parameters.getValue(key).asString());
            }
            stream = IOUtils.toInputStream(input, Charset.defaultCharset());
        }


        if (ident.getPath().toLowerCase(Locale.ROOT).endsWith(".caml")) {
            return CAML.parse(stream);
        }
        if (!ident.getPath().toLowerCase(Locale.ROOT).endsWith(".json")) {
            ImmersiveRailroading.warn("Unexpected file extension '%s', trying JSON...", ident.toString());
        }
        return JSON.parse(stream);
    }
}
