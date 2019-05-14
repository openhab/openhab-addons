package org.openhab.binding.somfymylink.internal;

public class SomfyMyLinkShade {

    private String targetID;
    private String name;

    public String getTargetID() {
        return targetID.replace('.', '-');
    }

    public String getName() {
        return name;
    }

}
