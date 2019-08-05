package org.openhab.binding.opensprinkler.internal.model;

public class StationProgram {
    public final long remainingWaterTime;

    public StationProgram(int remainingWaterTime) {
        this.remainingWaterTime = remainingWaterTime;
    }
}
