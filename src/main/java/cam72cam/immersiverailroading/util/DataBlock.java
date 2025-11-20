package cam72cam.immersiverailroading.util;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.mod.resource.Identifier;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Standard data object, representing JSON or CAML file
 */
@SuppressWarnings("unused")
public class DataBlock {
    public static final DataBlock EMPTY_BLOCK = new DataBlock(Collections.emptyMap(),
                                                              Collections.emptyMap(),
                                                              Collections.emptyMap(),
                                                              Collections.emptyMap());

    private static final String DEFAULT_ERROR = "Missing property \"%s\" in \"%s\" (Expected type: %s)";

    protected DataBlock parent;
    protected String name;
    protected final Map<String, Value> valueMap;
    protected final Map<String, List<Value>> valuesMap;
    protected final Map<String, DataBlock> blockMap;
    protected final Map<String, List<DataBlock>> blocksMap;

    DataBlock(Map<String, Value> valueMap, Map<String, List<Value>> valuesMap,
              Map<String, DataBlock> blockMap, Map<String, List<DataBlock>> blocksMap) {
        this.valueMap = valueMap;
        this.valuesMap = valuesMap;
        this.blockMap = blockMap;
        this.blocksMap = blocksMap;
    }

    public DataBlock getBlock(String key) {
        return blockMap.getOrDefault(key, EMPTY_BLOCK);
    }

    public List<DataBlock> getBlocks(String key) {
        return blocksMap.getOrDefault(key, Collections.singletonList(EMPTY_BLOCK));
    }

    public Value getValue(String key) {
        return valueMap.getOrDefault(key, Value.ofNull(key));
    }

    public List<Value> getValues(String key) {
        return valuesMap.getOrDefault(key, Collections.singletonList(Value.ofNull(key)));
    }

    public Map<String, DataBlock> getBlockMap() {
        return blockMap;
    }

    public Map<String, List<DataBlock>> getBlocksMap() {
        return blocksMap;
    }

    public Map<String, Value> getValueMap() {
        return valueMap;
    }

    public Map<String, List<Value>> getValuesMap() {
        return valuesMap;
    }

    private String getPath() {
        if (this.parent == null) {
            return "root";
        }
        return parent.getPath() + "/" + this.name;
    }

    public void processParent() {
        this.valueMap.forEach((s, value) -> value.setParent(this));
        this.valuesMap.forEach((s, value) -> value.forEach(value1 -> value1.setParent(this)));
        this.blockMap.forEach((s, block) -> {
            block.parent = this;
            block.processParent();
        });
        this.blocksMap.forEach((s, blocks) ->
            blocks.forEach(block -> {
                block.parent = this;
                block.processParent();
        }));
    }

    public abstract static class Value {
        private DataBlock parent;
        private final String key;

        public Value(String key) {
            this.key = key;
        }

        public void setParent(DataBlock parent) {
            this.parent = parent;
        }

        public String getParentPath() {
            if (this.parent == null) {
                return "root";
            }
            return parent.getPath();
        }

        public abstract @Nullable Boolean asBooleanNullable();
        public boolean asBoolean() {
            Boolean b = asBooleanNullable();
            if (b == null) {
                throw new IllegalArgumentException(String.format(DEFAULT_ERROR, key, getParentPath(), "boolean"));
            }
            return b;
        }
        public boolean asBoolean(boolean fallback) {
            Boolean val = asBooleanNullable();
            return val != null ? val : fallback;
        }

        public abstract @Nullable Integer asIntegerNullable();
        public int asInteger() {
            Integer i = asIntegerNullable();
            if (i == null) {
                throw new IllegalArgumentException(String.format(DEFAULT_ERROR, key, getParentPath(), "integer"));
            }
            return i;
        }
        public int asInteger(int fallback) {
            Integer val = asIntegerNullable();
            return val != null ? val : fallback;
        }

