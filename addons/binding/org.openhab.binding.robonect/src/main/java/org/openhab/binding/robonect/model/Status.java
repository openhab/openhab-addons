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

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public void setStatus(MowerStatus status) {
        this.status = status;
    }

    public void setMode(MowerMode mode) {
        this.mode = mode;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}
