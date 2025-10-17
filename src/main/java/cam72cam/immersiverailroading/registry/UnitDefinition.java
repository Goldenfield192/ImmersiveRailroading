package cam72cam.immersiverailroading.registry;

import javax.annotation.Nullable;
import java.util.*;

public class UnitDefinition {
    public String getName() {
        return name;
    }

    private final String name;
    private final List<Stock> stocks;

    public UnitDefinition(String name, List<Stock> stocks) {
        this.name = name;
        this.stocks = stocks;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public static class UnitDefBuilder {
        private String name;
        private List<Stock> stocks;

        private boolean isBuilt = false;

        private UnitDefBuilder(){}

        public static UnitDefBuilder of(String name) {
            UnitDefBuilder builder = new UnitDefBuilder();
            builder.name = name;
            builder.stocks = new ArrayList<>();
            return builder;
        }

        public void appendStock(String defID, Direction direction, String tex, Map<String, Float> defaultCGs){
            if(isBuilt){
                throw new UnsupportedOperationException();
            }
            this.appendStock(new Stock(DefinitionManager.getDefinition(defID), direction, tex, defaultCGs));
        }

        public void appendStock(Stock stock){
            if(isBuilt){
                throw new UnsupportedOperationException();
            }

            if (stock.texture == null) {
                if(!stock.definition.textureNames.keySet().isEmpty()){
                    stock.texture = stock.definition.textureNames.keySet().stream().findFirst().get();
                }
            } else if (!stock.definition.textureNames.containsKey(stock.texture)) {
                stock.texture = stock.definition.textureNames.keySet().stream().findFirst().get();
            }
            if(stock.controlGroup == null) {
                stock.controlGroup = new LinkedHashMap<>();
            }
            if(stock.direction == null){
                stock.direction = Direction.FORWARD;
            }

            stocks.add(stock);
        }

        public UnitDefinition build() {
            if(isBuilt){
                throw new UnsupportedOperationException();
            }
            isBuilt = true;
            return new UnitDefinition(name, stocks);
        }
    }

    public static class Stock {
        public EntityRollingStockDefinition definition;
        public Direction direction;
        public String texture;
        public Map<String, Float> controlGroup;

        public Stock() {
        }

        public Stock(EntityRollingStockDefinition stock, Direction direction, @Nullable String texture, Map<String, Float> controlGroup) {
            this.definition = stock;
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
