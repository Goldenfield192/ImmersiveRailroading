package cam72cam.immersiverailroading.blocks;


import cam72cam.immersiverailroading.tile.TileRailGag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockRailGag extends BlockRailBase {
	public BlockRailGag() {
		super();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new TileRailGag(blockPos, blockState);
	}
}