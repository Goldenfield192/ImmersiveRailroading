package cam72cam.immersiverailroading.gui.manual.page;

import cam72cam.immersiverailroading.gui.manual.element.MDTrackRenderer;
import cam72cam.immersiverailroading.gui.markdown.IPageBuilder;
import cam72cam.immersiverailroading.gui.markdown.MarkdownDocument;
import cam72cam.immersiverailroading.gui.markdown.element.MarkdownStyledText;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.registry.TrackDefinition;
import cam72cam.mod.resource.Identifier;

public class TrackPageBuilder implements IPageBuilder {
    public static final IPageBuilder INSTANCE = new TrackPageBuilder();

    @Override
    public MarkdownDocument build(Identifier id) {
        MarkdownDocument document = new MarkdownDocument(id);
        TrackDefinition def = DefinitionManager.getTrack("immersiverailroading:track/" + id.getPath());
        document.addLine(new MDTrackRenderer(def));
        document.addLine(new MarkdownStyledText("Name: " + def.name));
        document.addLine(new MarkdownStyledText("Modeler: " + def.modelerName));
        document.addLine(new MarkdownStyledText("Pack: " + def.packName));
        return document;
    }

    @Override
    public boolean validatePath(Identifier id) {
        return id.getDomain().equals("irtrack");
    }

    @Override
    public String getPageTooltipName(Identifier id) {
        if(validatePath(id)){
            return id.getPath();
        }
        return "";
    }
}
