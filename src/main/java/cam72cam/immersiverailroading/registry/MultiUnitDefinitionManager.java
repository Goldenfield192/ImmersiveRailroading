package cam72cam.immersiverailroading.registry;

import cam72cam.mod.util.MinecraftFiles;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

//Client-sided
public class MultiUnitDefinitionManager {
    private static final Map<String, UnitDefinition> allUnits;
    private static final Map<String, UnitDefinition> validUnits;
    private static final File save;

    static {
        save = new File(MinecraftFiles.getConfigDir(), "immersiverailroading.multi_unit.cfg");
        if (!save.exists()) {
            try {
                save.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        allUnits = new TreeMap<>(String::compareToIgnoreCase);
        validUnits = new TreeMap<>(String::compareToIgnoreCase);
    }

    public static void save() {
        try {
            // Structure:
            // multi_unit [name]
            // stock [stockName] [direction] [text variant] TODO optional: [cg:value]
            List<String> list = new LinkedList<>();
            list.add("//DO NOT TOUCH UNLESS YOU KNOW WHAT ARE YOU DOING");
            for (Map.Entry<String, UnitDefinition> entry : allUnits.entrySet()) {
                list.add("multi_unit "+entry.getKey().replace(" ", "^"));
                for (UnitDefinition.Stock stock : entry.getValue().getStocks()) {
                    String texture = (stock.texture == null||stock.texture.isEmpty()) ? "default" : stock.texture;
                    list.add("stock "+stock.defID+" "+stock.direction+" "+texture);
                }
            }
            Files.write(save.toPath(), list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void load() {
        try {
            // Structure:
            // multi_unit [name]
            // stock [stockName] [direction] [text variant] TODO optional: [cg:value]
            List<String> lines = Files.readAllLines(save.toPath());
            allUnits.clear();
            validUnits.clear();
            UnitDefinition.UnitDefBuilder builder = null;
            for (String line : lines) {
                if (line.startsWith("multi_unit")) {
                    String[] split = line.split(" ");
                    if(builder != null) {
                        UnitDefinition built = builder.build();
                        allUnits.put(built.getName(), built);
                        if (built.valid()) {
                            validUnits.put(built.getName(), built);
                        }
                    }
                    builder = UnitDefinition.UnitDefBuilder.of(split[1].replace("^", " "));
                } else if (line.startsWith("stock")) {
                    String[] split = line.split(" ");
                    if (builder != null) {
                        builder.appendStock(split[1], UnitDefinition.Direction.valueOf(split[2]), split[3], Collections.emptyMap());
                    }
                }
            }
            if(builder != null) {
                UnitDefinition built = builder.build();
                allUnits.put(built.getName(), built);
                if (built.valid()) {
                    validUnits.put(built.getName(), built);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addUnit(UnitDefinition build) {
        allUnits.put(build.getName(), build);
        save();
        load();
    }

    public static Map<String, UnitDefinition> getValidUnits() {
        return validUnits;
    }

    public static UnitDefinition getUnitDef(String name) {
        return allUnits.get(name);
    }
}
