package cam72cam.immersiverailroading.gui.markdown.element;

import cam72cam.immersiverailroading.gui.markdown.ManualHoverRenderer;
import cam72cam.immersiverailroading.gui.markdown.MarkdownDocument;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.resource.Identifier;

import java.awt.geom.Rectangle2D;

/**
 * Abstract class for those elements which can be clicked
 * @see MarkdownUrl
 */
public abstract class MarkdownClickableElement extends MarkdownElement{
    public Rectangle2D section;

    /**
     * Called upon click inside the detect section
     */
    public abstract void click(MarkdownDocument context);

    public abstract void updateSection(Vec3d offset);

    /**
     * Render a specific string as tooltip
     * @param bottomBound Param for internal use
     */
    public void renderTooltip(String text, int bottomBound){
        ManualHoverRenderer.renderTooltip(text, bottomBound);
    }

    /**
     * Called when mouse is hovering
     * @param bottomBound Param for internal use
     */
    public abstract void renderTooltip(Identifier id, int bottomBound);
}
