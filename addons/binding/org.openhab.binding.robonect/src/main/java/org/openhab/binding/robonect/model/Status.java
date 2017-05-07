package org.openhab.binding.robonect.model;

public class Status {

    private int battery;
    private int duration;
    private int hours;
    private MowerStatus status;
    private MowerMode mode;
    private boolean stopped;

    public int getBattery() {
        return battery;
    }

    public int getDuration() {
        return duration;
    }

    public int getHours() {
        return hours;
    }

    public MowerStatus getStatus() {
        return status;
    }

    public boolean isStopped() {
        return stopped;
    }

    public MowerMode getMode() {
        return mode;
    }
}
