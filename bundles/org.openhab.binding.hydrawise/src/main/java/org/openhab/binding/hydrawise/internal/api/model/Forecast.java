package org.openhab.binding.hydrawise.internal.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Forecast {

    @SerializedName("temp_hi")
    @Expose
    private String tempHi;

    @SerializedName("temp_lo")
    @Expose
    private String tempLo;

    @SerializedName("conditions")
    @Expose
    private String conditions;

    @SerializedName("day")
    @Expose
    private String day;

    @SerializedName("pop")
    @Expose
    private Integer pop;

    @SerializedName("humidity")
    @Expose
    private Integer humidity;

    @SerializedName("wind")
    @Expose
    private String wind;

    @SerializedName("icon")
    @Expose
    private String icon;

    @SerializedName("icon_local")
    @Expose
    private String iconLocal;

    /**
     * @return
     */
    public String getTempHi() {
        return tempHi;
    }

    /**
     * @param tempHi
     */
    public void setTempHi(String tempHi) {
        this.tempHi = tempHi;
    }

    /**
     * @return
     */
    public String getTempLo() {
        return tempLo;
    }

    /**
     * @param tempLo
     */
    public void setTempLo(String tempLo) {
        this.tempLo = tempLo;
    }

    /**
     * @return
     */
    public String getConditions() {
        return conditions;
    }

    /**
     * @param conditions
     */
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    /**
     * @return
     */
    public String getDay() {
        return day;
    }

    /**
     * @param day
     */
    public void setDay(String day) {
        this.day = day;
    }

    /**
     * @return
     */
    public Integer getPop() {
        return pop;
    }

    /**
     * @param pop
     */
    public void setPop(Integer pop) {
        this.pop = pop;
    }

    /**
     * @return
     */
    public Integer getHumidity() {
        return humidity;
    }

    /**
     * @param humidity
     */
    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    /**
     * @return
     */
    public String getWind() {
        return wind;
    }

    /**
     * @param wind
     */
    public void setWind(String wind) {
        this.wind = wind;
    }

    /**
     * @return
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * @return
     */
    public String getIconLocal() {
        return iconLocal;
    }

    /**
     * @param iconLocal
     */
    public void setIconLocal(String iconLocal) {
        this.iconLocal = iconLocal;
    }

}