
package org.openhab.binding.wlanthermo.internal.api.mini.builtin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Pit {

    @SerializedName("enabled")
    @Expose
    private Boolean enabled;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("setpoint")
    @Expose
    private Integer setpoint;
    @SerializedName("current")
    @Expose
    private Integer current;
    @SerializedName("control_out")
    @Expose
    private Integer controlOut;
    @SerializedName("ch")
    @Expose
    private Integer ch;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("open_lid")
    @Expose
    private String openLid;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Pit() {
    }

    /**
     * 
     * @param current
     * @param setpoint
     * @param ch
     * @param openLid
     * @param controlOut
     * @param type
     * @param enabled
     * @param timestamp
     */
    public Pit(Boolean enabled, String timestamp, Integer setpoint, Integer current, Integer controlOut, Integer ch, String type, String openLid) {
        super();
        this.enabled = enabled;
        this.timestamp = timestamp;
        this.setpoint = setpoint;
        this.current = current;
        this.controlOut = controlOut;
        this.ch = ch;
        this.type = type;
        this.openLid = openLid;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Pit withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Pit withTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Integer getSetpoint() {
        return setpoint;
    }

    public void setSetpoint(Integer setpoint) {
        this.setpoint = setpoint;
    }

    public Pit withSetpoint(Integer setpoint) {
        this.setpoint = setpoint;
        return this;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Pit withCurrent(Integer current) {
        this.current = current;
        return this;
    }

    public Integer getControlOut() {
        return controlOut;
    }

    public void setControlOut(Integer controlOut) {
        this.controlOut = controlOut;
    }

    public Pit withControlOut(Integer controlOut) {
        this.controlOut = controlOut;
        return this;
    }

    public Integer getCh() {
        return ch;
    }

    public void setCh(Integer ch) {
        this.ch = ch;
    }

    public Pit withCh(Integer ch) {
        this.ch = ch;
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Pit withType(String type) {
        this.type = type;
        return this;
    }

    public String getOpenLid() {
        return openLid;
    }

    public void setOpenLid(String openLid) {
        this.openLid = openLid;
    }

    public Pit withOpenLid(String openLid) {
        this.openLid = openLid;
        return this;
    }

}
