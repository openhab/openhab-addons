
package org.openhab.binding.wlanthermo.internal.api.data;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Pitmaster {

    @SerializedName("type")
    @Expose
    private List<String> type = new ArrayList<String>();
    @SerializedName("pm")
    @Expose
    private List<Pm> pm = new ArrayList<Pm>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Pitmaster() {
    }

    /**
     * 
     * @param type
     * @param pm
     */
    public Pitmaster(List<String> type, List<Pm> pm) {
        super();
        this.type = type;
        this.pm = pm;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public Pitmaster withType(List<String> type) {
        this.type = type;
        return this;
    }

    public List<Pm> getPm() {
        return pm;
    }

    public void setPm(List<Pm> pm) {
        this.pm = pm;
    }

    public Pitmaster withPm(List<Pm> pm) {
        this.pm = pm;
        return this;
    }

}
