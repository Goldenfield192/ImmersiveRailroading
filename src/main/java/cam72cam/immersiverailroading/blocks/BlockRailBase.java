package cam72cam.immersiverailroading.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public abstract class BlockRailBase extends BaseEntityBlock {
	public BlockRailBase() {
		super(Block.Properties.of().mapColor(MapColor.METAL)
							  .sound(SoundType.METAL)
							  .strength(1,5)
							  .dynamicShape());
	}

	@Override
	public abstract BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState);
}
