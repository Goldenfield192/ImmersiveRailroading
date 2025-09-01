package cam72cam.immersiverailroading.library;

public enum TrackModelPart {
    RAIL_LEFT,
    RAIL_RIGHT,
    RAIL_BASE,
    TABLE;

    public boolean is(String str) {
        return str.contains(this.name());
    }
}
