package org.openhab.binding.fronius.api;

import com.google.gson.annotations.SerializedName;

public class PowerFlowRealtimeResponse {
    @SerializedName("Head")
    private Head head;

    @SerializedName("Body")
    private PowerFlowRealtimeBody body;

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public PowerFlowRealtimeBody getBody() {
        return body;
    }

    public void setBody(PowerFlowRealtimeBody body) {
        this.body = body;
    }

}
