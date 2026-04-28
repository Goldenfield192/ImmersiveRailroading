package cam72cam.immersiverailroading.library.unit;

public enum SpeedDisplayType {
	kmh,
	ms,
	mph;

	public double convertFromKmh(double value) {
        return switch (this) {
            case ms -> value / 3.6;
            case mph -> value * 0.621371;
            default -> value;
        };
	}

	public String toUnitString() {
        return switch (this) {
            case ms -> "m/s";
            case mph -> "mph";
            default -> "km/h";
        };
	}
}
