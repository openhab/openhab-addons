package org.openhab.binding.sensibosky.http;

import java.util.List;

import org.openhab.binding.sensibosky.model.AcStateRead;

import com.google.gson.annotations.SerializedName;

public class SensiboAcStateResponse {
    @SerializedName("status")
    public String status;
    @SerializedName("moreResults")
    public boolean moreResults;
    @SerializedName("result")
    public List<AcStateRead> result;
}