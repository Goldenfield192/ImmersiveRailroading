package cam72cam.immersiverailroading.gui.markdown;

import cam72cam.immersiverailroading.gui.manual.ItemComponentPageBuilder;
import cam72cam.immersiverailroading.gui.manual.StockDescriptionPageBuilder;
import cam72cam.mod.resource.Identifier;

import java.util.HashMap;
import java.util.Map;

public class MarkdownPageManager {
    private static final Map<String, IPageBuilder> ACCEPTABLE_DOMAINS = new HashMap<>();
    private static final Map<String, Map<Identifier, MarkdownDocument>> CUSTOM_PAGES = new HashMap<>();

    static {
        registerPageBuilder("irstock", StockDescriptionPageBuilder.INSTANCE);
        registerPageBuilder("iritem", ItemComponentPageBuilder.INSTANCE);
//        registerPageBuilder("irmultiblock", MultiblockPageBuilder::build);
        registerPageBuilder("immersiverailroading", MarkdownBuilder.INSTANCE);
    }

    public static void registerPageBuilder(String domain, IPageBuilder builder){
        ACCEPTABLE_DOMAINS.put(domain, builder);
        CUSTOM_PAGES.put(domain, new HashMap<>());
    }

    /**
     * Try to get a cached page
     * @param id The page's content location
     * @return The cached page or a new page if not present
     */
    public static synchronized MarkdownDocument getOrComputePageByID(Identifier id, int screenWidth){
        MarkdownDocument document;
        if(ACCEPTABLE_DOMAINS.containsKey(id.getDomain())){
            IPageBuilder builder = ACCEPTABLE_DOMAINS.get(id.getDomain());
            document = CUSTOM_PAGES.get(id.getDomain()).computeIfAbsent(id, identifier -> builder.build(id));
        } else {
            throw new IllegalArgumentException();
        }
        document.setPageWidth(screenWidth);
        return MarkdownLineBreaker.breakDocument(document, screenWidth);
    }

    /**
     * API method for dynamic generated content
     * @param id The cached page need to be cleared
     */
    public static synchronized void refreshByID(Identifier id){
        if(ACCEPTABLE_DOMAINS.containsKey(id.getDomain())){
            IPageBuilder builder = ACCEPTABLE_DOMAINS.get(id.getDomain());
            CUSTOM_PAGES.get(id.getDomain()).computeIfPresent(id, (ident, document) -> builder.build(ident));
        }
    }

    public static String getPageName(Identifier id){
        if(ACCEPTABLE_DOMAINS.containsKey(id.getDomain())){
            return ACCEPTABLE_DOMAINS.get(id.getDomain()).getPageTooltipName(id);
        }
        return "";
    }

    public static boolean validate(Identifier id){
        return ACCEPTABLE_DOMAINS.containsKey(id.getDomain()) && ACCEPTABLE_DOMAINS.get(id.getDomain()).validatePath(id);
    }
}
