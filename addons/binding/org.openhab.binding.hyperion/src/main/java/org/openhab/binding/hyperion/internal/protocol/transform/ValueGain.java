package org.openhab.binding.hyperion.internal.protocol.transform;

import com.google.gson.annotations.SerializedName;

public class ValueGain implements Transform {

    @SerializedName("valueGain")
    protected Object value;

    public ValueGain(double value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

}
