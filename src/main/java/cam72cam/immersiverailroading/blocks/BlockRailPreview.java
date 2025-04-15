package cam72cam.immersiverailroading.blocks;

import cam72cam.immersiverailroading.tile.TileRailPreview;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class BlockRailPreview extends BaseEntityBlock {
	public BlockRailPreview() {
		super(Block.Properties.of().mapColor(MapColor.WOOL)
							  .sound(SoundType.WOOL)
							  .strength(0.2f,2000)
							  .dynamicShape());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new TileRailPreview(blockPos, blockState);
	}
}
