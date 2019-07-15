package org.openhab.binding.hydrawise.internal.api.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sensor {

    @SerializedName("input")
    @Expose
    private Integer input;
    @SerializedName("type")
    @Expose
    private Integer type;
    @SerializedName("mode")
    @Expose
    private Integer mode;
    @SerializedName("timer")
    @Expose
    private Integer timer;
    @SerializedName("offtimer")
    @Expose
    private Integer offtimer;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("offlevel")
    @Expose
    private Integer offlevel;
    @SerializedName("active")
    @Expose
    private Integer active;
    @SerializedName("relays")
    @Expose
    private List<Object> relays = null;

    /**
     * @return
     */
    public Integer getInput() {
        return input;
    }

    /**
     * @param input
     */
    public void setInput(Integer input) {
        this.input = input;
    }

    /**
     * @return
     */
    public Integer getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * @return
     */
    public Integer getMode() {
        return mode;
    }

    /**
     * @param mode
     */
    public void setMode(Integer mode) {
        this.mode = mode;
    }

    /**
     * @return
     */
    public Integer getTimer() {
        return timer;
    }

    /**
     * @param timer
     */
    public void setTimer(Integer timer) {
        this.timer = timer;
    }

    /**
     * @return
     */
    public Integer getOfftimer() {
        return offtimer;
    }

    /**
     * @param offtimer
     */
    public void setOfftimer(Integer offtimer) {
        this.offtimer = offtimer;
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
    public Integer getOfflevel() {
        return offlevel;
    }

    /**
     * @param offlevel
     */
    public void setOfflevel(Integer offlevel) {
        this.offlevel = offlevel;
    }

    /**
     * @return
     */
    public Integer getActive() {
        return active;
    }

    /**
     * @param active
     */
    public void setActive(Integer active) {
        this.active = active;
    }

    /**
     * @return
     */
    public List<Object> getRelays() {
        return relays;
    }

    /**
     * @param relays
     */
    public void setRelays(List<Object> relays) {
        this.relays = relays;
    }

}