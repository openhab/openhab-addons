
package org.openhab.binding.wlanthermo.internal.api.settings;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Notes {

    @SerializedName("fcm")
    @Expose
    public List<Object> fcm = new ArrayList<Object>();
    @SerializedName("ext")
    @Expose
    public Ext ext;

}
