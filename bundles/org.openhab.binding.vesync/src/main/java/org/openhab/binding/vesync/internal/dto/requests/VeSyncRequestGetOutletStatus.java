package org.openhab.binding.vesync.internal.dto.requests;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

@NonNullByDefault
public class VeSyncRequestGetOutletStatus extends VeSyncRequest {

    @SerializedName("cid")
    public String cid = "";

    @SerializedName("configModule")
    public String configModule = "";

    @SerializedName("debugMode")
    public boolean debugMode = false;

    @SerializedName("subDeviceNo")
    public int subDeviceNo = 0;

    @SerializedName("token")
    public String token = "";

    @SerializedName("userCountryCode")
    public String userCountryCode = "";

    @SerializedName("deviceId")
    public String deviceId = "";

    @SerializedName("configModel")
    public String configModel = "";

    @SerializedName("payload")
    public Payload payload = new Payload();

    public class Payload {

        @SerializedName("data")
        public Data data = new Data();

        // Empty class
        public class Data {
        }

        @SerializedName("method")
        public String method = "";

        @SerializedName("subDeviceNo")
        public int subDeviceNo = 0;

        @SerializedName("source")
        public String source = "APP";
    }
}
