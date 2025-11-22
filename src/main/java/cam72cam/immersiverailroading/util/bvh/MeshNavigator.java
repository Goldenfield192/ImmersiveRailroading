package cam72cam.immersiverailroading.util.bvh;

import cam72cam.immersiverailroading.model.StockModel;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.util.VecUtil;
import cam72cam.mod.entity.boundingbox.IBoundingBox;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.model.obj.FaceAccessor;
import cam72cam.mod.model.obj.OBJFace;
import cam72cam.mod.model.obj.Vec2f;
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
            root = buildBVH(rotateToWorld(floor), 0);

            List<OBJFace> collision = new ArrayList<>();
            if (model.collision != null) {
                model.collision.groups().forEach(group -> {
                    FaceAccessor sub = accessor.getSubByGroup(group.name);
                    sub.forEach(a -> collision.add(a.asOBJFace()));
                });
            }
            collisionRoot = buildBVH(rotateToWorld(collision), 0);
        } else {
            OBJFace face1 = new OBJFace();
            OBJFace face2 = new OBJFace();
            EntityRollingStockDefinition def = model.getDefinition();
            Vec3d center = def.passengerCenter.rotateYaw(-90);
            Vec3d v1 = center.add(-def.passengerCompartmentLength, 0, def.passengerCompartmentWidth/2);
            Vec3d v2 = center.add(def.passengerCompartmentLength, 0, def.passengerCompartmentWidth/2);
            Vec3d v3 = center.add(def.passengerCompartmentLength, 0, -def.passengerCompartmentWidth/2);
            Vec3d v4 = center.add(-def.passengerCompartmentLength, 0, -def.passengerCompartmentWidth/2);

            Vec2f emptyUV = new Vec2f(0, 0);

            face1.vertices = Arrays.asList(v1, v2, v3);
            face1.normal = new Vec3d(0, 1, 0);
            face1.uv = Arrays.asList(emptyUV, emptyUV, emptyUV);
            face2.vertices = Arrays.asList(v1, v3, v4);
            face2.normal = new Vec3d(0, 1, 0);
            face2.uv = Arrays.asList(emptyUV, emptyUV, emptyUV);

            root = buildBVH(rotateToWorld(Arrays.asList(face1, face2)), 0);
            collisionRoot = buildBVH(Collections.emptyList(), 0);
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
        
        List<OBJFace> getTrianglesRecursive() {
            List<OBJFace> result = new ArrayList<>(triangles);
            if (left != null) {
                result.addAll(left.getTrianglesRecursive());
            }
            if (right != null) {
                result.addAll(right.getTrianglesRecursive());
            }
            return result;
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

    public boolean atCarEnds(Vec3d pos, Vec3d movement) {
        Vec3d direction = new Vec3d(pos.x - root.bound.center().x, 0, 0);
        return (pos.x >= root.bound.max().x || pos.x <= root.bound.min().x) & direction.dotProduct(movement) >= 0;
    }

    public List<OBJFace> getAllFloorMesh(double scale) {
        return root.getTrianglesRecursive().stream().map(f -> f.scale(scale)).collect(Collectors.toList());
    }

    public List<OBJFace> getFloorMeshWithin(IBoundingBox targetBB, double gaugeScale) {
        return getCollidingMesh(root, targetBB, gaugeScale);
    }

    public List<OBJFace> getCollisionMeshWithin(IBoundingBox targetBB, double gaugeScale) {
        return getCollidingMesh(collisionRoot, targetBB, gaugeScale);
    }

    public List<OBJFace> getCollidingMesh(BVHNode root, IBoundingBox targetBB, double gaugeScale) {
        //Scale back to normal model
        targetBB = scaleBB(targetBB, 1 / gaugeScale);
        List<OBJFace> faces = new ArrayList<>();
        getCollidingMeshInternal(root, targetBB, faces);
        //Scale model faces to match gauge
        return faces.stream().map(f -> f.scale(gaugeScale)).collect(Collectors.toList());
    }

    private void getCollidingMeshInternal(BVHNode parent, IBoundingBox targetBB, List<OBJFace> result) {
        if (parent == null || !parent.bound.intersects(targetBB)) {
            return;
        }

        if (parent.isLeaf()) {
            parent.triangles.stream()
                            .filter(face -> face.getBoundingBox().intersects(targetBB))
                            .forEach(result::add);
        } else {
            getCollidingMeshInternal(parent.left, targetBB, result);
            getCollidingMeshInternal(parent.right, targetBB, result);
        }
    }

    private static IBoundingBox scaleBB(IBoundingBox box, double s) {
        Vec3d a = box.min().scale(s);
        Vec3d b = box.max().scale(s);

		return IBoundingBox.from(
                new Vec3d(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z)),
                new Vec3d(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z))
        );
    }

    private static List<OBJFace> rotateToWorld(List<OBJFace> triangles) {
        return triangles.stream().map(f -> {
            OBJFace face1 = new OBJFace();
            face1.uv = f.uv;
            face1.normal = f.normal.rotateYaw(-90);
            face1.vertices = f.vertices.stream().map(v -> v.rotateYaw(-90)).collect(Collectors.toList());
            return face1;
        }).collect(Collectors.toList());
    }

    private double getCentroidInAxis(OBJFace tri, Axis axis) {
        return (VecUtil.getByAxis(tri.vertices.get(0), axis) + VecUtil.getByAxis(tri.vertices.get(1), axis) + VecUtil.getByAxis(tri.vertices.get(2), axis)) / 3f;
    }
}