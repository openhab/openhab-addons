
package org.openhab.binding.wlanthermo.internal.api.nano.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Device {

    @SerializedName("device")
    @Expose
    public String device;
    @SerializedName("serial")
    @Expose
    public String serial;
    @SerializedName("item")
    @Expose
    public String item;
    @SerializedName("hw_version")
    @Expose
    public String hwVersion;
    @SerializedName("sw_version")
    @Expose
    public String swVersion;
    @SerializedName("api_version")
    @Expose
    public String apiVersion;
    @SerializedName("language")
    @Expose
    public String language;

}
