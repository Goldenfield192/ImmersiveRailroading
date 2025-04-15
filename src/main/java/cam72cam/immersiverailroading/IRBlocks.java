package cam72cam.immersiverailroading;

import cam72cam.immersiverailroading.blocks.BlockMultiblock;
import cam72cam.immersiverailroading.blocks.BlockRail;
import cam72cam.immersiverailroading.blocks.BlockRailGag;
import cam72cam.immersiverailroading.blocks.BlockRailPreview;
import cam72cam.immersiverailroading.tile.TileMultiblock;
import cam72cam.immersiverailroading.tile.TileRail;
import cam72cam.immersiverailroading.tile.TileRailGag;
import cam72cam.immersiverailroading.tile.TileRailPreview;
import com.mojang.datafixers.DSL;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class IRBlocks {
	//WAITING FOR REMOVAL
	public static final BlockRailPreview BLOCK_RAIL_PREVIEW = new BlockRailPreview();
	public static final BlockRailGag BLOCK_RAIL_GAG = new BlockRailGag();
	public static final BlockRail BLOCK_RAIL = new BlockRail();
	public static BlockMultiblock BLOCK_MULTIBLOCK = new BlockMultiblock();
	//WAIT FOR REMOVAL

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ImmersiveRailroading.MODID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ImmersiveRailroading.MODID);

	public static final RegistryObject<Block> MULTIBLOCK =
			BLOCKS.register("multiblock", BlockMultiblock::new);
	public static final RegistryObject<BlockEntityType<TileMultiblock>> TILE_MULTIBLOCK =
			BLOCK_ENTITIES.register("multiblock", () -> BlockEntityType.Builder
					.of(TileMultiblock::new, MULTIBLOCK.get())
					.build(DSL.remainderType()));

	public static final RegistryObject<Block> RAIL_PREVIEW =
			BLOCKS.register("block_rail_preview", BlockRailPreview::new);
	public static final RegistryObject<BlockEntityType<TileRailPreview>> TILE_RAIL_PREVIEW =
			BLOCK_ENTITIES.register("block_rail_preview", () -> BlockEntityType.Builder
					.of(TileRailPreview::new, RAIL_PREVIEW.get()).build(DSL.remainderType()));

	public static final RegistryObject<Block> RAIL_GAG =
			BLOCKS.register("block_rail_gag", BlockRailGag::new);
	public static final RegistryObject<BlockEntityType<TileRailGag>> TILE_RAIL_GAG =
			BLOCK_ENTITIES.register("block_rail_gag", () -> BlockEntityType.Builder
					.of(TileRailGag::new, RAIL_GAG.get()).build(DSL.remainderType()));

	public static final RegistryObject<Block> RAIL =
			BLOCKS.register("block_rail", BlockRail::new);
	public static final RegistryObject<BlockEntityType<TileRail>> TILE_RAIL =
			BLOCK_ENTITIES.register("block_rail", () -> BlockEntityType.Builder
					.of(TileRail::new, RAIL.get()).build(DSL.remainderType()));

	public static void register(IEventBus modEventBus) {
		BLOCKS.register(modEventBus);
		BLOCK_ENTITIES.register(modEventBus);
	}
}
