package cam72cam.immersiverailroading.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MergedBlocks extends DataBlock {
    public MergedBlocks(DataBlock base, DataBlock override) {
        super(null,
              new LinkedHashMap<>(base.getValueMap()),
              new LinkedHashMap<>(base.getValuesMap()),
              new LinkedHashMap<>(base.getBlockMap()),
              new LinkedHashMap<>(base.getBlocksMap()));

        valueMap.putAll(override.getValueMap());
        override.getValuesMap().forEach((key, values) -> {
            if (valuesMap.containsKey(key)) {
                // Merge into new list
                List<Value> tmp = new ArrayList<>(valuesMap.get(key));
                tmp.addAll(values);
                values = tmp;
            }
            valuesMap.put(key, values);
        });
        override.getBlockMap().forEach((key, block) -> {
            if (blockMap.containsKey(key)) {
                block = new MergedBlocks(blockMap.get(key), block);
            }
            blockMap.put(key, block);
        });
        override.getBlocksMap().forEach((key, blocks) -> {
            if (blocksMap.containsKey(key)) {
                List<DataBlock> tmp = new ArrayList<>(blocksMap.get(key));
                tmp.addAll(blocks);
                blocks = tmp;
            }
            blocksMap.put(key, blocks);
        });
    }
}
