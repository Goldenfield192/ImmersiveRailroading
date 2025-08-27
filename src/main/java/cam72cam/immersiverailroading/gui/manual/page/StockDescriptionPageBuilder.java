package cam72cam.immersiverailroading.gui.manual.page;

import cam72cam.immersiverailroading.gui.manual.element.MDStockModelRenderer;
import cam72cam.immersiverailroading.gui.markdown.IPageBuilder;
import cam72cam.immersiverailroading.gui.markdown.DefaultPageBuilder;
import cam72cam.immersiverailroading.gui.markdown.MarkdownDocument;
import cam72cam.immersiverailroading.gui.markdown.element.MarkdownElement;
import cam72cam.immersiverailroading.gui.markdown.element.MarkdownStyledText;
import cam72cam.immersiverailroading.gui.markdown.element.MarkdownUrl;
import cam72cam.immersiverailroading.library.ItemComponentType;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.mod.resource.Identifier;

import java.util.*;

public class StockDescriptionPageBuilder implements IPageBuilder {
    public static final IPageBuilder INSTANCE = new StockDescriptionPageBuilder();

    @Override
    public MarkdownDocument build(Identifier id){
        MarkdownDocument document = new MarkdownDocument(id);
        EntityRollingStockDefinition def = DefinitionManager.getDefinition(id.getPath());

        if(def.description != null && def.description.canLoad()){
            return DefaultPageBuilder.INSTANCE.build(def.description);
        }

        document.addLine(new MDStockModelRenderer(def))
                .addLine(new MarkdownStyledText("Modeler: "), new MarkdownStyledText(def.modelerName))
                .addLine(new MarkdownStyledText("Pack: "), new MarkdownStyledText(def.packName))
                .addLine(new MarkdownStyledText(""))
                .addLine(new MarkdownStyledText("Required components:"));
        Map<String, Integer> componentMap = new HashMap<>();
        for(ItemComponentType componentType : def.getItemComponents()){
            componentMap.computeIfPresent(componentType.name(), (string, integer) -> integer+1);
            componentMap.putIfAbsent(componentType.name(), 1);
        }

        componentMap.forEach((orig, integer) -> {
            String replace = orig.toLowerCase().replace('_', ' ');
            char[] c = replace.toCharArray();
            c[0] = Character.toUpperCase(c[0]);
            String uppercase = new String(c);
            List<MarkdownElement> elements = new ArrayList<>(16);
            elements.add(new MarkdownStyledText(integer.toString()));
            elements.add(new MarkdownStyledText(" * "));
            elements.add(new MarkdownUrl(uppercase, new Identifier("iritem", def.defID + '@' + orig)));
            document.addLine(elements);
        });

        return document;
    }

    @Override
    public boolean validatePath(Identifier id) {
        return id.getDomain().equals("irstock") && DefinitionManager.getDefinition(id.getPath()) != null;
    }

    @Override
    public String getPageTooltipName(Identifier id) {
        if(validatePath(id)){
            return id.getPath().split("/")[id.getPath().split("/").length - 1];
        }
        return "";
    }
}
