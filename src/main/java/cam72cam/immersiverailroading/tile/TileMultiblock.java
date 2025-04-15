package cam72cam.immersiverailroading.tile;

import cam72cam.immersiverailroading.IRBlocks;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.library.CraftingMachineMode;
import cam72cam.immersiverailroading.library.Permissions;
import cam72cam.immersiverailroading.multiblock.Multiblock.MultiblockInstance;
import cam72cam.immersiverailroading.multiblock.MultiblockRegistry;
import cam72cam.immersiverailroading.net.MultiblockSelectCraftPacket;
import cam72cam.mod.energy.Energy;
import cam72cam.mod.energy.IEnergy;
import cam72cam.mod.entity.Player;
import cam72cam.mod.entity.boundingbox.IBoundingBox;
import cam72cam.mod.item.IInventory;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.item.ItemStackHandler;
import cam72cam.mod.math.Rotation;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.serialization.TagField;
import cam72cam.mod.util.Facing;
import cam72cam.mod.world.BlockInfo;
import cam72cam.mod.world.World;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileMultiblock extends BlockEntity implements ICapabilityProvider {

	@TagField("replaced")
	private BlockInfo replaced;
	@TagField("offset")
	private Vec3i offset;
	@TagField("rotation")
	private Rotation rotation;
	@TagField("name")
	private String name;
	@TagField("craftMode")
	private CraftingMachineMode craftMode = CraftingMachineMode.STOPPED;
	private long ticks;
	private MultiblockInstance mb;
	
	//Crafting
	@TagField("craftProgress")
	private int craftProgress = 0;
	@TagField("craftItem")
	private ItemStack craftItem = ItemStack.EMPTY;
	@TagField
	private ItemStackHandler container = new ItemStackHandler(0);
	private net.minecraftforge.items.ItemStackHandler handler =
	@TagField("energyStorage")
    private Energy energy = new Energy(0, 1000);

	public TileMultiblock(BlockPos p_155229_, BlockState p_155230_) {
		super(IRBlocks.TILE_MULTIBLOCK.get(), p_155229_, p_155230_);
	}

	public boolean isLoaded() {
			//TODO FIX ME bad init
    	return this.name != null && this.name.length() != 0;
    }

	public void configure(String name, Rotation rot, Vec3i offset, BlockInfo replaced) {
		this.name = name;
		this.rotation = rot;
		this.offset = offset;
		this.replaced = replaced;
		
		container.setSize(this.getMultiblock().getInvSize(offset));
		
		this.setChanged();
	}

	@Override
	public void load(CompoundTag nbt) {
		container.onChanged(slot -> this.setChanged());
		container.setSlotLimit(slot -> getMultiblock().getSlotLimit(offset, slot));
		energy.onChanged(this::setChanged);
	}

	public void update() {
		this.ticks += 1;

		if (offset != null && getMultiblock() != null) {
			this.getMultiblock().tick(offset);
		} else if (ticks > 20) {
			System.out.println("Error in multiblock, reverting");
			getLevel().setBlockAndUpdate(getBlockPos(), Blocks.AIR.defaultBlockState());
		}
	}

	@Override
	public AABB getRenderBoundingBox() {
		return IBoundingBox.INFINITE;
	}

	public Vec3i getOrigin() {
		return new Vec3i(getBlockPos()).subtract(offset.rotate(rotation));
	}
	
	public MultiblockInstance getMultiblock() {
		if (this.mb == null && this.isLoaded()) {
			this.mb = MultiblockRegistry.get(name).instance(World.get(getLevel()), getOrigin(), rotation);
		}
		return this.mb;
	}
	
	public String getName() {
		return name;
	}
	
	public long getRenderTicks() {
		return this.ticks;
	}
	
	public ItemStackHandler getContainer() {
		if (container.getSlotCount() != getMultiblock().getInvSize(offset)) {
			container.setSize(getMultiblock().getInvSize(offset));
		}
		return this.container;
	}

	/*
	 * BlockType Functions to pass on to the multiblock
	 */
	public void breakBlock() {
		if (getMultiblock() != null) {
			getMultiblock().onBreak();
		}
	}

	public boolean onBlockActivated(Player player, Player.Hand hand) {
		return getMultiblock().onBlockActivated(player, hand, offset);
	}
	
	/*
	 * Event Handlers
	 */
	
	public void onBreakEvent() {
		for (int slot = 0; slot < container.getSlotCount(); slot ++) {
			ItemStack item = container.get(slot);
			if (!item.isEmpty()) {
				getWorld().dropItem(item, getPos());
			}
		}

		if (replaced != null) {
			getWorld().setBlock(getPos(), replaced);
		}
	}

	public boolean isRender() {
		return getMultiblock().isRender(offset);
	}

	public double getRotation() {
		return 180 - Facing.EAST.rotate(rotation).getAngle();
	}
	
	/*
	 * Crafting
	 */
	public int getCraftProgress() {
		return craftProgress;
	}
	
	public void setCraftProgress(int progress) {
		if (craftProgress != progress) {
			craftProgress = progress;
			this.setChanged();
		}
	}
	
	public CraftingMachineMode getCraftMode() {
		return craftMode;
	}
	
	public void setCraftMode(CraftingMachineMode mode) {
		if (!getLevel().isClientSide()) {
			if (craftMode != mode) {
				craftMode = mode;
				this.setChanged();
			}
		} else {
			new MultiblockSelectCraftPacket(new Vec3i(getBlockPos()), craftItem, mode).sendToServer();
		}
	}
	
	public ItemStack getCraftItem() {
		return craftItem;
	}

	public void setCraftItem(ItemStack selected) {
		if (!getLevel().isClientSide()) {
			if (selected == null || !selected.equals(craftItem)) {
				this.craftItem = selected == null ? null : selected.copy();
				this.craftProgress = 0;
				this.setChanged();
			}
		} else {
			new MultiblockSelectCraftPacket(new Vec3i(getBlockPos()), selected, craftMode).sendToServer();
		}
	}
	
	/*
	 * Capabilities
	 */

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return super.getCapability(cap, side);
	}

	@Override
	public IInventory getInventory(Facing facing) {
		if (this.getMultiblock() == null || this.getMultiblock().getInvSize(offset) == 0) {
			return null;
		}

		if (container.getSlotCount() != getMultiblock().getInvSize(offset)) {
			container.setSize(getMultiblock().getInvSize(offset));
		}

		return new IInventory() {
			@Override
			public int getSlotCount() {
				return container.getSlotCount();
			}

			@Override
			public ItemStack get(int slot) {
				return container.get(slot);
			}

			@Override
			public void set(int slot, ItemStack stack) {
				container.set(slot, stack);
			}

			@Override
			public ItemStack insert(int slot, ItemStack stack, boolean simulate) {
				if (getMultiblock().canInsertItem(offset, slot, stack)) {
					return container.insert(slot, stack, simulate);
				}
				return stack;
			}

			@Override
			public ItemStack extract(int slot, int amount, boolean simulate) {
				if (getMultiblock().isOutputSlot(offset, slot)) {
					return container.extract(slot, amount, simulate);
				}
				return ItemStack.EMPTY;
			}

			@Override
			public int getLimit(int slot) {
				return container.getLimit(slot);
			}
		};
	}

	@Override
	public IEnergy getEnergy(Facing facing) {
		return this.isLoaded() && this.getMultiblock().canRecievePower(offset) ? energy : null;
	}

	@Override
	public void onBreak() {
		try {
			// Multiblock break
			this.breakBlock();
		} catch (Exception ex) {
			ImmersiveRailroading.catching(ex);
			// Something broke
			// TODO figure out why
			getWorld().setToAir(getPos());
		}
	}

	@Override
	public boolean onClick(Player player, Player.Hand hand, Facing facing, Vec3d hit) {
		if (!player.hasPermission(Permissions.MACHINIST)) {
			return false;
		}
		return onBlockActivated(player, hand);
	}

	@Override
	public ItemStack onPick() {
		return ItemStack.EMPTY;
	}
}
