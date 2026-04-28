package cam72cam.immersiverailroading.library;

import cam72cam.mod.text.TextUtil;

import java.util.Locale;

public enum TrackItems {
	STRAIGHT(0),
	CROSSING(1),
	SLOPE(2),
	TURN(3),
	SWITCH(4),
	TURNTABLE(5),
	CUSTOM(7),
	TRANSFERTABLE(6);

	private final int order;

	TrackItems(int order){
		this.order = order;
	}
	
	@Override
	public String toString() {
	    return TextUtil.translate("track.immersiverailroading:class." + super.toString().toLowerCase(Locale.ROOT));
	}

	public boolean hasQuarters() {
        return switch (this) {
            case TURN, SWITCH -> true;
            default -> false;
        };
	}

	public boolean hasCurvosity() {
        return switch (this) {
            case SWITCH, CUSTOM -> true;
            default -> false;
        };
	}

	public boolean hasSmoothing() {
        return switch (this) {
            case SLOPE, TURN, SWITCH, CUSTOM -> true;
            default -> false;
        };
	}

	public boolean hasDirection() {
        return switch (this) {
            case TURN, SWITCH -> true;
            default -> false;
        };
	}

	public boolean isTable() {
        return switch (this) {
            case TURNTABLE, TRANSFERTABLE -> true;
            default -> false;
        };
	}

	public int getOrder() {
		return this.order;
	}
}