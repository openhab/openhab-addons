
package org.openhab.binding.wlanthermo.internal.api.esp32.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Display {

    @SerializedName("updname")
    @Expose
    private String updname;
    @SerializedName("orientation")
    @Expose
    private Integer orientation;

    public String getUpdname() {
        return updname;
    }

    public void setUpdname(String updname) {
        this.updname = updname;
    }

    public Integer getOrientation() {
        return orientation;
    }

    public void setOrientation(Integer orientation) {
        this.orientation = orientation;
    }
}
