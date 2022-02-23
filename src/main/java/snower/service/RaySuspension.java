package snower.service;

import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.FastMath;

public class RaySuspension {
    private final PhysicsRigidBody body;
    private final float stiffness;
    private final float damping;
    
    public RaySuspension(PhysicsRigidBody body, float stiffness, float damping) {
        this.body = body;
        this.stiffness = stiffness;
        this.damping = damping;
    }

    public float calcSusResult(float distance, float rayLength, float contactVelocity) {
        float denominator = distance;
        float relative_vel = -body.getLinearVelocity().y - contactVelocity;

        float inv = -1f / denominator;
        relative_vel = relative_vel * inv;
        
        var length_diff = (rayLength - distance);
        float force = stiffness * length_diff;
        
        var damping_normalized = damping * 2.0f * FastMath.sqrt(stiffness);
        force -= relative_vel * damping_normalized;

		return Math.max(force, 0); //no negative spring force pls
	}
}
