package snower.base;

import java.util.ArrayList;
import java.util.List;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

public class Helper {
    public static Vector3f findNormalAtPosDown(final Vector3f from, float rayLength, PhysicsCollisionObject self) {
        Vector3f to = new Vector3f(from.x, from.y - rayLength, from.z);
        List<PhysicsRayTestResult> results = new ArrayList<>();
        Main.physicsSpace.rayTest(from, to, results);
        for (PhysicsRayTestResult result : results) {
            if (result.getCollisionObject().getObjectId() == self.getObjectId())
                continue; // no self collision please
            if (!(result.getCollisionObject() instanceof PhysicsRigidBody))
                continue;
            return result.getHitNormalLocal();
        }

        return null;
    }

    public static Vector3f findFirstPosDown(final Vector3f from, float rayLength, PhysicsCollisionObject self) {
        Vector3f to = new Vector3f(from.x, from.y - rayLength, from.z);
        List<PhysicsRayTestResult> results = new ArrayList<>();
        Main.physicsSpace.rayTest(from, to, results);
        for (PhysicsRayTestResult result : results) {
            if (result.getCollisionObject().getObjectId() == self.getObjectId())
                continue; // no self collision please
            if (!(result.getCollisionObject() instanceof PhysicsRigidBody))
                continue;
            return from.add(to.mult(result.getHitFraction()));
        }

        return null;
    }

    public static float findHeight(final Vector3f from, float rayLength, PhysicsCollisionObject self) {
        Vector3f to = new Vector3f(from.x, from.y - rayLength, from.z);
        List<PhysicsRayTestResult> results = new ArrayList<>();
        Main.physicsSpace.rayTest(from, to, results);
        for (PhysicsRayTestResult result : results) {
            if (result.getCollisionObject().getObjectId() == self.getObjectId())
                continue; // no self collision please
            if (!(result.getCollisionObject() instanceof PhysicsRigidBody))
                continue;

            float intersection = result.getHitFraction() * rayLength;
            return Math.min(rayLength, intersection);
        }

        return -1;
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
}
