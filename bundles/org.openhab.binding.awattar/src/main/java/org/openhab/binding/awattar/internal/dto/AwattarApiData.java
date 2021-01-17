
package org.openhab.binding.awattar.internal.dto;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AwattarApiData {

    @SerializedName("data")
    @Expose
    public List<Datum> data = null;
    @SerializedName("object")
    @Expose
    public String object;
    @SerializedName("url")
    @Expose
    public String url;
}
