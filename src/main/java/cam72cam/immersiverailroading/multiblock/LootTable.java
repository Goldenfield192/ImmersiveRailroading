package cam72cam.immersiverailroading.multiblock;

import cam72cam.mod.fluid.Fluid;
import cam72cam.mod.item.Fuzzy;
import cam72cam.mod.item.ItemStack;

import java.util.List;

public class LootTable {
    public Fuzzy input;
    public Fluid fluid;
    public int inputCount;
    public int rfPerCraft;
    public ItemStack output;
    public int outputCount;
    public boolean isRawCast;
    public int powerRequiredRF;
    public int craftTime;

    public LootTable(Fuzzy input, int inputCount, int rfPerCraft, ItemStack output, int outputCount, boolean isRawCast, int powerRequiredRF, int craftTime) {
        this.input = input;
        this.fluid = null;
        this.inputCount = inputCount;
        this.rfPerCraft = rfPerCraft;
        this.output = output;
        this.outputCount = outputCount;
        this.isRawCast = isRawCast;
        this.powerRequiredRF = powerRequiredRF;
        this.craftTime = craftTime;
    }

    public LootTable(Fluid input, int inputCount, int rfPerCraft, ItemStack output, int outputCount, boolean isRawCast, int powerRequiredRF, int craftTime) {
        this.fluid = input;
        this.input = null;
        this.inputCount = inputCount;
        this.rfPerCraft = rfPerCraft;
        this.output = output;
        this.outputCount = outputCount;
        this.isRawCast = isRawCast;
        this.powerRequiredRF = powerRequiredRF;
        this.craftTime = craftTime;
    }
}
