package org.openhab.binding.boschshc.internal;

import com.google.gson.annotations.SerializedName;

public class PowerSwitchStateUpdate {
    /*
     * "body": {
     * "mode": "raw",
     * "raw": "{\r\n    \"@type\": \"powerSwitchState\",\r\n    \"switchState\": \"ON\"\r\n}"
     * },
     */

    @SerializedName("@type")
    public String type;

    public String switchState;

    public PowerSwitchStateUpdate(String type, String state) {

        this.type = type;
        this.switchState = state;
    }
}
