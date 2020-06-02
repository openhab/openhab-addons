
package org.openhab.binding.wlanthermo.internal.api.mini.builtin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("temp")
    @Expose
    private Integer temp;
    @SerializedName("color")
    @Expose
    private String color;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("temp_min")
    @Expose
    private Integer tempMin;
    @SerializedName("temp_max")
    @Expose
    private Integer tempMax;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("alert")
    @Expose
    private Boolean alert;
    @SerializedName("show")
    @Expose
    private Boolean show;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Data() {
    }

    /**
     * 
     * @param tempMax
     * @param temp
     * @param color
     * @param alert
     * @param name
     * @param show
     * @param state
     * @param tempMin
     */
    public Data(Integer temp, String color, String state, Integer tempMin, Integer tempMax, String name, Boolean alert, Boolean show) {
        super();
        this.temp = temp;
        this.color = color;
        this.state = state;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.name = name;
        this.alert = alert;
        this.show = show;
    }

    public Integer getTemp() {
        return temp;
    }

    public void setTemp(Integer temp) {
        this.temp = temp;
    }

    public Data withTemp(Integer temp) {
        this.temp = temp;
        return this;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Data withColor(String color) {
        this.color = color;
        return this;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Data withState(String state) {
        this.state = state;
        return this;
    }

    public Integer getTempMin() {
        return tempMin;
    }

    public void setTempMin(Integer tempMin) {
        this.tempMin = tempMin;
    }

    public Data withTempMin(Integer tempMin) {
        this.tempMin = tempMin;
        return this;
    }

    public Integer getTempMax() {
        return tempMax;
    }

    public void setTempMax(Integer tempMax) {
        this.tempMax = tempMax;
    }

    public Data withTempMax(Integer tempMax) {
        this.tempMax = tempMax;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Data withName(String name) {
        this.name = name;
        return this;
    }

    public Boolean getAlert() {
        return alert;
    }

    public void setAlert(Boolean alert) {
        this.alert = alert;
    }

    public Data withAlert(Boolean alert) {
        this.alert = alert;
        return this;
    }

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
    }

    public Data withShow(Boolean show) {
        this.show = show;
        return this;
    }

}
