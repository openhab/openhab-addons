
package org.openhab.binding.wlanthermo.internal.api.esp32.settings;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Notes {

    @SerializedName("fcm")
    @Expose
    private List<Object> fcm = null;
    @SerializedName("ext")
    @Expose
    private Ext ext;

    public List<Object> getFcm() {
        return fcm;
    }

    public void setFcm(List<Object> fcm) {
        this.fcm = fcm;
    }

    public Ext getExt() {
        return ext;
    }

    public void setExt(Ext ext) {
        this.ext = ext;
    }
}
