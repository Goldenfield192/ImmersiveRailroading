package cam72cam.immersiverailroading.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import cam72cam.mod.entity.boundingbox.IBoundingBox;
import cam72cam.mod.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

/*
 * For now this just wraps the AABB constructor
 * 
 *  In the future we can override the intersects functions for better bounding boxes
 */
public class RealBB implements IBoundingBox {
	private final Vec3d min;
    private final Vec3d center;
	private final Vec3d max;
	private final double front;
	private final double rear;
	private final double width;
	private final double height;
	private final float yaw;
	private final double centerX;
	private final double centerY;
	private final double centerZ;
	private final float[][] heightMap;
	
	public RealBB(double front, double rear, double width, double height, float yaw) {
		this(front, rear, width, height, yaw, null);		
	}
	
	public RealBB(double front, double rear, double width, double height, float yaw, float[][] heightMap) {
		this(front, rear, width, height, yaw, 0, 0, 0, heightMap);
	}
	
	private RealBB(double front, double rear, double width, double height, float yaw, double centerX, double centerY, double centerZ, float[][] heightMap) {
		this.front = front;
		this.rear = rear;
		this.width = width;
		this.height = height;
		this.yaw = yaw;
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
		this.heightMap = heightMap;

        Vec3d frontPos = VecUtil.fromWrongYaw(front, yaw);
		Vec3d rearPos = VecUtil.fromWrongYaw(rear, yaw);

		// width
		Vec3d offsetRight = VecUtil.fromWrongYaw(width / 2, yaw + 90);
		Vec3d offsetLeft = VecUtil.fromWrongYaw(width / 2, yaw - 90);

		double[] x = new double[] {
				frontPos.x + offsetRight.x,
				rearPos.x + offsetRight.x,
				frontPos.x + offsetLeft.x,
				rearPos.x + offsetLeft.x,
		};
		double[] z = new double[] {
				frontPos.z + offsetRight.z,
				rearPos.z + offsetRight.z,
				frontPos.z + offsetLeft.z,
				rearPos.z + offsetLeft.z,
		};

		double xMin = x[0];
		double xMax = x[0];
		double zMin = z[0];
		double zMax = z[0];

		for (int i = 1; i < x.length; i++) {
			xMin = Math.min(xMin, x[i]);
			xMax = Math.max(xMax, x[i]);
			zMin = Math.min(zMin, z[i]);
			zMax = Math.max(zMax, z[i]);
		}

		this.min = new Vec3d(xMin + centerX, centerY, zMin + centerZ);
        this.center = new Vec3d(centerX, centerY, centerZ);
		this.max = new Vec3d(xMax + centerX, centerY + height, zMax + centerZ);
	}

	@Override
	public Vec3d min() {
		return min;
	}

    @Override
    public Vec3d center() {
        return center;
    }

    @Override
	public Vec3d max() {
		return max;
	}

	@Override
	public RealBB clone() {
		return new RealBB(front, rear, width, height, yaw, centerX, centerY, centerZ, heightMap);
	}
	@Override
	public RealBB contract(Vec3d val) {
		double front = this.front;
		double rear = this.rear;
		double width = this.width;
		double height = this.height;
		double centerY = this.centerY;
		if (val.x > 0) {
			front -= val.x;
		} else {
			rear -= val.x;
		}
		
		if (val.y > 0) {
			height -= val.y;
		} else {
			centerY -= val.y;
		}
		
		width -= val.z;
		
		return new RealBB(front, rear, width, height, yaw, centerX, centerY, centerZ, heightMap);
	}
	@Override
	public RealBB expand(Vec3d val) {
		double front = this.front;
		double rear = this.rear;
		double width = this.width;
		double height = this.height;
		double centerY = this.centerY;
		if (val.x > 0) {
			front += val.x;
		} else {
			rear += val.x;
		}

		if (val.y > 0) {
			height += val.y;
		} else {
			centerY += val.y;
		}

		width += val.z;

		return new RealBB(front, rear, width, height, yaw, centerX, centerY, centerZ, heightMap);
	}
	@Override
	public RealBB grow(Vec3d val) {
		return new RealBB(
				front+val.x, rear+val.x,
				width+val.z + val.z, height+val.y,
				yaw,
				centerX, centerY+val.y, centerZ,
				heightMap);
	}

