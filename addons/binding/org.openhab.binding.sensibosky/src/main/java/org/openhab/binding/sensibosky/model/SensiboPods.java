package org.openhab.binding.sensibosky.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class SensiboPods {
    @SerializedName("status")
    public String status;
    @SerializedName("result")
    public List<DeviceId> result;
}
