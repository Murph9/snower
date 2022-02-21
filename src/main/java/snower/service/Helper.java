package snower.service;

import java.util.ArrayList;
import java.util.List;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

import snower.base.Main;

public class Helper {
    public static RayResult findClosestResult(final Vector3f from, float rayLength, PhysicsCollisionObject self, boolean ignoreGhost) {
        Vector3f diff = new Vector3f(0, - rayLength, 0);
        Vector3f to = from.add(diff);
        List<PhysicsRayTestResult> results = new ArrayList<>();
        Main.physicsSpace.rayTest(from, to, results);
        for (PhysicsRayTestResult result : results) {
            if (result.getCollisionObject().getObjectId() == self.getObjectId())
                continue; // no self collision please
            if (ignoreGhost && !(result.getCollisionObject() instanceof PhysicsRigidBody))
                continue;
            return new RayResult(from.add(diff.mult(result.getHitFraction())), result.getHitNormalLocal(), Math.min(rayLength, result.getHitFraction() * rayLength));
        }

        return new RayResult(null, null, -1);
    }

    public static Vector3f findNormalAtPosDown(final Vector3f from, float rayLength, PhysicsCollisionObject self) {
        return findClosestResult(from, rayLength, self, true).normal;
    }

    public static Vector3f findFirstPosDown(final Vector3f from, float rayLength, PhysicsCollisionObject self) {
        return findClosestResult(from, rayLength, self, true).pos;
    }

    public static float findHeight(final Vector3f from, float rayLength, PhysicsCollisionObject self) {
        return findClosestResult(from, rayLength, self, true).distance;
    }

    public static Vector3f[] getMinMaxX(Vector3f ...points) {
        if (points == null || points.length < 1)
            return new Vector3f[0];
        if (points.length == 1)
            return points;

        Vector3f minX = null;
        Vector3f maxX = null;

        for (Vector3f v: points) {
            if (minX == null || v.x < minX.x)
                minX = v;
            if (maxX == null || v.x > maxX.x)
                maxX = v;
        }

        return new Vector3f[] {
            minX, maxX
        };
    }

    public static Vector3f getClosestPos(Vector3f start, Vector3f end, Vector3f point) {
        var dir = point.subtract(start);
        var result = dir.project(end.subtract(start)).add(start);

        var distance = start.distance(end);
        if (end.distance(result) > distance) { // before the start of the rail
            return start;
        }

        if (start.distance(result) > distance) { // after the end of the rail
            return end;
        }

        return result;
    }

    public static Vector3f getXZNormalized(Vector3f in) {
        return new Vector3f(in.x, 0, in.z).normalize();
    }
}
