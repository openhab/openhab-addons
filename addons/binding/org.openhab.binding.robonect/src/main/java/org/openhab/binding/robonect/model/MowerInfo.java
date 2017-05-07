package org.openhab.binding.robonect.model;

public class MowerInfo extends RobonectAnswer{
    
    private String name;
    private Status status;
    private Timer timer;
    private Wlan wlan;

    public String getName() {
        return name;
    }
    
    public Status getStatus() {
        return status;
    }

    public Timer getTimer() {
        return timer;
    }

    public Wlan getWlan() {
        return wlan;
    }
}
