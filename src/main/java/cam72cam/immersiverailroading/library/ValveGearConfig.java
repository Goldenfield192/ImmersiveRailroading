package cam72cam.immersiverailroading.library;

import cam72cam.immersiverailroading.util.DataBlock;
import cam72cam.mod.resource.Identifier;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public record ValveGearConfig(ValveGearType type, Map<Float, Identifier> custom) {
	public enum ValveGearType {
		CONNECTING,
		STEPHENSON,
		WALSCHAERTS,
		HIDDEN,
		// TODO
		SHAY,
		CLIMAX,
		CUSTOM,
		;

		public static ValveGearType from(String valveGear) {
			if (valveGear == null) {
				return null;
			}
			return switch (valveGear) {
				case "TRI_WALSCHAERTS", "GARRAT", "MALLET_WALSCHAERTS" -> WALSCHAERTS;
				case "T1" -> STEPHENSON;
				default -> ValveGearType.valueOf(valveGear);
			};
		}
	}

	public static ValveGearConfig get(DataBlock def, String key) {
		DataBlock block = def.getBlock(key);
		if (block != null) {
			DataBlock animatrix = block.getBlock("animatrix");
			Map<Float, Identifier> custom = new HashMap<>();
			animatrix.getValueMap().forEach(
					(percent, anim) -> custom.put(Float.parseFloat(percent), anim.asIdentifier()));
			return new ValveGearConfig(ValveGearType.CUSTOM, custom);
		}
		String name = def.getValue(key).asString();
		if (name != null) {
			return new ValveGearConfig(ValveGearType.from(name.toUpperCase(Locale.ROOT)), null);
		}

		return null;
	}
}