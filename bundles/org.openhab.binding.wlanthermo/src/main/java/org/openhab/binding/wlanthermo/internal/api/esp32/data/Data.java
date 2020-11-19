
package org.openhab.binding.wlanthermo.internal.api.esp32.data;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Data {

    @SerializedName("system")
    @Expose
    private System system;
    @SerializedName("channel")
    @Expose
    private List<Channel> channel = null;
    @SerializedName("pitmaster")
    @Expose
    private Pitmaster pitmaster;

    public System getSystem() {
        return system;
    }

    public void setSystem(System system) {
        this.system = system;
    }

    public List<Channel> getChannel() {
        return channel;
    }

    public void setChannel(List<Channel> channel) {
        this.channel = channel;
    }

    public Pitmaster getPitmaster() {
        return pitmaster;
    }

    public void setPitmaster(Pitmaster pitmaster) {
        this.pitmaster = pitmaster;
    }
}
