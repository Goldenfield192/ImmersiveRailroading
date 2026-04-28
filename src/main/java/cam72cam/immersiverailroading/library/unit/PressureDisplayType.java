package cam72cam.immersiverailroading.library.unit;

public enum PressureDisplayType {
    // 1 bar = 100 kPa
    bar,
    kpa,
    psi;

    public static final float psiToKPa = 6.89474f;
    public static final float kPaToPsi = 0.145037f;
    public static final float psiToBar = 0.0689474f;
    public static final float BarToPsi = 14.5037f;

    public float convertFromPSI(float value) {
        return switch (this) {
            case kpa -> value * psiToKPa;
            case psi -> value;
            default -> value * psiToBar;
        };
    }

    public String toUnitString() {
        return switch (this) {
            case kpa -> "kPa";
            case psi -> "psi";
            default -> "bar";
        };
    }
}
