package cam72cam.immersiverailroading.gui.markdown;

import cam72cam.mod.resource.Identifier;

public interface IPageBuilder {
    MarkdownDocument build(Identifier id);

    boolean validatePath(Identifier id);

    String getPageTooltipName(Identifier id);
}
