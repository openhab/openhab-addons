package org.openhab.binding.fronius.api;

import com.google.gson.annotations.SerializedName;

public class PowerFlowRealtimeInverter {

    @SerializedName("DT")
    private double dt;
    @SerializedName("P")
    private double p;
    @SerializedName("E_Day")
    private double eDay;
    @SerializedName("E_Year")
    private double eYear;
    @SerializedName("E_Total")
    private double eTotal;

    public double getDt() {
        return dt;
    }

    public void setDt(double dt) {
        this.dt = dt;
    }

    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    public double geteDay() {
        return eDay;
    }

    public void seteDay(double eDay) {
        this.eDay = eDay;
    }

    public double geteYear() {
        return eYear;
    }

    public void seteYear(double eYear) {
        this.eYear = eYear;
    }

    public double geteTotal() {
        return eTotal;
    }

    public void seteTotal(double eTotal) {
        this.eTotal = eTotal;
    }

}