	@Override
	public RealBB offset(Vec3d val) {
		return new RealBB(front, rear, width, height, yaw, centerX+val.x, centerY+val.y, centerZ+val.z, heightMap);
	}
	@Override
	public Vec3d adjustMovement(IBoundingBox other, Vec3d movement) {
		// 获取 A 的当前边界框（世界坐标）
		Vec3d aMin = other.min();
		Vec3d aMax = other.max();
		double aHeight = aMax.y - aMin.y;

		// 最终累计的移动向量（世界坐标）
		Vec3d totalMove = Vec3d.ZERO;

		// ---------- 1. 处理 Y 轴移动（垂直） ----------
		double moveY = movement.y;
		if (moveY != 0) {
			Vec3d newMin = new Vec3d(aMin.x, aMin.y + moveY, aMin.z);
			Vec3d newMax = new Vec3d(aMax.x, aMax.y + moveY, aMax.z);
			Pair<Boolean, Double> yCheck = intersectsAt(newMin, newMax, true);
			if (yCheck.getLeft()) {
//				// 发生碰撞，需要调整移动量
//				if (moveY < 0) {
//					// 向下移动：A 的底部应贴合 B 的顶部（返回的 Y 值）
//					double newBottom = yCheck.getRight();
//					moveY = newBottom - aMin.y;
//				} else {
//					// 向上移动：A 的顶部应贴合 B 的底部（centerY）
//					double newTop = this.centerY; // B 的底部 Y 坐标
//					moveY = newTop - aMax.y;
//				}
			}
			totalMove = totalMove.add(0, moveY, 0);
			// 更新 A 的边界框（仅 Y 变化）
			aMin = aMin.add(0, moveY, 0);
			aMax = aMax.add(0, moveY, 0);
		}

		// 标记 A 是否站在 B 的顶部（用于水平移动后重新调整 Y）
		boolean onGround = false;
		// 检查当前 A 的底部是否正好接触 B 的顶部（允许微小误差）
		Pair<Boolean, Double> groundCheck = intersectsAt(aMin, aMax, true);
		if (groundCheck.getLeft()) {
			double bTop = groundCheck.getRight();
			if (Math.abs(aMin.y - bTop) < 1e-4) {
				onGround = true;
			}
		}

		// ---------- 2. 处理水平移动（在 B 的局部坐标系中分解） ----------
		// 计算 B 的局部 X 轴（向前）和 Z 轴（向右）的世界方向
		double yawRad = Math.toRadians(this.yaw);
		Vec3d localX = new Vec3d(Math.sin(yawRad), 0, Math.cos(yawRad)); // 注意：与原代码中 fromWrongYaw 行为一致
		Vec3d localZ = new Vec3d(Math.cos(yawRad), 0, -Math.sin(yawRad)); // yaw+90 方向

		// 将水平速度分解到 B 的局部坐标系
		Vec3d horizMove = new Vec3d(movement.x, 0, movement.z);
		double vx_local = horizMove.dotProduct(localX);
		double vz_local = horizMove.dotProduct(localZ);

		// 分别处理局部 X 和局部 Z 轴移动
		double[] locals = {vx_local, vz_local};
		Vec3d[] axes = {localX, localZ};

		for (int i = 0; i < 2; i++) {
			double v = locals[i];
			if (v == 0) continue;

			Vec3d axis = axes[i];
			Vec3d delta = axis.scale(v);
			Vec3d newMin = aMin.add(delta);
			Vec3d newMax = aMax.add(delta);

			if (intersectsAt(newMin, newMax, true).getLeft()) {
				// 该方向碰撞，取消此分量移动
				continue;
			}

			// 无碰撞，接受移动
			totalMove = totalMove.add(delta);
			aMin = newMin;
			aMax = newMax;

			// 如果 A 站在 B 上，水平移动后需要重新贴合地形高度
			if (onGround) {
				Pair<Boolean, Double> terrain = intersectsAt(aMin, aMax, true);
				if (terrain.getLeft()) {
					double newBottom = terrain.getRight();
					double deltaY = newBottom - aMin.y;
					if (deltaY != 0) {
						totalMove = totalMove.add(0, deltaY, 0);
						aMin = aMin.add(0, deltaY, 0);
						aMax = aMax.add(0, deltaY, 0);
					}
				} else {
					// 水平移动后不再与 B 重叠，失去地面接触
					onGround = false;
				}
			}
		}

		return totalMove;
	}

