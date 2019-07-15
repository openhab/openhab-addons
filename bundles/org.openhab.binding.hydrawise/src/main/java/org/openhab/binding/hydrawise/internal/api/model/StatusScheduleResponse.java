package org.openhab.binding.hydrawise.internal.api.model;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StatusScheduleResponse extends Response {

    @SerializedName("controller_id")
    @Expose
    private Integer controllerId;
    @SerializedName("customer_id")
    @Expose
    private Integer customerId;
    @SerializedName("user_id")
    @Expose
    private Integer userId;
    @SerializedName("nextpoll")
    @Expose
    private Integer nextpoll;
    @SerializedName("sensors")
    @Expose
    private List<Sensor> sensors = new LinkedList<Sensor>();
    @SerializedName("running")
    @Expose
    private List<Running> running = new LinkedList<Running>();
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("obs_rain")
    @Expose
    private String obsRain;
    @SerializedName("obs_rain_week")
    @Expose
    private String obsRainWeek;
    @SerializedName("obs_maxtemp")
    @Expose
    private String obsMaxtemp;
    @SerializedName("obs_rain_upgrade")
    @Expose
    private Integer obsRainUpgrade;
    @SerializedName("obs_rain_text")
    @Expose
    private String obsRainText;
    @SerializedName("obs_currenttemp")
    @Expose
    private String obsCurrenttemp;
    @SerializedName("watering_time")
    @Expose
    private String wateringTime;
    @SerializedName("water_saving")
    @Expose
    private Integer waterSaving;
    @SerializedName("last_contact")
    @Expose
    private String lastContact;
    @SerializedName("forecast")
    @Expose
    private List<Forecast> forecast = new LinkedList<Forecast>();
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("status_icon")
    @Expose
    private String statusIcon;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("relays")
    @Expose
    private List<Relay> relays = new LinkedList<Relay>();

    public Integer getControllerId() {
        return controllerId;
    }

    public void setControllerId(Integer controllerId) {
        this.controllerId = controllerId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getNextpoll() {
        return nextpoll;
    }

    public void setNextpoll(Integer nextpoll) {
        this.nextpoll = nextpoll;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public List<Running> getRunning() {
        return running;
    }

    public void setRunning(List<Running> running) {
        this.running = running;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getObsRain() {
        return obsRain;
    }

    public void setObsRain(String obsRain) {
        this.obsRain = obsRain;
    }

    public String getObsRainWeek() {
        return obsRainWeek;
    }

    public void setObsRainWeek(String obsRainWeek) {
        this.obsRainWeek = obsRainWeek;
    }

    public String getObsMaxtemp() {
        return obsMaxtemp;
    }

    public void setObsMaxtemp(String obsMaxtemp) {
        this.obsMaxtemp = obsMaxtemp;
    }

    public Integer getObsRainUpgrade() {
        return obsRainUpgrade;
    }

    public void setObsRainUpgrade(Integer obsRainUpgrade) {
        this.obsRainUpgrade = obsRainUpgrade;
    }

    public String getObsRainText() {
        return obsRainText;
    }

    public void setObsRainText(String obsRainText) {
        this.obsRainText = obsRainText;
    }

    public String getObsCurrenttemp() {
        return obsCurrenttemp;
    }

    public void setObsCurrenttemp(String obsCurrenttemp) {
        this.obsCurrenttemp = obsCurrenttemp;
    }

    public String getWateringTime() {
        return wateringTime;
    }

    public void setWateringTime(String wateringTime) {
        this.wateringTime = wateringTime;
    }

    public Integer getWaterSaving() {
        return waterSaving;
    }

    public void setWaterSaving(Integer waterSaving) {
        this.waterSaving = waterSaving;
    }

    public String getLastContact() {
        return lastContact;
    }

    public void setLastContact(String lastContact) {
        this.lastContact = lastContact;
    }

    public List<Forecast> getForecast() {
        return forecast;
    }

    public void setForecast(List<Forecast> forecast) {
        this.forecast = forecast;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusIcon() {
        return statusIcon;
    }

    public void setStatusIcon(String statusIcon) {
        this.statusIcon = statusIcon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Relay> getRelays() {
        return relays;
    }

    public void setRelays(List<Relay> relays) {
        this.relays = relays;
    }

}
