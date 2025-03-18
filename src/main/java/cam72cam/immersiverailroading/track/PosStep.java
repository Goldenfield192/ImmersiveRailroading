package cam72cam.immersiverailroading.track;

import cam72cam.mod.math.Vec3d;

public class PosStep extends Vec3d {
    public final float yaw;
    public final float pitch;
    public final float roll;

    public PosStep(double xIn, double yIn, double zIn, float yaw, float pitch, float roll) {
        super(xIn, yIn, zIn);
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    public PosStep(double xIn, double yIn, double zIn, float yaw, float pitch) {
        this(xIn, yIn, zIn, yaw, pitch, 0);
    }

    public PosStep(Vec3d orig, float angle, float pitch, float roll) {
        this(orig.x, orig.y, orig.z, angle, pitch, roll);
    }

    public PosStep(Vec3d orig, float angle, float pitch) {
        this(orig.x, orig.y, orig.z, angle, pitch);
    }
}
