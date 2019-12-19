package org.openhab.binding.wizlighting.internal.entities;

import org.eclipse.smarthome.core.library.types.PercentType;

public class ColorTemperatureRequestParam implements Param {
    private int temp;

    public ColorTemperatureRequestParam(PercentType colorTemperature) {
        temp = 2200 + (colorTemperature.intValue() * (6500 - 2200) / 100);
    }
}
