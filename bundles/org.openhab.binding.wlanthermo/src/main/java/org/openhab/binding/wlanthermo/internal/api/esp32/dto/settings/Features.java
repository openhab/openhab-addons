
package org.openhab.binding.wlanthermo.internal.api.esp32.dto.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Features {

    @SerializedName("bluetooth")
    @Expose
    private Boolean bluetooth;
    @SerializedName("pitmaster")
    @Expose
    private Boolean pitmaster;

    public Boolean getBluetooth() {
        return bluetooth;
    }

    public void setBluetooth(Boolean bluetooth) {
        this.bluetooth = bluetooth;
    }

    public Boolean getPitmaster() {
        return pitmaster;
    }

    public void setPitmaster(Boolean pitmaster) {
        this.pitmaster = pitmaster;
    }
}
