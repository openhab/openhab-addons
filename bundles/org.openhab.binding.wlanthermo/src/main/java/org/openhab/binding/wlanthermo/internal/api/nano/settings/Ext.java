
package org.openhab.binding.wlanthermo.internal.api.nano.settings;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ext {

    @SerializedName("on")
    @Expose
    public Integer on;
    @SerializedName("token")
    @Expose
    public String token;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("repeat")
    @Expose
    public Integer repeat;
    @SerializedName("service")
    @Expose
    public Integer service;
    @SerializedName("services")
    @Expose
    public List<String> services = new ArrayList<String>();

}
