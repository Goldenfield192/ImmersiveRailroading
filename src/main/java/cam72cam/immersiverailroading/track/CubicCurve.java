package cam72cam.immersiverailroading.track;

import cam72cam.immersiverailroading.library.TrackSmoothing;
import cam72cam.immersiverailroading.util.VecUtil;
import cam72cam.mod.ModCore;
import cam72cam.mod.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;
import util.Matrix4;

import java.util.ArrayList;
import java.util.List;

public class CubicCurve {
    public final Vec3d p1;
    public final Vec3d ctrl1;
    public final Vec3d ctrl2;
    public final Vec3d p2;

    //http://spencermortensen.com/articles/bezier-circle/
    public final static double c = 0.55191502449;

    public CubicCurve(Vec3d p1, Vec3d ctrl1, Vec3d ctrl2, Vec3d p2) {
        this.p1 = p1;
        this.ctrl1 = ctrl1;
        this.ctrl2 = ctrl2;
        this.p2 = p2;
    }

    public static CubicCurve circle(int radius, float degrees) {
        float cRadScale = degrees / 90;
        Vec3d p1 = new Vec3d(0, 0, radius);
        Vec3d ctrl1 = new Vec3d(cRadScale * c * radius, 0, radius);
        Vec3d ctrl2 = new Vec3d(radius, 0, cRadScale * c * radius);
        Vec3d p2 = new Vec3d(radius, 0, 0);

        Matrix4 quart = new Matrix4();
        quart.rotate(Math.toRadians(-90+degrees), 0, 1, 0);

        return new CubicCurve(p1, ctrl1, quart.apply(ctrl2), quart.apply(p2)).apply(new Matrix4().translate(0, 0, -radius));
    }

    public CubicCurve apply(Matrix4 mat) {
        return new CubicCurve(
                mat.apply(p1),
                mat.apply(ctrl1),
                mat.apply(ctrl2),
                mat.apply(p2)
        );
    }

    public CubicCurve reverse() {
        return new CubicCurve(p2, ctrl2, ctrl1, p1);
    }

    public CubicCurve truncate(double t) {
        Vec3d midpoint = this.ctrl1.add(this.ctrl2).scale(t);
        Vec3d ctrl1 = p1.add(this.ctrl1).scale(t);
        Vec3d ctrl2 = p2.add(this.ctrl2).scale(t);

        Vec3d temp = ctrl2.add(midpoint).scale(t);
        ctrl2 = ctrl1.add(midpoint).scale(t);
        midpoint = ctrl2.add(temp).scale(t);
        return new CubicCurve(
                p1,
                ctrl1,
                ctrl2,
                midpoint
        );
    }

    public Pair<CubicCurve, CubicCurve> split(double t) {
        return Pair.of(this.truncate(t), this.reverse().truncate(1-t));
    }

    public Vec3d position(double t) {
        //WILL CAUSE ABOUT 2850% PERFORMANCE DECREASE
//        Vec3d pt = Vec3d.ZERO;
//        pt = pt.add(p1.		scale(1 * Math.pow(1-t, 3) * Math.pow(t, 0)));
//        pt = pt.add(ctrl1.	scale(3 * Math.pow(1-t, 2) * Math.pow(t, 1)));
//        pt = pt.add(ctrl2.	scale(3 * Math.pow(1-t, 1) * Math.pow(t, 2)));
//        pt = pt.add(p2.		scale(1 * Math.pow(1-t, 0) * Math.pow(t, 3)));
//
//        return pt;
        double u = 1 - t;
        double x = p1.x * u * u * u + ctrl1.x * 3 * u * u * t + ctrl2.x * 3 * u * t * t + p2.x * t * t * t;
        double y = p1.y * u * u * u + ctrl1.y * 3 * u * u * t + ctrl2.y * 3 * u * t * t + p2.y * t * t * t;
        double z = p1.z * u * u * u + ctrl1.z * 3 * u * u * t + ctrl2.z * 3 * u * t * t + p2.z * t * t * t;
        return new Vec3d(x, y, z);
    }

    public Vec3d derivative(double t){
        //WILL CAUSE 1000%+ decrease if using Vec3d
//        Vec3d pt = Vec3d.ZERO;
//        double u = 1 - t;
//        pt = pt.add(ctrl1.add(p1   .scale(-1)).scale(3 * u * u));
//        pt = pt.add(ctrl2.add(ctrl1.scale(-1)).scale(6 * t * u));
//        pt = pt.add(p2   .add(ctrl2.scale(-1)).scale(3 * t * t));
//        return pt;
        double u = 1 - t;
        double d1 = 3 * u * u;
        double d2 = 6 * u * t;
        double d3 = 3 * t * t;

        double dx = d1 * (ctrl1.x - p1.x) + d2 * (ctrl2.x - ctrl1.x) + d3 * (p2.x - ctrl2.x);
        double dy = d1 * (ctrl1.y - p1.y) + d2 * (ctrl2.y - ctrl1.y) + d3 * (p2.y - ctrl2.y);
        double dz = d1 * (ctrl1.z - p1.z) + d2 * (ctrl2.z - ctrl1.z) + d3 * (p2.z - ctrl2.z);

        return new Vec3d(dx, dy, dz);
    }

