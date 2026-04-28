package cam72cam.immersiverailroading.library.unit;

public enum PowerDisplayType {
    kw,
    w,
    horsepower,
    ps;

    public static final float kWToHp = 1.34102f;
    public static final float hpToKW = 0.745701f;
    public static final float wToHp = 0.00134102f;
    public static final float PSToKW = 0.735498f;
    public static final float wToPS = 0.00135962f;

    public float convertFromWatt(float value) {
        return switch (this) {
            case w -> value;
            case horsepower -> value * wToHp;
            case ps -> value * wToPS;
            default -> value / 1000f;
        };
    }

    public String toUnitString() {
        return switch (this) {
            case w -> "W";
            case kw -> "kW";
            case ps -> "PS";
            default -> "hp";
        };
    }
}
