package cam72cam.immersiverailroading.blocks;

import cam72cam.immersiverailroading.tile.TileRail;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockRail extends BlockRailBase {
	public BlockRail() {
		super();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new TileRail(blockPos, blockState);
	}
}
