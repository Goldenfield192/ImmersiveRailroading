package cam72cam.immersiverailroading.library.unit;

public enum ForceDisplayType {
    kn,
    n,
    lbf;

    public static final float lbfToNewton = 4.448221f;

    public float convertFromNewton(float value) {
        return switch (this) {
            case n -> value;
            case lbf -> value * 0.224809f;
            default -> value / 1000f;
        };
    }

    public String toUnitString() {
        return switch (this) {
            case n -> "N";
            case lbf -> "lbf";
            default -> "kN";
        };
    }
}
