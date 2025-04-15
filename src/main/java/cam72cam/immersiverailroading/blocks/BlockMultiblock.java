package cam72cam.immersiverailroading.blocks;

import cam72cam.immersiverailroading.tile.TileMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public class BlockMultiblock extends BaseEntityBlock {
	public BlockMultiblock() {
		super(Block.Properties.of().mapColor(MapColor.METAL)
							  .sound(SoundType.METAL)
							  .strength(0.2f,1)
							  .dynamicShape());
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new TileMultiblock(blockPos, blockState);
	}
}
