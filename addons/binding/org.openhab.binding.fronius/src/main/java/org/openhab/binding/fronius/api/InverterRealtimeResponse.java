package org.openhab.binding.fronius.api;

import com.google.gson.annotations.SerializedName;

public class InverterRealtimeResponse {
    @SerializedName("Head")
    private Head head;
    @SerializedName("Body")
    private InverterRealtimeBody body;

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public InverterRealtimeBody getBody() {
        return body;
    }

    public void setBody(InverterRealtimeBody body) {
        this.body = body;
    }

}
