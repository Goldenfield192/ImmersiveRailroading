package cam72cam.immersiverailroading.gui.manual;

import cam72cam.immersiverailroading.gui.markdown.element.MarkdownElement;
import cam72cam.immersiverailroading.model.StockModel;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.render.obj.OBJRender;
import cam72cam.mod.render.opengl.RenderState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MarkdownStockModelRenderer extends MarkdownElement {
    public StockModel<?,?> model;
    public List<String> groups;
    public double length;
    public double width;
    public double height;
    public EntityRollingStockDefinition def;

    public static final double SIN30 = Math.sin(Math.toRadians(30));
    public static final double COS30 = Math.cos(Math.toRadians(30));

    public MarkdownStockModelRenderer(EntityRollingStockDefinition definition, String... groups) {
        this.model = definition.getModel();
        this.def = definition;
        if(groups.length != 0){
            this.groups = Arrays.stream(groups).collect(Collectors.toList());
        } else {
            this.groups = new ArrayList<>(model.groups.keySet());
        }
        Vec3d max = model.maxOfGroup(model.groups());
        Vec3d min = model.minOfGroup(model.groups());
        this.length = max.x - min.x;
        this.width = max.z - min.z;
        this.height = max.y - min.y;
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
        double scale = pageWidth / (length * COS30 + SIN30 * (height * SIN30 + width * COS30));
        double screenHeight = scale * (height * COS30 + width * SIN30);
        state1.translate(0,0,100);
        state1.translate(pageWidth / 2d,height * scale * 1.4,0);
        state1.rotate(180,0,1,0);
        state1.rotate(30,1,0,0);
        state1.rotate(30,0,1,0);
        state1.scale(-scale, -scale, -scale);
        state1.lightmap(1,1);
        state1.lighting(true);
        try(OBJRender.Binding binding = model.binder().bind(state1)){
            binding.draw(groups);
        }
        state.translate(0,screenHeight * 1.8,0);
        return (int) (screenHeight * 1.8);
    }
}
