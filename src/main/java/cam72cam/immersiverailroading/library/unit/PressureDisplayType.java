package cam72cam.immersiverailroading.library.unit;

public enum PressureDisplayType {
    psi,
    bar,
    kpa;

    public float convertFromPSI(float value) {
        switch (this) {
            case bar: return value * 0.0689476f;
            case kpa: return value * 6.89476f;
            default: return value;
        }
    }

    public String toUnitString() {
        switch (this) {
            case bar:
                return "bar";
            case kpa:
                return "kPa";
            case psi:
            default:
                return "psi";
        }
    }
}
