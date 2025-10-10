package cam72cam.immersiverailroading.registry;

import cam72cam.immersiverailroading.util.DataBlock;

import javax.annotation.Nullable;
import java.util.*;

public class UnitDefinition {
    private final String name;
    private final List<String> description;
    private final List<Stock> stocks;

    public UnitDefinition(String name, List<String> description, List<Stock> stocks) {
        this.name = name;
        this.description = description;
        this.stocks = stocks;
    }

    public List<String> getTooltip() {
        return description;
    }

    public static class UnitDefBuilder {
        private String name;
        private List<String> description;
        private List<Stock> stocks;

        private boolean isBuilt = false;

        private UnitDefBuilder(){}

        public static UnitDefBuilder of(String name, String description) {
            return of(name, Collections.singletonList(description));
        }

        public static UnitDefBuilder of(String name, List<String> description) {
            UnitDefBuilder builder = new UnitDefBuilder();
            builder.name = name;
            builder.description = description;
            builder.stocks = new ArrayList<>();
            return builder;
        }

        public void appendStock(String defID, Direction direction, String tex, Map<String, Float> defaultCGs){
            if(isBuilt){
                throw new UnsupportedOperationException();
            }
            stocks.add(new Stock(DefinitionManager.getDefinition(defID), direction, tex, defaultCGs));
        }

        public UnitDefinition build() {
            if(isBuilt){
                throw new UnsupportedOperationException();
            }
            isBuilt = true;
            return new UnitDefinition(name, description, stocks);
        }

    }

    public static class Stock {
        public EntityRollingStockDefinition definition;
        public Direction direction;
        public String texture;
        public Map<String, Float> controlGroup;

        public Stock(EntityRollingStockDefinition stock, Direction direction, @Nullable String texture, Map<String, Float> controlGroup) {
            this.definition = stock;
            this.direction = direction;
            this.texture = texture;
            this.controlGroup = controlGroup;
        }
    }

    public enum Direction{
        DEFAULT,
        FLIPPED,
        RANDOM;

        public static Direction parse(@Nullable DataBlock.Value val) {
            if (val == null) {
                return DEFAULT;
            }

            String str = val.asString("default").toUpperCase();

            Direction dir;
            try {
                dir = Direction.valueOf(str);
            } catch (IllegalArgumentException e) {
                return DEFAULT;
            }

            return dir;
        }

        public boolean getDirection() {
            switch (this) {
                case FLIPPED:
                    return true;
                case RANDOM:
                    Random random = new Random();
                    return random.nextBoolean();
                case DEFAULT:
                default:
                    return false;
            }
        }
    }
}
