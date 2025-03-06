package cam72cam.immersiverailroading.gui.markdown;

import cam72cam.immersiverailroading.gui.manual.ItemComponentPageBuilder;
import cam72cam.immersiverailroading.gui.manual.StockDescriptionPageBuilder;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.mod.ModCore;
import cam72cam.mod.resource.Identifier;

import java.io.IOException;
import java.util.HashMap;

public class MarkdownPageManager {
    private static final HashMap<Identifier, MarkdownDocument> MAIN_PAGES = new HashMap<>();
    private static final HashMap<Identifier, MarkdownDocument> STOCK_PAGES = new HashMap<>();
    private static final HashMap<Identifier, MarkdownDocument> ITEM_PAGES = new HashMap<>();
    //TODO
    private static final HashMap<Identifier, MarkdownDocument> MB_PAGES = new HashMap<>();

    /**
     * Try to get a cached page
     * @param id The page's content location
     * @return The cached page or a new page if not present
     */
    public static synchronized MarkdownDocument getOrComputePageByID(Identifier id, int screenWidth){
        MarkdownDocument document;
        ModCore.info(id.toString());
        refreshByID(id);
        switch (id.getDomain()) {
            case "irstock":
                document = STOCK_PAGES.computeIfAbsent(id, StockDescriptionPageBuilder::build);
                break;
            case "iritem":
                document = ITEM_PAGES.computeIfAbsent(id, ItemComponentPageBuilder::build);
                break;
//            case "irmultiblock":
//                document = MB_PAGES.computeIfAbsent(id, MarkdownDocument::new);
            case "immersiverailroading":
            default:
                document = MAIN_PAGES.computeIfAbsent(id, identifier -> {
                    try {
                        return MarkdownBuilder.build(identifier);
                    } catch (IOException e){
                        throw new RuntimeException();
                    }
                });
        }
        document.setPageWidth(screenWidth);
        return MarkdownLineBreaker.breakDocument(document, screenWidth);
    }

    /**
     * API method for dynamic generated content
     * @param id The cached page need to be cleared
     */
    public static synchronized void refreshByID(Identifier id){
        MAIN_PAGES.computeIfPresent(id, ((identifier, document) -> {
            try {
                ModCore.info("test");
                ModCore.info(identifier.toString());
                return MarkdownBuilder.build(identifier);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        STOCK_PAGES.computeIfPresent(id, (identifier, document) -> StockDescriptionPageBuilder.build(identifier));
        ITEM_PAGES.computeIfPresent(id, (identifier, document) -> ItemComponentPageBuilder.build(identifier));
//        Optional.ofNullable(ITEM_PAGES.get(id)).ifPresent(document -> {
//            document.clearCache();
//            try {
//                MarkdownBuilder.build(id, document.getPageWidth());
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//        Optional.ofNullable(MB_PAGES.get(id)).ifPresent(document -> {
//            document.clearCache();
//            try {
//                MarkdownBuilder.build(id, document.getPageWidth());
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
    }

    public static String getPageName(Identifier id){
        switch (id.getDomain()) {
            case "irstock":
                return DefinitionManager.getDefinition(id.getPath()).name();
            case "iritem":
                return id.getPath().replaceAll("_", " ");
//            case "irmultiblock":
//                document = MB_PAGES.computeIfAbsent(id, MarkdownDocument::new);
            case "immersiverailroading":
            default:
                return id.getPath().split("/")[id.getPath().split("/").length - 1];
        }
    }

    public static boolean validate(Identifier id){
        return (id.getDomain().equals("immersiverailroading") && id.getPath().endsWith(".md"))
                || id.getDomain().equals("irstock")
                || id.getDomain().equals("iritem")
                || id.getDomain().equals("irmultiblock");
    }
}
