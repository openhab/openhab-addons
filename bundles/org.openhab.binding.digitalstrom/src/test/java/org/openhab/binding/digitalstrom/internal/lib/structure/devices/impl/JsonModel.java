package org.openhab.binding.digitalstrom.internal.lib.structure.devices.impl;

import java.util.ArrayList;
import java.util.List;

public class JsonModel {
	public JsonModel(int outputMode, List<OutputChannel> outputChannels) {
		super();
		this.outputMode = outputMode;
		this.outputChannels = new ArrayList<>();
		if (outputChannels != null) {
			this.outputChannels = outputChannels;
		}
	}

	int outputMode;

	List<OutputChannel> outputChannels;
}
