package cam72cam.immersiverailroading.gui.manual;

import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.gui.markdown.MarkdownDocument;
import cam72cam.immersiverailroading.gui.markdown.element.MarkdownItemRenderer;
import cam72cam.immersiverailroading.gui.markdown.element.MarkdownStyledText;
import cam72cam.immersiverailroading.gui.markdown.element.MarkdownUrl;
import cam72cam.immersiverailroading.items.ItemRollingStockComponent;
import cam72cam.immersiverailroading.library.CraftingType;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.resource.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemComponentPageBuilder {
    private static final List<ItemStack> COMPONENTS = new ArrayList<>(IRItems.ITEM_ROLLING_STOCK_COMPONENT.getItemVariants());

    public static MarkdownDocument build(Identifier id){
        String defID = id.getPath().split("@")[0];
        String type = id.getPath().split("@")[1].toUpperCase();
        MarkdownDocument document = new MarkdownDocument(id);
        ItemRollingStockComponent.Data d = null;
        Optional<ItemStack> stack = COMPONENTS.stream().filter(itemstack -> {
            ItemRollingStockComponent.Data data = new ItemRollingStockComponent.Data(itemstack);
            return data.componentType.name().equals(type) && data.def.defID.equals(defID);
        }).findFirst();
        document.addLine(new MarkdownItemRenderer(stack.get()));
        d = new ItemRollingStockComponent.Data(stack.get());

        document.addLine(new MarkdownStyledText("Item component for "), new MarkdownUrl(d.def.name(), new Identifier("irstock", defID)));
        if(d.componentType.crafting == CraftingType.CASTING || d.componentType.crafting == CraftingType.CASTING_HAMMER){
            document.addLine(new MarkdownStyledText("Can be crafted in "), new MarkdownUrl("Casting Basin", "immersiverailroading:wiki/en_us/casting_basin.md"));
        } else {
            document.addLine(new MarkdownStyledText("Can be crafted in "),
                    new MarkdownUrl("Plate Rolling Machine", "immersiverailroading:wiki/en_us/plate_rolling_machine.md"),
                    new MarkdownStyledText(" and "),
                    new MarkdownUrl("Boiler Roller", "immersiverailroading:wiki/en_us/boiler_roller.md"));
        }
        if(d.requiresHammering()){
            document.addLine(new MarkdownStyledText("And this component requires the following in the "), new MarkdownUrl("Steam Hammer", "immersiverailroading:wiki/en_us/steam_hammer.md"));
        }
        return document;
    }
}
