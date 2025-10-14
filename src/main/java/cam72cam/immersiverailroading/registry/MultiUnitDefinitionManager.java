package cam72cam.immersiverailroading.registry;

import cam72cam.mod.util.MinecraftFiles;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

//Client-sided
public class MultiUnitDefinitionManager {
    public static Map<String, UnitDefinition> units;

    public static void initMultiUnit() {
        units = new LinkedHashMap<>();
        try {
            File file = new File(MinecraftFiles.getConfigDir(), "immersiverailroading_multi_unit.cfg");
            if (!file.exists()) {
                file.mkdir();
            }
            // Start with # -> skip
            // Structure: def [name] [description] [number]
            try (Scanner reader = new Scanner(file)) {
                String line;
                while ((line = reader.nextLine()) != null) {
                    if(line.isEmpty()||line.startsWith("#")) {
                        continue;
                    }
                    if(line.startsWith("def")) {
                        String[] prop = line.split(" ");
                        String name = prop[1];
                        String description = prop[2];
                        UnitDefinition.UnitDefBuilder builder = UnitDefinition.UnitDefBuilder.of(name, description);
                        int count = Integer.parseInt(prop[3]);
                        for (int i = 0; i < count; i++) {
                            line = reader.nextLine();
                            String[] stock = line.split(" ");
                            String defID = stock[0];
                            UnitDefinition.Direction direction = UnitDefinition.Direction.valueOf(stock[1]);
                            if (stock.length == 2) {
                                builder.appendStock(defID, direction, null, Collections.emptyMap());
                            } else if (stock.length == 3) {
                                String tex = stock[2];
                                builder.appendStock(defID, direction, tex, Collections.emptyMap());
                            } else {
                                String tex = stock[2];
                                Map<String, Float> defaultCGs = new LinkedHashMap<>();
                                for (int j = 3; j < stock.length; j++) {
                                    String[] strings = stock[j].split(":");
                                    defaultCGs.put(strings[0], Float.valueOf(strings[1]));
                                }
                                builder.appendStock(defID, direction, tex, defaultCGs);
                            }
                        }
                        units.put(name, builder.build());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
