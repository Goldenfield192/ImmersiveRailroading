package cam72cam.immersiverailroading.tile;

import cam72cam.immersiverailroading.IRBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileRailGag extends TileRailBase {
	public TileRailGag(BlockPos p_155229_, BlockState p_155230_) {
		super(IRBlocks.TILE_RAIL_GAG.get(), p_155229_, p_155230_);
	}

	public void setFlexible(boolean flexible) {
		this.flexible = flexible;
	}

	public double getRenderDistance() {
		return 0;
	}
}