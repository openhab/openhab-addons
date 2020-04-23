
package org.openhab.binding.wlanthermo.internal.api.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Pid {

    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("aktor")
    @Expose
    public Integer aktor;
    @SerializedName("Kp")
    @Expose
    public Double kp;
    @SerializedName("Ki")
    @Expose
    public Double ki;
    @SerializedName("Kd")
    @Expose
    public Double kd;
    @SerializedName("DCmmin")
    @Expose
    public Double dCmmin;
    @SerializedName("DCmmax")
    @Expose
    public Double dCmmax;
    @SerializedName("opl")
    @Expose
    public Integer opl;
    @SerializedName("tune")
    @Expose
    public Integer tune;
    @SerializedName("jp")
    @Expose
    public Integer jp;

}
