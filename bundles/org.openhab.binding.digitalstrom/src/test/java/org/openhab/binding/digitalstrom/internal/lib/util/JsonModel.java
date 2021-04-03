package org.openhab.binding.digitalstrom.internal.lib.util;

import java.util.ArrayList;
import java.util.List;

public class JsonModel {
    public JsonModel(List<OutputChannel> outputChannels) {
        this(-1, outputChannels);
    }

    public JsonModel(int outputMode, List<OutputChannel> outputChannels) {
        this.outputMode = outputMode;
        this.outputChannels = new ArrayList<>();
        if (outputChannels != null) {
            this.outputChannels = outputChannels;
        }
    }

    int outputMode;

    List<OutputChannel> outputChannels;
}
