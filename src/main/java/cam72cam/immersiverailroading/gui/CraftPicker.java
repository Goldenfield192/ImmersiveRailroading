package cam72cam.immersiverailroading.gui;

import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.immersiverailroading.items.ItemPlate;
import cam72cam.immersiverailroading.items.ItemRollingStock;
import cam72cam.immersiverailroading.items.ItemRollingStockComponent;
import cam72cam.immersiverailroading.items.ItemTabs;
import cam72cam.immersiverailroading.library.CraftingType;
import cam72cam.immersiverailroading.library.ItemComponentType;
import cam72cam.immersiverailroading.library.PlateType;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.util.IRFuzzy;
import cam72cam.mod.gui.screen.IScreenBuilder;
import cam72cam.mod.gui.helpers.ItemPickerGUI;
import cam72cam.mod.item.Fuzzy;
import cam72cam.mod.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CraftPicker {
	private final boolean enableItemPicker;
	private ItemPickerGUI stockSelector;
	private ItemPickerGUI itemSelector;
	private List<ItemStack> items;
	private Consumer<ItemStack> onChoose;
    public static void showCraftPicker(IScreenBuilder screen, ItemStack current, CraftingType craftType, Consumer<ItemStack> onChoose) {
        new CraftPicker(screen, current, craftType, null, onChoose);
    }

	public static void showCraftPicker(IScreenBuilder screen, ItemStack current, CraftingType craftType,
                                       List<String> packs, Consumer<ItemStack> onChoose) {
		new CraftPicker(screen, current, craftType, packs, onChoose);
	}
	
	private CraftPicker(IScreenBuilder screen, ItemStack current, CraftingType craftType, List<String> packs, Consumer<ItemStack> onChoose) {
		this.enableItemPicker = craftType.isCasting();
		this.onChoose = stack -> {
			screen.show();
			onChoose.accept(stack);
		};
        if(packs == null || packs.isEmpty()) {
            this.items = new ArrayList<>(IRItems.ITEM_ROLLING_STOCK_COMPONENT.getItemVariants());
        }else{
            this.items = IRItems.ITEM_ROLLING_STOCK_COMPONENT.getItemVariants().stream().filter(itemStack -> {
                ItemRollingStock.Data data = new ItemRollingStock.Data(itemStack);
                EntityRollingStockDefinition definition = data.def;
                return packs.contains(definition.getPackName());
            }).collect(Collectors.toList());
        }

        List<ItemStack> stock = new ArrayList<>();

        stock.addAll(IRItems.ITEM_ROLLING_STOCK.getItemVariants(ItemTabs.LOCOMOTIVE_TAB));
        stock.addAll(IRItems.ITEM_ROLLING_STOCK.getItemVariants(ItemTabs.PASSENGER_TAB));
        stock.addAll(IRItems.ITEM_ROLLING_STOCK.getItemVariants(ItemTabs.STOCK_TAB));

		List<ItemStack> toRemove = new ArrayList<ItemStack>();
		for (ItemStack item : items) {
			ItemRollingStockComponent.Data data = new ItemRollingStockComponent.Data(item);
			ItemComponentType comp = data.componentType;
			if (comp.isWooden(data.def)) {
				toRemove.add(item);
				continue;
			}
			boolean isCastable = craftType == CraftingType.CASTING && comp.crafting == CraftingType.CASTING_HAMMER;
			if (comp.crafting != craftType && !isCastable) {
				toRemove.add(item);
			}
		}
		items.removeAll(toRemove);


		stockSelector = new ItemPickerGUI(stock, this::onStockExit);
		toRemove = new ArrayList<>();
		for (ItemStack itemStock : stock) {
			boolean hasComponent = false;
			for (ItemStack item : items) {
				if (isPartOf(itemStock, item)) {
					hasComponent = true;
					break;
				}
			}
			if (!hasComponent) {
				toRemove.add(itemStock);
				continue;
			}
			if (isPartOf(itemStock, current)) {				
				stockSelector.choosenItem = itemStock;
			}
		}
		stock.removeAll(toRemove);
		
		if (craftType.isCasting()) {
        	stock.add(new ItemStack(IRItems.ITEM_CAST_RAIL, 1));
        	stock.addAll(IRFuzzy.steelIngotOrFallback().enumerate());
        	stock.addAll(IRFuzzy.steelBlockOrFallback().enumerate());
	        stock.addAll(IRItems.ITEM_AUGMENT.getItemVariants(ItemTabs.MAIN_TAB));
		}
		else if (craftType.isPlate()) {
			for (PlateType value : PlateType.values()) {
				if (value == PlateType.BOILER) {
					continue;
				}
				ItemStack item = new ItemStack(IRItems.ITEM_PLATE, 1);
				ItemPlate.Data data = new ItemPlate.Data(item);
				data.type = value;
				data.write();
				stock.add(item);
			}
		}
		stockSelector.setItems(stock);
		
		itemSelector = new ItemPickerGUI(new ArrayList<>(), this::onItemExit);
		if (current != null && (current.is(IRItems.ITEM_ROLLING_STOCK_COMPONENT) || current.is(IRItems.ITEM_ROLLING_STOCK))) {
			itemSelector.choosenItem = current;
		}

		// Draw/init
		if (stockSelector.choosenItem != null && enableItemPicker) {
			setupItemSelector();
			if (itemSelector.hasOptions()) {
				itemSelector.show();
				return;
			}
		}
		stockSelector.show();
	}
	
	private boolean isPartOf(ItemStack stock, ItemStack item) {
		if (stock == null || item == null) {
			return false;
		}
		
    	if (!stock.is(IRItems.ITEM_ROLLING_STOCK)) {
    		return false;
    	}
    	if (!item.is(IRItems.ITEM_ROLLING_STOCK_COMPONENT)) {
    		return false;
    	}
    	return new ItemRollingStockComponent.Data(item).def == new ItemRollingStock.Data(stock).def;
    }
	
	private void setupItemSelector() {
		List<ItemStack> filteredItems = new ArrayList<>();
		filteredItems.add(stockSelector.choosenItem);
		for (ItemStack item : items) {
			if (isPartOf(stockSelector.choosenItem, item)) {
				filteredItems.add(item);
			}
		}
		itemSelector.setItems(filteredItems);
	}
	
	private void onStockExit(ItemStack stack) {
		if (stack == null) {
			onChoose.accept(null);
		} else {
			if (enableItemPicker) {
				this.setupItemSelector();
				if (itemSelector.hasOptions()) {
					itemSelector.show();
				} else {
					this.itemSelector.choosenItem = null;
					onChoose.accept(stack);
				}
			} else {
				onChoose.accept(stack);
			}
		}
	}
	
	private void onItemExit(ItemStack stack) {
		if (stack == null) {
			stockSelector.show();
		} else {
			onChoose.accept(stack);
		}
	}
}
