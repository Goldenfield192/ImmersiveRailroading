package cam72cam.immersiverailroading.util;

import cam72cam.immersiverailroading.model.StockModel;
import cam72cam.mod.entity.boundingbox.IBoundingBox;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.model.obj.FaceAccessor;
import cam72cam.mod.model.obj.OBJFace;
import cam72cam.mod.util.Axis;

import java.util.*;
import java.util.stream.Collectors;

public class MeshNavigator {
    // Theoretically this could be much lower. IR floor meshes probably won't use the whole depth, but who knows
    private static final int MAX_BVH_DEPTH = 20;
    private static final int MAX_LEAF_SIZE = 8;

    private final BVHNode root;
    private final BVHNode collisionRoot;
    private final boolean hasNavMesh;

    public MeshNavigator(StockModel<?, ?> model) {
        hasNavMesh = model.groups().stream().anyMatch(s -> s.contains("FLOOR"));
        if (hasNavMesh) {
            FaceAccessor accessor = model.getFaceAccessor();

            List<OBJFace> floor = new ArrayList<>();
            if (model.floor != null) {
                model.floor.groups().forEach(group -> {
                    FaceAccessor sub = accessor.getSubByGroup(group.name);
                    sub.forEach(a -> floor.add(a.asOBJFace()));
                });
            }
            this.root = buildBVH(floor, 0);

            List<OBJFace> collision = new ArrayList<>();
            if (model.collision != null) {
                model.collision.groups().forEach(group -> {
                    FaceAccessor sub = accessor.getSubByGroup(group.name);
                    sub.forEach(a -> collision.add(a.asOBJFace()));
                });
            }
            this.collisionRoot = buildBVH(collision, 0);
        } else {
            root = null;
            collisionRoot = null;
        }
    }

    public boolean hasNavMesh() {
        return hasNavMesh;
    }

    public static class BVHNode {
        IBoundingBox bound;
        BVHNode left;
        BVHNode right;
        List<OBJFace> triangles;

        boolean isLeaf() {
            return triangles != null;
        }
    }

    public BVHNode buildBVH(List<OBJFace> triangles, int depth) {
        if (triangles.size() <= MAX_LEAF_SIZE || depth > MAX_BVH_DEPTH) {
            IBoundingBox bound = IBoundingBox.ORIGIN;
            for (OBJFace face : triangles) {
                bound = bound.expandToFit(face.getBoundingBox());
            }
            BVHNode node = new BVHNode();
            node.bound = bound;
            node.triangles = triangles;
            return node;
        }

        IBoundingBox bound = IBoundingBox.from(Vec3i.ZERO);
        for (OBJFace face : triangles) {
            bound = bound.expandToFit(face.getBoundingBox());
        }

        Vec3d size = bound.max().subtract(bound.min());
        Axis longest = (size.x > size.y && size.x > size.z)
                       ? Axis.X
                       : (size.y > size.z
                          ? Axis.Y
                          : Axis.Z);

        triangles.sort((a, b) -> Double.compare(getCentroidInAxis(a, longest), getCentroidInAxis(b, longest)));
        int mid = triangles.size() / 2;

        BVHNode node = new BVHNode();
        node.left = buildBVH(triangles.subList(0, mid), depth + 1);
        node.right = buildBVH(triangles.subList(mid, triangles.size()), depth + 1);
        node.bound = bound;
        return node;
    }

    public List<OBJFace> getFloorFacesWithin(IBoundingBox targetBB, double gaugeScale) {
        return getCollidingFaces(this.root, targetBB, gaugeScale);
    }

    public List<OBJFace> getCollisionFacesWithin(IBoundingBox targetBB, double gaugeScale) {
        return getCollidingFaces(this.collisionRoot, targetBB, gaugeScale);
    }

    public List<OBJFace> getCollidingFaces(BVHNode root, IBoundingBox targetBB, double gaugeScale) {
        //Scale back to normal model
        targetBB = scaleBB(targetBB, 1 / gaugeScale);
        List<OBJFace> faces = new ArrayList<>();
        getCollidingFacesInternal(root, targetBB, faces);
        //Scale model faces to match gauge
        return faces.stream().map(f -> f.scale(gaugeScale)).collect(Collectors.toList());
    }

    private void getCollidingFacesInternal(BVHNode parent, IBoundingBox targetBB, List<OBJFace> result) {
        if (parent == null || !parent.bound.intersects(targetBB)) {
            return;
        }

        if (parent.isLeaf()) {
            parent.triangles.stream()
                            .filter(face -> face.getBoundingBox().intersects(targetBB))
                            .forEach(result::add);
        } else {
            getCollidingFacesInternal(parent.left, targetBB, result);
            getCollidingFacesInternal(parent.right, targetBB, result);
        }
    }

    private static IBoundingBox scaleBB(IBoundingBox box, double s) {
        Vec3d a = box.min().scale(1.0 / s);
        Vec3d b = box.max().scale(1.0 / s);

		return IBoundingBox.from(
                new Vec3d(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z)),
                new Vec3d(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z))
        );
    }

    private double getCentroidInAxis(OBJFace tri, Axis axis) {
        return (VecUtil.getByAxis(tri.vertices.get(0), axis) + VecUtil.getByAxis(tri.vertices.get(1), axis) + VecUtil.getByAxis(tri.vertices.get(2), axis)) / 3f;
    }
}