package cam72cam.immersiverailroading.library;

import cam72cam.mod.render.Color;
import cam72cam.mod.resource.Identifier;

public enum Augment {
	SPEED_RETARDER("immersiverailroading:textures/augment/speed_retarder.png"),
	WATER_TROUGH("immersiverailroading:textures/augment/fluid_loader.png"), //NOT PLACEABLE
	LOCO_CONTROL("immersiverailroading:textures/augment/locomotive_controller.png"),
	ITEM_LOADER("immersiverailroading:textures/augment/item_loader.png"),
	ITEM_UNLOADER("immersiverailroading:textures/augment/item_unloader.png"),
	FLUID_LOADER("immersiverailroading:textures/augment/fluid_loader.png"),
	FLUID_UNLOADER("immersiverailroading:textures/augment/fluid_unloader.png"),
	DETECTOR("immersiverailroading:textures/augment/detector.png"),
	COUPLER("immersiverailroading:textures/augment/coupler.png"),
	ACTUATOR("immersiverailroading:textures/augment/door_actuator.png"),
	;

	public final Identifier texture;

    Augment(String s) {
        texture = new Identifier(s);
    }

    public Color color() {
		switch (this) {
		case DETECTOR:
			return Color.RED;
		case FLUID_LOADER:
			return Color.BLUE;
		case FLUID_UNLOADER:
			return Color.LIGHT_BLUE;
		case ITEM_LOADER:
			return Color.GREEN;
		case ITEM_UNLOADER:
			return Color.LIME;
		case LOCO_CONTROL:
			return Color.BLACK;
		case SPEED_RETARDER:
			return Color.GRAY;
		case WATER_TROUGH:
			return Color.CYAN;
		case COUPLER:
			return Color.ORANGE;
		case ACTUATOR:
			return Color.SILVER;
		}
		return Color.WHITE;
	}
}
