package cam72cam.immersiverailroading.gui.manual;

import cam72cam.immersiverailroading.gui.ManualGui;
import cam72cam.immersiverailroading.gui.markdown.MarkdownDocument;
import cam72cam.immersiverailroading.gui.markdown.MarkdownPageManager;
import cam72cam.immersiverailroading.gui.markdown.element.MarkdownStyledText;
import cam72cam.immersiverailroading.gui.markdown.element.MarkdownUrl;
import cam72cam.mod.resource.Identifier;
import cam72cam.mod.text.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageProvider {
    public static final String SYNTAX = "[lang_provider]";

    public static List<MarkdownDocument.MarkdownLine> parse(String input, MarkdownDocument context){
        List<MarkdownDocument.MarkdownLine> lines = new ArrayList<>();
        Locale client = new TextUtil().getClientLocal();
        if(MarkdownPageManager.getAvailableLanguages().contains(client.toString().toLowerCase())){
            lines.add(MarkdownDocument.MarkdownLine.create(new MarkdownStyledText("Current language not available")));
        }
        lines.add(MarkdownDocument.MarkdownLine.create(new MarkdownStyledText("All available languages:")));
        for(String str : MarkdownPageManager.getAvailableLanguages()){
            lines.add(MarkdownDocument.MarkdownLine.create(new MarkdownUrl(str, ""){
                @Override
                public void click(MarkdownDocument document) {
                    ManualGui.setLang(this.text);
                    ManualGui.refresh();
                }

                @Override
                public void renderTooltip(Identifier id, int bottomBound) {
                    super.renderTooltip(id, bottomBound);
                }
            }));
        }
        return lines;
    }
}
