
package org.openhab.binding.wlanthermo.internal.api.settings;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Settings {

    @SerializedName("device")
    @Expose
    public Device device;
    @SerializedName("system")
    @Expose
    public System system;
    @SerializedName("hardware")
    @Expose
    public List<String> hardware = new ArrayList<String>();
    @SerializedName("api")
    @Expose
    public Api api;
    @SerializedName("sensors")
    @Expose
    public List<String> sensors = new ArrayList<String>();
    @SerializedName("pid")
    @Expose
    public List<Pid> pid = new ArrayList<Pid>();
    @SerializedName("aktor")
    @Expose
    public List<String> aktor = new ArrayList<String>();
    @SerializedName("iot")
    @Expose
    public Iot iot;
    @SerializedName("notes")
    @Expose
    public Notes notes;

}
