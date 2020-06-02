
package org.openhab.binding.wlanthermo.internal.api.nano.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class System {

    @SerializedName("time")
    @Expose
    public String time;
    @SerializedName("unit")
    @Expose
    public String unit;
    @SerializedName("ap")
    @Expose
    public String ap;
    @SerializedName("host")
    @Expose
    public String host;
    @SerializedName("language")
    @Expose
    public String language;
    @SerializedName("version")
    @Expose
    public String version;
    @SerializedName("getupdate")
    @Expose
    public String getupdate;
    @SerializedName("autoupd")
    @Expose
    public Boolean autoupd;
    @SerializedName("hwversion")
    @Expose
    public String hwversion;
    @SerializedName("god")
    @Expose
    public Integer god;

}
