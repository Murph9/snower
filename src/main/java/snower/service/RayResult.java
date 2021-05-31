package snower.service;

import com.jme3.math.Vector3f;

public class RayResult {
    public final Vector3f pos;
    public final Vector3f normal;
    public final float distance;

    public RayResult(Vector3f pos, Vector3f normal, float distance) {
        this.pos = pos;
        this.normal = normal;
        this.distance = distance;
    }
}