        public abstract @Nullable Float asFloatNullable();
        public float asFloat() {
            Float f = asFloatNullable();
            if (f == null) {
                throw new IllegalArgumentException(String.format(DEFAULT_ERROR, key, getParentPath(), "float"));
            }
            return f;
        }
        public float asFloat(float fallback) {
            Float val = asFloatNullable();
            return val != null ? val : fallback;
        }

        public abstract @Nullable Double asDoubleNullable();
        public double asDouble() {
            Double d = asDoubleNullable();
            if (d == null) {
                throw new IllegalArgumentException(String.format(DEFAULT_ERROR, key, getParentPath(), "double"));
            }
            return d;
        }
        public double asDouble(double fallback) {
            Double val = asDoubleNullable();
            return val != null ? val : fallback;
        }

        public abstract @Nullable String asStringNullable();
        public @Nonnull String asString() {
            String s = asStringNullable();
            if (s == null) {
                throw new IllegalArgumentException(String.format(DEFAULT_ERROR, key, getParentPath(), "string"));
            }
            return s;
        }
        public @Nonnull String asString(String fallback) {
            String val = asStringNullable();
            return val != null ? val : fallback;
        }

        public @Nullable Identifier asIdentifierNullable() {
            String value = asStringNullable();
            return value != null ? new Identifier(ImmersiveRailroading.MODID, new Identifier(value).getPath()) : null;
        }
        public @Nonnull Identifier asIdentifier() {
            String value = asStringNullable();
            if (value == null) {
                throw new IllegalArgumentException(String.format(DEFAULT_ERROR, key, getParentPath(), "location"));
            }
            return new Identifier(ImmersiveRailroading.MODID, new Identifier(value).getPath());
        }
        public @Nonnull Identifier asIdentifier(Identifier fallback) {
            Identifier val = asIdentifierNullable();
            return val != null && val.canLoad() ? val : fallback;
        }

        public static Value ofNull(String key) {
            return new Value(key) {
                @Override
                public Boolean asBooleanNullable() {
                    return null;
                }

                @Override
                public Integer asIntegerNullable() {
                    return null;
                }

                @Override
                public Float asFloatNullable() {
                    return null;
                }

                @Override
                public Double asDoubleNullable() {
                    return null;
                }

                @Override
                public String asStringNullable() {
                    return null;
                }
            };
        }
    }

    public static DataBlock load(Identifier ident) throws IOException {
        return load(ident, null);
    }

    public static DataBlock load(Identifier ident, DataBlock parameters) throws IOException {
        return load(ident, parameters, false);
    }

    public static DataBlock load(Identifier ident, DataBlock parameters, boolean last) throws IOException {
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

    public DataBlock merge(DataBlock other) {
        DataBlock result = new DataBlock(new LinkedHashMap<>(this.getValueMap()), new LinkedHashMap<>(this.getValuesMap()),
                                         new LinkedHashMap<>(this.getBlockMap()), new LinkedHashMap<>(this.getBlocksMap()));

        result.valueMap.putAll(other.getValueMap());
        other.getValuesMap().forEach((key, values) -> {
            if (result.valuesMap.containsKey(key)) {
                // Merge into new list
                List<Value> tmp = new ArrayList<>(result.valuesMap.get(key));
                tmp.addAll(values);
                values = tmp;
            }
            result.valuesMap.put(key, values);
        });
        other.getBlockMap().forEach((key, block) -> {
            if (result.blockMap.containsKey(key)) {
                block = result.blockMap.get(key).merge(block);
            }
            result.blockMap.put(key, block);
        });
        other.getBlocksMap().forEach((key, blocks) -> {
            if (result.blocksMap.containsKey(key)) {
                List<DataBlock> tmp = new ArrayList<>(result.blocksMap.get(key));
                tmp.addAll(blocks);
                blocks = tmp;
            }
            result.blocksMap.put(key, blocks);
        });
        return result;
    }
}
