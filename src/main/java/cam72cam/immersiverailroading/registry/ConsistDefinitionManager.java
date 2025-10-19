package cam72cam.immersiverailroading.registry;

import cam72cam.mod.util.MinecraftFiles;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

//Client-sided
public class ConsistDefinitionManager {
    private static final Map<String, ConsistDefinition> allConsist;
    private static final Map<String, ConsistDefinition> validConsist;
    private static final File save;

    static {
        save = new File(MinecraftFiles.getConfigDir(), "immersiverailroading_consist.cfg");
        if (!save.exists()) {
            try {
                save.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        allConsist = new TreeMap<>(String::compareToIgnoreCase);
        validConsist = new TreeMap<>(String::compareToIgnoreCase);
    }

    public static void save() {
        try {
            // Structure:
            // multi_unit [name]
            // stock [stockName] [direction] [text variant] TODO optional: [cg:value]
            List<String> list = new LinkedList<>();
            list.add("//DO NOT TOUCH UNLESS YOU KNOW WHAT ARE YOU DOING");
            for (Map.Entry<String, ConsistDefinition> entry : allConsist.entrySet()) {
                list.add("consist "+entry.getKey().replace(" ", "^"));
                for (ConsistDefinition.Stock stock : entry.getValue().getStocks()) {
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
            allConsist.clear();
            validConsist.clear();
            ConsistDefinition.ConsistDefBuilder builder = null;
            for (String line : lines) {
                if (line.startsWith("consist")) {
                    String[] split = line.split(" ");
                    if(builder != null) {
                        ConsistDefinition built = builder.build();
                        allConsist.put(built.getName(), built);
                        if (built.valid()) {
                            validConsist.put(built.getName(), built);
                        }
                    }
                    builder = ConsistDefinition.ConsistDefBuilder.of(split[1].replace("^", " "));
                } else if (line.startsWith("stock")) {
                    String[] split = line.split(" ");
                    if (builder != null) {
                        builder.appendStock(split[1], ConsistDefinition.Direction.valueOf(split[2]), split[3], Collections.emptyMap());
                    }
                }
            }
            if(builder != null) {
                ConsistDefinition built = builder.build();
                allConsist.put(built.getName(), built);
                if (built.valid()) {
                    validConsist.put(built.getName(), built);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addConsist(ConsistDefinition build) {
        allConsist.put(build.getName(), build);
        save();
        load();
    }

    public static Map<String, ConsistDefinition> getValidConsists() {
        return validConsist;
    }

    public static ConsistDefinition getConsistDefinition(String name) {
        return allConsist.get(name);
    }
}
