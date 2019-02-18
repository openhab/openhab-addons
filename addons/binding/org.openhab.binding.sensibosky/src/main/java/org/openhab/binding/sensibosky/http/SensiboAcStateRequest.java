package org.openhab.binding.sensibosky.http;

import org.openhab.binding.sensibosky.model.AcState;

import com.google.gson.annotations.SerializedName;

public class SensiboAcStateRequest {
    @SerializedName("acState")
    public AcState acState;
}
