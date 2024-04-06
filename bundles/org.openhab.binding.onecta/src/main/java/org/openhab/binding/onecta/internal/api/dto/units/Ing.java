package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class Ing {
    @SerializedName("d")
    private Float[] day;
    @SerializedName("w")
    private Float[] week;
    @SerializedName("m")
    private Float[] month;

    public Float[] getDay() {
        return day;
    }

    public Float[] getWeek() {
        return week;
    }

    public Float[] getMonth() {
        return month;
    }
}
