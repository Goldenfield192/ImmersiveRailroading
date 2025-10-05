package cam72cam.immersiverailroading.render;

import cam72cam.immersiverailroading.library.TrackModelPart;
import cam72cam.mod.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VecYawPitch extends Vec3d {
    private final float yaw;
    private final float pitch;
    private final float length;
    private final List<TrackModelPart> parts;
    private final List<VecYawPitch> children;

    public VecYawPitch(double xIn, double yIn, double zIn, float yaw, TrackModelPart... parts) {
        this(xIn, yIn, zIn, yaw, 0, parts);
    }

    public VecYawPitch(double xIn, double yIn, double zIn, float yaw, float pitch, TrackModelPart... parts) {
        this(xIn, yIn, zIn, yaw, pitch, -1, parts);
    }

    public VecYawPitch(double xIn, double yIn, double zIn, float yaw, float pitch, float length, TrackModelPart... parts) {
        super(xIn, yIn, zIn);
        this.yaw = yaw;
        this.parts = Arrays.asList(parts);
        this.pitch = pitch;
        this.length = length;
        this.children = new ArrayList<>();
    }

    public void addChild(VecYawPitch another) {
        this.children.add(another);
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getLength() {
        return this.length;
    }

    public List<TrackModelPart> getParts() {
        return this.parts;
    }

    public List<VecYawPitch> getChildren() {
        return children;
    }

    @Override
    public VecYawPitch add(Vec3d other) {
        return new VecYawPitch(this.x + other.x, this.y + other.y, this.z + other.z, this.yaw, this.pitch, this.length);
    }
}
