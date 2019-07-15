package org.openhab.binding.hydrawise.internal.api.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocalScheduleResponse {

    @SerializedName("running")
    @Expose
    private List<Running> running = null;
    @SerializedName("relays")
    @Expose
    private List<Relay> relays = null;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("time")
    @Expose
    private Integer time;

    /**
     * @return
     */
    public List<Running> getRunning() {
        return running;
    }

    /**
     * @param running
     */
    public void setRunning(List<Running> running) {
        this.running = running;
    }

    /**
     * @return
     */
    public List<Relay> getRelays() {
        return relays;
    }

    /**
     * @param relays
     */
    public void setRelays(List<Relay> relays) {
        this.relays = relays;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    public Integer getTime() {
        return time;
    }

    /**
     * @param time
     */
    public void setTime(Integer time) {
        this.time = time;
    }
}
