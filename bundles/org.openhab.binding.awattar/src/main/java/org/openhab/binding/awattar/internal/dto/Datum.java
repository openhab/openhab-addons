
package org.openhab.binding.awattar.internal.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum {

    @SerializedName("end_timestamp")
    @Expose
    public long endTimestamp;
    @SerializedName("marketprice")
    @Expose
    public double marketprice;
    @SerializedName("start_timestamp")
    @Expose
    public long startTimestamp;
    @SerializedName("unit")
    @Expose
    public String unit;
}
