package org.openhab.binding.fronius.api;

import com.google.gson.annotations.SerializedName;

public class InverterRealtimeBody {
    @SerializedName("Data")
    private InverterRealtimeBodyData data;

    public InverterRealtimeBodyData getData() {
        return data;
    }

    public void setData(InverterRealtimeBodyData data) {
        this.data = data;
    }

}
