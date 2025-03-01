package cam72cam.immersiverailroading.gui.markdown;

import cam72cam.immersiverailroading.model.StockModel;
import cam72cam.mod.model.obj.OBJModel;
import cam72cam.mod.render.opengl.RenderState;
import cam72cam.mod.render.opengl.VBO;
import util.Matrix4;

public class MarkdownModelRenderer extends MarkdownElement{
    public StockModel model;

    public MarkdownModelRenderer(StockModel model) {
        this.model = model;
    }

    @Override
    public String apply() {
        return "";
    }

    @Override
    public MarkdownElement[] split(int splitPos) {
        return new MarkdownElement[0];
    }

    @Override
    public int render(RenderState state, int pageWidth) {
        RenderState state1 = state.clone();
        state1.translate(pageWidth / 2d,60,0);
        state1.scale(-10,-10,-10);
        state1.lightmap(1,1);
        try(VBO.Binding binding = model.binder().bind(state1)){
            binding.draw();
        }
        state.translate(0,80,0);
        return 80;
    }
}
