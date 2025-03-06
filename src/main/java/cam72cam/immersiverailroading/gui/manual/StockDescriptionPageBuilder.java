package cam72cam.immersiverailroading.gui.manual;

import cam72cam.immersiverailroading.gui.markdown.MarkdownBuilder;
import cam72cam.immersiverailroading.gui.markdown.MarkdownDocument;
import cam72cam.immersiverailroading.gui.markdown.element.MarkdownElement;
import cam72cam.immersiverailroading.gui.markdown.element.MarkdownStyledText;
import cam72cam.immersiverailroading.gui.markdown.element.MarkdownUrl;
import cam72cam.immersiverailroading.library.ItemComponentType;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.mod.resource.Identifier;

import java.io.IOException;
import java.util.*;

public class StockDescriptionPageBuilder {
    public static MarkdownDocument build(Identifier id){
        MarkdownDocument document = new MarkdownDocument(id);
        EntityRollingStockDefinition def = DefinitionManager.getDefinition(id.getPath());

        if(def.description.canLoad()){
            try {
                return MarkdownBuilder.build(def.description);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        document.addLine(MarkdownDocument.MarkdownLine.create(new MarkdownStockModelRenderer(def)));
        document.addLine(MarkdownDocument.MarkdownLine.create(Arrays.asList(new MarkdownStyledText("Modeler: "), new MarkdownStyledText(def.modelerName))));
        document.addLine(MarkdownDocument.MarkdownLine.create(Arrays.asList(new MarkdownStyledText("Pack: "), new MarkdownStyledText(def.packName))));

        document.addLine(new MarkdownStyledText(""));
        document.addLine(new MarkdownStyledText("Required components:"));
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
}
