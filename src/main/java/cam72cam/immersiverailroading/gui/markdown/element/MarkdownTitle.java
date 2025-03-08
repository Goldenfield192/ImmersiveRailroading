package cam72cam.immersiverailroading.gui.markdown.element;

import cam72cam.mod.gui.helpers.GUIHelpers;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.render.opengl.RenderState;

import static cam72cam.immersiverailroading.gui.markdown.Colors.DEFAULT_TEXT_COLOR;

/**
 * Element class representing a title
 * <p>
 * Text will be scale by level:
 * <p>
 * 1 -> scale to 1.35x and italicize
 * <p>
 * 2 -> scale to 1.15x and italicize
 * <p>
 * 3+ -> Italic regular text
 * <p>
 * CANNOT CONTAIN URL
 * @see MarkdownElement
 */
public class MarkdownTitle extends MarkdownElement {
    //Starting from 1
    public final int level;

    public static final double LEVEL1 = 1/1.8;
    public static final double LEVEL2 = 1/1.5;
    public static final double LEVEL3 = 1/1.2;

    public MarkdownTitle(String text) {
        label: {
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) != '#') {
                    this.level = i;
                    this.text = text.substring(i).trim();
                    break label;
                }
            }
            //All the chars are '#'
            this.text = "";
            this.level = -1;
        }
    }

    public MarkdownTitle(String text, int level) {
        this.text = text;
        this.level = level;
    }

    @Override
    public String apply() {
        if(level == -1){//Invalid
            return "";
        } else {
            return text;
        }
    }

    @Override
    public MarkdownElement[] split(int splitPos) {
        int i = splitPos;
        while (this.text.charAt(i) == ' '){
            i++;
            if(i == this.text.length()){//Reaching end, which means chars after splitPos are all spaces
                return new MarkdownElement[]{
                        new MarkdownTitle(this.text.substring(0, splitPos), this.level),
                        //Just return empty string
                        new MarkdownTitle("", this.level)};
            }
        }
        return new MarkdownElement[]{
                new MarkdownTitle(this.text.substring(0, splitPos), this.level),
                new MarkdownTitle(this.text.substring(i), this.level)};
    }

    @Override
    public int render(RenderState state, int pageWidth){
        Vec3d offset = state.model_view().apply(Vec3d.ZERO);
        String str = this.apply();
        if(this.level == 1){
            //Scale matrix
            state.translate(-offset.x, -offset.y, 0);
            state.scale(1.8, 1.8, 1.8);
            state.translate(offset.x * LEVEL1, offset.y * LEVEL1, 0);
            GUIHelpers.drawString(str, 0, 0, DEFAULT_TEXT_COLOR, state.model_view());

            //Revert matrix
            state.translate(-offset.x * LEVEL1, -offset.y * LEVEL1, 0);
            state.scale(LEVEL1, LEVEL1, LEVEL1);
            state.translate(offset.x, offset.y, 0);

            //Move down
            state.translate(0, 18, 0);
            return 18;
        } else if(this.level == 2){
            //Scale matrix
            state.translate(-offset.x, -offset.y, 0);
            state.scale(1.5, 1.5, 1.5);
            state.translate(offset.x * LEVEL2, offset.y * LEVEL2, 0);
            GUIHelpers.drawString(str, 0, 0, DEFAULT_TEXT_COLOR, state.model_view());

            //Revert matrix
            state.translate(-offset.x * LEVEL2, -offset.y * LEVEL2, 0);
            state.scale(LEVEL2, LEVEL2, LEVEL2);
            state.translate(offset.x, offset.y, 0);
            //Move down
            state.translate(0, 15, 0);
            return 15;
        } else if (this.level == 3) {
            //Scale matrix
            state.translate(-offset.x, -offset.y, 0);
            state.scale(1.2, 1.2, 1.2);
            state.translate(offset.x * LEVEL3, offset.y * LEVEL3, 0);
            GUIHelpers.drawString(str, 0, 0, DEFAULT_TEXT_COLOR, state.model_view());

            //Revert matrix
            state.translate(-offset.x * LEVEL3, -offset.y * LEVEL3, 0);
            state.scale(LEVEL3, LEVEL3, LEVEL3);
            state.translate(offset.x, offset.y, 0);
            //Move down
            state.translate(0, 12, 0);
            return 12;
        } else {
            GUIHelpers.drawString(str, 0, 0, DEFAULT_TEXT_COLOR, state.model_view());
            state.translate(0, 10, 0);
            return 10;
        }
    }
}
