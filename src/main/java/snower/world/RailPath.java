package snower.world;

import java.io.IOException;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Vector3f;

import snower.service.Helper;

public class RailPath implements Savable {
    public Vector3f start;
    public Vector3f end;

    public RailPath(Vector3f start, Vector3f end) {
        this.start = start;
        this.end = end;
    }

    public float totalDistance() {
        return end.distance(start);
    }

    public Vector3f getRailDirection() {
        return end.subtract(start).normalizeLocal();
    }

    public Vector3f getClosestPos(Vector3f pos) {
        return Helper.getClosestPos(start, end, pos);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);

        capsule.write(start, "start", new Vector3f());
        capsule.write(end, "end", new Vector3f());
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);

        start = (Vector3f)capsule.readSavable("start", new Vector3f());
        end = (Vector3f)capsule.readSavable("end", new Vector3f());
    }

    @Override
    public String toString() {
        return "RailPath["+this.start+" | " + this.end + "]";
    }
}
