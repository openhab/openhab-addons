package org.openhab.binding.nest.internal.data;

import com.google.gson.annotations.SerializedName;

/**
 * Top level data for all the nest stuff, this is the format the nest data comes back from nest in.
 * 
 * @author David Bennett
 */
public class TopLevelData {
    public NestDevices getDevices() {
        return devices;
    }

    public NestMetadata getMetadata() {
        return metadata;
    }

    @SerializedName("devices")
    private NestDevices devices;
    @SerializedName("metadata")
    private NestMetadata metadata;
}
