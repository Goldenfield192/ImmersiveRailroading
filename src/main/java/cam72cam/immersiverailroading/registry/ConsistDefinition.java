package cam72cam.immersiverailroading.registry;

import javax.annotation.Nullable;
import java.util.*;

//Client only
public class ConsistDefinition {
    private final String name;
    private final List<Stock> stocks;
    private final boolean editable;

    public ConsistDefinition(String name, List<Stock> stocks) {
        this.name = name;
        this.stocks = stocks;
        this.editable = true; //TODO Pack defined consist
    }

    public String getName() {
        return name;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public boolean valid() {
        return stocks.stream().noneMatch(stock -> stock.error);
    }

    public static class ConsistDefBuilder {
        private String name;
        private List<Stock> stocks;

        private boolean isBuilt = false;

        private ConsistDefBuilder(){}

        public static ConsistDefBuilder of(String name) {
            ConsistDefBuilder builder = new ConsistDefBuilder();
            builder.name = name;
            builder.stocks = new ArrayList<>();
            return builder;
        }

        public void appendStock(String defID, Direction direction, String tex, Map<String, Float> defaultCGs){
            if(isBuilt){
                throw new UnsupportedOperationException();
            }
            this.appendStock(new Stock(defID, direction, tex, defaultCGs));
        }

        public void appendStock(Stock stock){
            if(isBuilt){
                throw new UnsupportedOperationException();
            }

            if(!stock.error){
                if (stock.texture == null) {
                    if (!stock.definition.textureNames.keySet().isEmpty()) {
                        stock.texture = stock.definition.textureNames.keySet().stream().findFirst().get();
                    }
                } else if (!stock.definition.textureNames.containsKey(stock.texture)) {
                    stock.texture = stock.definition.textureNames.keySet().stream().findFirst().orElse(null);
                }
            }
            if(stock.controlGroup == null) {
                stock.controlGroup = new LinkedHashMap<>();
            }
            if(stock.direction == null){
                stock.direction = Direction.FORWARD;
            }

            stocks.add(stock);
        }

        public ConsistDefinition build() {
            if(isBuilt){
                throw new UnsupportedOperationException();
            }
            isBuilt = true;
            stocks.forEach(stock -> {
                if (stock.defID == null && stock.definition != null) {
                    stock.defID = stock.definition.defID;
                }
            });
            return new ConsistDefinition(name, stocks);
        }
    }

    public static class Stock {
        public String defID;
        public EntityRollingStockDefinition definition;
        public Direction direction;
        public String texture;
        public Map<String, Float> controlGroup;

        public boolean error = false;

        public Stock() {
        }

        public Stock(String defID, Direction direction, @Nullable String texture, Map<String, Float> controlGroup) {
            this.defID = defID;
            //Used to preserve unit def in case used stock is uninstalled
            if (DefinitionManager.getDefinition(defID) == null) {
                error = true;
            } else {
                this.definition = DefinitionManager.getDefinition(defID);
            }
            this.direction = direction;
            this.texture = texture;
            this.controlGroup = controlGroup;
        }
    }

    public enum Direction{
        FORWARD,
        REVERSE,
        RANDOM;

        private static final Random random = new Random();

        public boolean shouldFlip() {
            switch (this) {
                case REVERSE:
                    return true;
                case RANDOM:
                    return random.nextBoolean();
                case FORWARD:
                default:
                    return false;
            }
        }
    }
}
