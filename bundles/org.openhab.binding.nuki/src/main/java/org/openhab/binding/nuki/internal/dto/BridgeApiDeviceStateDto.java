package org.openhab.binding.nuki.internal.dto;

public class BridgeApiDeviceStateDto {
    private int mode;
    private int state;
    private String stateName;
    private boolean batteryCritical;
    private Boolean batteryCharging;
    private Integer batteryChargeState;
    private Boolean keypadBatteryCritical;
    private Integer doorsensorState;
    private String doorsensorStateName;
    private String ringActionTimestamp;
    private Boolean ringactionState;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public boolean isBatteryCritical() {
        return batteryCritical;
    }

    public void setBatteryCritical(boolean batteryCritical) {
        this.batteryCritical = batteryCritical;
    }

    public Boolean getBatteryCharging() {
        return batteryCharging;
    }

    public void setBatteryCharging(Boolean batteryCharging) {
        this.batteryCharging = batteryCharging;
    }

    public Integer getBatteryChargeState() {
        return batteryChargeState;
    }

    public void setBatteryChargeState(Integer batteryChargeState) {
        this.batteryChargeState = batteryChargeState;
    }

    public Boolean getKeypadBatteryCritical() {
        return keypadBatteryCritical;
    }

    public void setKeypadBatteryCritical(Boolean keypadBatteryCritical) {
        this.keypadBatteryCritical = keypadBatteryCritical;
    }

    public Integer getDoorsensorState() {
        return doorsensorState;
    }

    public void setDoorsensorState(Integer doorsensorState) {
        this.doorsensorState = doorsensorState;
    }

    public String getRingActionTimestamp() {
        return ringActionTimestamp;
    }

    public void setRingActionTimestamp(String ringActionTimestamp) {
        this.ringActionTimestamp = ringActionTimestamp;
    }

    public Boolean getRingactionState() {
        return ringactionState;
    }

    public void setRingactionState(Boolean ringactionState) {
        this.ringactionState = ringactionState;
    }

    public String getDoorsensorStateName() {
        return doorsensorStateName;
    }

    public void setDoorsensorStateName(String doorsensorStateName) {
        this.doorsensorStateName = doorsensorStateName;
    }
}