    public double length(double precision){ //recommend 4
        double segments = Math.pow(10, precision);
        double length = 0.0;
        double tStep = 1.0 / segments;
        Vec3d prevDeriv = derivative(0);
        double prevSpeed = prevDeriv.length();

        for (int i = 1; i <= segments; i++) {
            double t = i * tStep;
            Vec3d deriv = derivative(t);
            double speed = deriv.length();

            length += (prevSpeed + speed) * tStep / 2.0;
            prevSpeed = speed;
        }
        return length;
    }

    public List<Vec3d> toList(double stepSize) {
//        {
//            List<Vec3d> res = new ArrayList<>();
//            List<Vec3d> resRev = new ArrayList<>();
//            res.add(p1);
//            if (p1.equals(p2)) {
//                return res;
//            }
//
//            resRev.add(p2);
//            double precision = 5;
//            double stepSizeSquared = stepSize * stepSize;
//
//            double t = 0;
//            while (t <= 0.5) {
//                for (double i = 1; i < precision; i++) {
//                    Vec3d prev = res.get(res.size() - 1);
//
//                    double delta = (Math.pow(10, -i));
//
//                    for (; t < 1 + delta; t += delta) {
//                        Vec3d pos = position(t);
//                        if (pos.distanceToSquared(prev) > stepSizeSquared) {
//                            // We passed it, just barely
//                            t -= delta;
//                            break;
//                        }
//                    }
//                }
//                res.add(position(t));
//            }
//
//            double lt = t;
//            t = 1;
//
//            while (t > lt) {
//                for (double i = 1; i < precision; i++) {
//                    Vec3d prev = resRev.get(resRev.size() - 1);
//
//                    double delta = (Math.pow(10, -i));
//
//                    for (; t > lt - delta; t -= delta) {
//                        Vec3d pos = position(t);
//                        if (pos.distanceToSquared(prev) > stepSizeSquared) {
//                            // We passed it, just barely
//                            t += delta;
//                            break;
//                        }
//                    }
//                }
//                if (t > lt) {
//                    resRev.add(position(t));
//                }
//            }
//            Collections.reverse(resRev);
//            res.addAll(resRev);
//            return res;
//        }
        long time = System.nanoTime();

        List<Vec3d> result = new ArrayList<>();
        double length = this.length(4);
        result.add(p1);
        if(p1.equals(p2)){
            return result;
        }

        double segments = length / (0.01 * stepSize);
        double l = 0.0;
        double tStep = 1.0 / segments;
        Vec3d prevDeriv = derivative(0);
        double prevSpeed = prevDeriv.length();

        int count = 0;

        for (int i = 1; i <= segments; i++) {
            double t = i * tStep;
            Vec3d deriv = derivative(t);
            double speed = deriv.length();

            l += (prevSpeed + speed) * tStep / 2.0;
            if(l >= count * stepSize){
                result.add(position(t));
                count ++;
            }
            prevSpeed = speed;
        }

        if (result.size() < Math.round(length / stepSize)) {//For some precision reason the last point is skipped, add it back
           result.add(p2);
        }
        ModCore.info(String.valueOf(System.nanoTime() - time));

        return result;
    }

    public float angleStop() {
        return VecUtil.toYaw(p2.subtract(ctrl2));
    }

    public float angleStart() {
        return VecUtil.toYaw(p1.subtract(ctrl1)) + 180;
    }

    public List<CubicCurve> subsplit(int maxSize) {
        List<CubicCurve> res = new ArrayList<>();
        if (p1.distanceTo(p2) <= maxSize) {
            res.add(this);
        } else {
            res.addAll(this.truncate(0.5).subsplit(maxSize));
            res.addAll(this.reverse().truncate(0.5).reverse().subsplit(maxSize));
        }
        return res;
    }


    public CubicCurve linearize(TrackSmoothing smoothing) {
        double start = p1.distanceTo(ctrl1);
        double middle = ctrl1.distanceTo(ctrl2);
        double end = ctrl2.distanceTo(p2);

        double lengthGuess = start + middle + end;
        double height = p2.y - p1.y;

        switch (smoothing) {
            case NEITHER:
                return new CubicCurve(
                        p1,
                        ctrl1.add(0, (start / lengthGuess) * height, 0),
                        ctrl2.add(0, -(end / lengthGuess) * height, 0),
                        p2
                );
            case NEAR:
                return new CubicCurve(
                        p1,
                        ctrl1,
                        ctrl2.add(0, -(end / (middle + end)) * height, 0),
                        p2
                );
            case FAR:
                return new CubicCurve(
                        p1,
                        ctrl1.add(0, (start / (start + middle)) * height, 0),
                        ctrl2,
                        p2
                );
            case BOTH: default:
                return this;
        }
    }
}
