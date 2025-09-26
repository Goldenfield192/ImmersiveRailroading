package cam72cam.immersiverailroading.library;

public enum PowerDisplayType {
    horsepower,
    w,
    kw,
    ;

    public int convertFromWatt(int value) {
        switch (this) {
            case w:
                return value;
            case kw:
                return value / 1000;
            case horsepower:
            default:
                return (int) (value * 1.34102209);
        }
    }

    public String toUnitString() {
        switch (this) {
            case w:
                return "W";
            case kw:
                return "kW";
            case horsepower:
            default:
                return "hp";
        }
    }
}
