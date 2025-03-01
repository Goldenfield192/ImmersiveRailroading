package cam72cam.immersiverailroading.gui.manual;

import cam72cam.immersiverailroading.gui.ManualGui;
import cam72cam.immersiverailroading.gui.markdown.MarkdownDocument;
import cam72cam.immersiverailroading.gui.markdown.MarkdownTitle;
import cam72cam.immersiverailroading.gui.markdown.MarkdownUrl;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.mod.ModCore;
import cam72cam.mod.resource.Identifier;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.*;
import java.util.stream.Collectors;

public class StockListProvider {
    public static final String SYNTAX = "[list_stock_page]";

    public static List<MarkdownDocument.MarkdownLine> parse(String input, MarkdownDocument context){
        List<MutablePair<String, EntityRollingStockDefinition>> definitions =
                DefinitionManager.getDefinitions().stream()
                .map(def -> {
                    switch (context.getProperty("stock")){
                        case 0:
                            return MutablePair.of("N/A".equals(def.name) ? "Unknown" : def.name, def);
                        case 1:
                            return MutablePair.of("N/A".equals(def.modelerName) ? "Unknown" : def.modelerName, def);
                        case 2:
                        default:
                            return MutablePair.of("N/A".equals(def.packName) ? "Unknown" : def.packName, def);
                    }
                })
                .sorted(Comparator.comparing(MutablePair::getLeft))
                .collect(Collectors.toList());
        List<MarkdownDocument.MarkdownLine> lines = new LinkedList<>();
        Character lastStartingChar = null;
        String lastFullName = null;
        for (MutablePair<String, EntityRollingStockDefinition> definition : definitions) {
            if(lastStartingChar == null || lastStartingChar != definition.getLeft().charAt(0)){
                lastStartingChar = definition.getLeft().charAt(0);
                ModCore.info(definition.getLeft());
                lines.add(MarkdownDocument.MarkdownLine.create(new MarkdownTitle(lastStartingChar.toString(), 1)));
            }
            if(context.getProperty("stock") != 0 && (lastFullName == null || !lastFullName.equals(definition.getLeft()))){
                lastFullName = definition.getLeft();
                lines.add(MarkdownDocument.MarkdownLine.create(new MarkdownTitle(lastFullName, 2)));
            }
            Identifier id = new Identifier("immersiverailroading:wiki/en_us/stock.md");
            lines.add(MarkdownDocument.MarkdownLine.create(new MarkdownUrl(definition.getRight().name, id){
                @Override
                public void renderTooltip(int bottomBound) {
                    renderTooltip("Click me th show this stock's dedicated page!", bottomBound);
                }

                @Override
                public void click(MarkdownDocument context) {
                    ManualGui.currentDefID = definition.getRight().defID;
                    MarkdownDocument.refreshByID(id);
                    super.click(context);
                }
            }));
        }
        return lines;
    }
}
