package org.openhab.binding.hyperion.internal.protocol.request;

import org.openhab.binding.hyperion.internal.protocol.transform.Transform;

public class TransformCommand extends HyperionCommand {

    private final static String NAME = "transform";
    private Transform transform;

    public TransformCommand(Transform transform) {
        super(NAME);
        setTransform(transform);
    }

    public Transform getTransform() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

}
