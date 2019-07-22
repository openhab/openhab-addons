package org.openhab.binding.hydrawise.internal.api.model;

public class Running {

    private String relay;

    private String relayId;

    private Integer timeLeft;

    private String run;

    /**
     * @return
     */
    public String getRelay() {
        return relay;
    }

    /**
     * @param relay
     */
    public void setRelay(String relay) {
        this.relay = relay;
    }

    /**
     * @return
     */
    public Integer getRelayId() {
        return new Integer(relayId);
    }

    /**
     * @param relayId
     */
    public void setRelayId(String relayId) {
        this.relayId = relayId;
    }

    /**
     * @return
     */
    public Integer getTimeLeft() {
        return timeLeft;
    }

    /**
     * @param timeLeft
     */
    public void setTimeLeft(Integer timeLeft) {
        this.timeLeft = timeLeft;
    }

    /**
     * @return
     */
    public String getRun() {
        return run;
    }

    /**
     * @param run
     */
    public void setRun(String run) {
        this.run = run;
    }

}