package cam72cam.immersiverailroading.gui.manual;

import cam72cam.immersiverailroading.gui.ManualGui;
import cam72cam.immersiverailroading.gui.markdown.MarkdownDocument;
import cam72cam.immersiverailroading.gui.markdown.MarkdownModelRenderer;
import cam72cam.immersiverailroading.gui.markdown.MarkdownStyledText;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.mod.ModCore;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class StockDescriptionProvider {
    public static final String SYNTAX = "[stock_description_provider]";

    public static List<MarkdownDocument.MarkdownLine> parse(String input, MarkdownDocument context){
        ModCore.info("called");
        List<MarkdownDocument.MarkdownLine> lines = new LinkedList<>();
        EntityRollingStockDefinition def = DefinitionManager.getDefinition(ManualGui.currentDefID);
        lines.add(MarkdownDocument.MarkdownLine.create(new MarkdownModelRenderer(def.getModel())));
        lines.add(MarkdownDocument.MarkdownLine.create(Arrays.asList(new MarkdownStyledText("Name: "), new MarkdownStyledText(def.name))));
        lines.add(MarkdownDocument.MarkdownLine.create(Arrays.asList(new MarkdownStyledText("Modeler: "), new MarkdownStyledText(def.modelerName))));
        lines.add(MarkdownDocument.MarkdownLine.create(Arrays.asList(new MarkdownStyledText("Pack: "), new MarkdownStyledText(def.packName))));
        return lines;
    }
}
