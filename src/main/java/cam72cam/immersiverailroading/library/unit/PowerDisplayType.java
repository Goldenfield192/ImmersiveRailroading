package cam72cam.immersiverailroading.library.unit;

public enum PowerDisplayType {
    horsepower,
    w,
    kw,
    ;

    public float convertFromWatt(float value) {
        switch (this) {
            case w:
                return value;
            case kw:
                return value / 1000f;
            case horsepower:
            default:
                return value * 1.341022f;
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