	@Override
	public double calculateXOffset(IBoundingBox other, double offsetX) {
		return 0;
	}
	@Override
    public double calculateYOffset(IBoundingBox other, double offsetY) {
		double hack = 0.05;
		Double intersect = intersectsAt(other.min().subtract(hack, 0, hack), other.max().add(hack, 0, hack), true).getRight();
		double minY = other.min().y;
		return MathUtil.clamp(intersect - minY, -0.1, 0.1);
	}
	@Override
	public double calculateZOffset(IBoundingBox other, double offsetZ) {
		return 0;
	}

	public Pair<Boolean, Double> intersectsAt(Vec3d min, Vec3d max, boolean useHeightmap) {
		if (!(this.min.x < max.x && this.max.x > min.x && this.min.y < max.y && this.max.y > min.y && this.min.z < max.z && this.max.z > min.z)) {
			return Pair.of(false, min.y);
		}
		
		double actualYMin = this.centerY;
		double actualYMax = this.centerY + this.height;
		if (! (actualYMin < max.y && actualYMax > min.y)) {
			return Pair.of(false, min.y);
		}
		
		Rectangle2D otherRect = new Rectangle2D.Double(min.x, min.z, 0, 0);
		if (min.x == max.x && min.z == max.z) {
			otherRect.add(max.x+0.2, max.z + 0.2);
		} else {
			otherRect.add(max.x, max.z);
		}

		Rectangle2D myRect = new Rectangle2D.Double(
				this.rear + this.centerX,
				-this.width/2 + this.centerZ,
				this.front - this.rear,
				this.width);

		AffineTransform otherTransform = new AffineTransform();
		otherTransform.rotate(Math.toRadians(180-yaw+90), this.centerX, this.centerZ);
		Shape otherShape = otherTransform.createTransformedShape(otherRect);

		if (!otherShape.intersects(myRect)) {
			return Pair.of(false, min.y);
		}
		if (this.heightMap != null && useHeightmap) {
			int xRes = this.heightMap.length-1;
			int zRes = this.heightMap[0].length-1;
			
			double length = this.front-this.rear;
			
			actualYMin = this.centerY;
			actualYMax = this.centerY;

			Rectangle2D bds = otherShape.getBounds2D();

			double px = bds.getMinX() - (this.centerX - length/2);
			double pz = bds.getMinY() - (this.centerZ - width/2);
			double Px = bds.getMaxX() - (this.centerX - length/2);
			double Pz = bds.getMaxY() - (this.centerZ - width/2);

			double cx = MathUtil.clamp(px, 0, length);
			double cz = MathUtil.clamp(pz, 0, width);
			double Cx = MathUtil.clamp(Px, 0, length);
			double Cz = MathUtil.clamp(Pz, 0, width);

			cx = (cx/length*xRes);
			cz = (cz/width*zRes);
			Cx = (Cx/length*xRes);
			Cz = (Cz/width*zRes);
			
			for (int x = (int) cx; x < (int)Cx; x++) {
				for (int z = (int) cz; z < (int)Cz; z++) {
					actualYMax = Math.max(actualYMax, this.centerY + this.height * this.heightMap[x][z]);
				}
			}

			return Pair.of(actualYMin < max.y && actualYMax > min.y, actualYMax);
		}
		
		return Pair.of(true, this.max.y);
	}
	@Override
	public boolean intersects(Vec3d min, Vec3d max) {
		return intersectsAt(min, max, true).getLeft();
	}

    @Override
    public IBoundingBox expandToFit(IBoundingBox other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean intersectsSegment(Vec3d start, Vec3d end) {
        return false;
    }

    @Override
	public boolean contains(Vec3d vec) {
		return this.intersectsAt(vec, vec, false).getLeft();
	}
	public RealBB withHeightMap(float[][] heightMap) {
		return new RealBB(front, rear, width, height, yaw, centerX, centerY, centerZ, heightMap);
	}
}
