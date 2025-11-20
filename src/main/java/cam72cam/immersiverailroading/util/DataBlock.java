package cam72cam.immersiverailroading.util;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.mod.resource.Identifier;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

@SuppressWarnings("unused")
public class DataBlock {
    public static final DataBlock EMPTY_BLOCK = new DataBlock(null,
                                                              Collections.emptyMap(),
                                                              Collections.emptyMap(),
                                                              Collections.emptyMap(),
                                                              Collections.emptyMap());

    protected final DataBlock parent;
    protected final Map<String, Value> valueMap;
    protected final Map<String, List<Value>> valuesMap;
    protected final Map<String, DataBlock> blockMap;
    protected final Map<String, List<DataBlock>> blocksMap;

    DataBlock(DataBlock parent, Map<String, Value> valueMap, Map<String, List<Value>> valuesMap,
                     Map<String, DataBlock> blockMap, Map<String, List<DataBlock>> blocksMap) {
        this.parent = parent;
        this.valueMap = valueMap;
        this.valuesMap = valuesMap;
        this.blockMap = blockMap;
        this.blocksMap = blocksMap;
    }

    public DataBlock getBlock(String key) {
        return blockMap.get(key);
    }

    public Map<String, DataBlock> getBlockMap() {
        return blockMap;
    }

    public List<DataBlock> getBlocks(String key) {
        return blocksMap.get(key);
    }

    public Map<String, List<DataBlock>> getBlocksMap() {
        return blocksMap;
    }
    
    public Value getValue(String key) {
        return valueMap.getOrDefault(key, Value.NULL);
    }
    
    public Map<String, Value> getValueMap() {
        return valueMap;
    }
    
    public List<Value> getValues(String key) {
        return valuesMap.get(key);
    }

    public Map<String, List<Value>> getValuesMap() {
        return valuesMap;
    }

    public abstract static class Value {
        private DataBlock parent;
        private String propertyName;

        public abstract Boolean asBoolean();
        public boolean asBoolean(boolean fallback) {
            Boolean val = asBoolean();
            return val != null ? val : fallback;
        }

        public abstract Integer asInteger();
        public int asInteger(int fallback) {
            Integer val = asInteger();
            return val != null ? val : fallback;
        }

        public abstract Float asFloat();
        public float asFloat(float fallback) {
            Float val = asFloat();
            return val != null ? val : fallback;
        }

        public abstract Double asDouble();
        public double asDouble(double fallback) {
            Double val = asDouble();
            return val != null ? val : fallback;
        }

        public abstract String asString();
        public String asString(String fallback) {
            String val = asString();
            return val != null ? val : fallback;
        }

        public Identifier asIdentifier() {
            String value = asString();
            return value != null ? new Identifier(ImmersiveRailroading.MODID, new Identifier(value).getPath()) : null;
        }
        public Identifier asIdentifier(Identifier fallback) {
            Identifier val = asIdentifier();
            return val != null && val.canLoad() ? val : fallback;
        }

        public static final Value NULL = new Value() {
            @Override
            public Boolean asBoolean() {
                return null;
            }

            @Override
            public Integer asInteger() {
                return null;
            }

            @Override
            public Float asFloat() {
                return null;
            }

            @Override
            public Double asDouble() {
                return null;
            }

            @Override
            public String asString() {
                return null;
            }
        };
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
        DataBlock result = new DataBlock(this.parent,
              new LinkedHashMap<>(this.getValueMap()),
              new LinkedHashMap<>(this.getValuesMap()),
              new LinkedHashMap<>(this.getBlockMap()),
              new LinkedHashMap<>(this.getBlocksMap()));

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
