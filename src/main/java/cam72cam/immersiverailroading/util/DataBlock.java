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

    protected String location;
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

    public DataBlock processLocation(String selfName) {        
        this.valueMap.forEach((s, value) -> value.loc = this.location + "/" + value.key);
        this.valuesMap.forEach((s, values) -> values.forEach(value -> value.loc = this.location + "/" + value.key));
        this.blockMap.forEach((s, block) -> block.processLocation(selfName + "/" + s));
        this.blocksMap.forEach((s, blocks) -> blocks.forEach(block -> block.processLocation(selfName + "/" + s)));
        return this;
    }

    public abstract static class Value {
        private final String key;
        private String loc;

        public Value(String key) {
            this.key = key;
        }
        public abstract @Nullable Boolean asBooleanNullable();
        public boolean asBoolean() {
            return Optional.ofNullable(asBooleanNullable())
                           .orElseThrow(() -> new IllegalArgumentException(String.format(DEFAULT_ERROR, key, loc, "boolean")));
        }
        public boolean asBoolean(boolean fallback) {
            return Optional.ofNullable(asBooleanNullable()).orElse(fallback);
        }

        public abstract @Nullable Integer asIntegerNullable();
        public int asInteger() {
            return Optional.ofNullable(asIntegerNullable())
                           .orElseThrow(() -> new IllegalArgumentException(String.format(DEFAULT_ERROR, key, loc, "integer")));
        }
        public int asInteger(int fallback) {
            return Optional.ofNullable(asIntegerNullable()).orElse(fallback);
        }

        public abstract @Nullable Float asFloatNullable();
        public float asFloat() {
            return Optional.ofNullable(asFloatNullable())
                           .orElseThrow(() -> new IllegalArgumentException(String.format(DEFAULT_ERROR, key, loc, "float")));
        }
        public float asFloat(float fallback) {
            return Optional.ofNullable(asFloatNullable()).orElse(fallback);
        }

        public abstract @Nullable Double asDoubleNullable();
        public double asDouble() {
            return Optional.ofNullable(asDoubleNullable())
                           .orElseThrow(() -> new IllegalArgumentException(String.format(DEFAULT_ERROR, key, loc, "double")));
        }
        public double asDouble(double fallback) {
            return Optional.ofNullable(asDoubleNullable()).orElse(fallback);
        }

        public abstract @Nullable String asStringNullable();
        public @Nonnull String asString() {
            return Optional.ofNullable(asStringNullable())
                           .orElseThrow(() -> new IllegalArgumentException(String.format(DEFAULT_ERROR, key, loc, "string")));
        }
        public @Nonnull String asString(String fallback) {
            return Optional.ofNullable(asStringNullable()).orElse(fallback);
        }

        public @Nullable Identifier asIdentifierNullable() {
            return Optional.ofNullable(asStringNullable())
                           .map(value -> new Identifier(ImmersiveRailroading.MODID, new Identifier(value).getPath()))
                           .orElse(null);
        }
        public @Nonnull Identifier asIdentifier() {
            return Optional.ofNullable(asStringNullable())
                           .map(value -> new Identifier(ImmersiveRailroading.MODID, new Identifier(value).getPath()))
                           .orElseThrow(() -> new IllegalArgumentException(String.format(DEFAULT_ERROR, key, loc, "location")));
        }
        public @Nonnull Identifier asIdentifier(Identifier fallback) {
            return Optional.ofNullable(asIdentifierNullable())
                           .filter(Identifier::canLoad)
                           .orElse(fallback);
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
