package org.openhab.binding.boschshc.internal;

import com.google.gson.annotations.SerializedName;

/**
 * {
 * "result": [
 * {
 * "path": "/devices/hdm:ZigBee:000d6f0004b95a62/services/LatestMotion",
 * "@type": "DeviceServiceData",
 * "id": "LatestMotion",
 * "state": {
 * "latestMotionDetected": "2020-04-03T19:02:19.054Z",
 * "@type": "latestMotionState"
 * },
 * "deviceId": "hdm:ZigBee:000d6f0004b95a62"
 * }
 * ],
 * "jsonrpc": "2.0"
 * }
 *
 */
public class LatestMotionState {

    @SerializedName("@type")
    String type;

    String latestMotionDetected;
}
