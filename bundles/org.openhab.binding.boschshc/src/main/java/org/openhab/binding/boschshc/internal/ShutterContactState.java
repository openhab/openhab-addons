package org.openhab.binding.boschshc.internal;

import com.google.gson.annotations.SerializedName;

public class ShutterContactState {

    /*
     * :{"@type":"shutterContactState","value":"OPEN"}
     */

    @SerializedName("@type")
    String type;

    String value;
}
