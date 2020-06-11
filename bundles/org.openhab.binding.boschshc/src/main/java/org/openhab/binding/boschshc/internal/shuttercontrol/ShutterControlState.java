package org.openhab.binding.boschshc.internal.shuttercontrol;

import com.google.gson.annotations.SerializedName;

public class ShutterControlState {
    @SerializedName("@type")
    public String type = "shutterControlState";

    /**
     * Current open ratio of shutter (0.0 [closed] to 1.0 [open])
     */
    public double level;

    /**
     * Current operation state of shutter
     */
    public OperationState operationState;

    public ShutterControlState() {
        this.level = 0.0;
    }

    public ShutterControlState(double level) {
        this.level = level;
    }
}