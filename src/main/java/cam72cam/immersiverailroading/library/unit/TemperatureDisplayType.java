package cam72cam.immersiverailroading.library.unit;

public enum TemperatureDisplayType {
    celcius,
    kelvin,
    farenheit;

    public float convertFromCelcius(float value) {
        return switch (this) {
            case kelvin -> value + 273.15f;
            case farenheit -> (value * 9f / 5f) + 32f;
            default -> value;
        };
    }

    public String toUnitString() {
        return switch (this) {
            case kelvin -> "K";
            case farenheit -> "°F";
            default -> "°C";
        };
    }
}
