package org.openhab.binding.hydrawise.internal.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Running {

    @SerializedName("relay")
    @Expose
    private String relay;
    @SerializedName("relay_id")
    @Expose
    private String relayId;
    @SerializedName("time_left")
    @Expose
    private Integer timeLeft;
    @SerializedName("run")
    @Expose
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