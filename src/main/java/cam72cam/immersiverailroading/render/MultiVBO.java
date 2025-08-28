package cam72cam.immersiverailroading.render;

import cam72cam.mod.render.opengl.RenderState;
import cam72cam.mod.render.opengl.VBO;

import java.util.Collections;
import java.util.Set;

public class MultiVBO {
    private final Set<VBO> vbos;

    public MultiVBO(Set<VBO> vbos) {
        this.vbos = vbos;
    }

    public MultiVBO(VBO vbo){
        this.vbos = Collections.singleton(vbo);
    }

    public void bindAndDraw(RenderState state, boolean waitForLoad){
        for(VBO v : vbos){
            try (VBO.Binding vbo = v.bind(state, waitForLoad)) {
                vbo.draw();
            }
        }
    }

    public void free(){
        for(VBO vbo : vbos){
            vbo.free();
        }
    }
}
