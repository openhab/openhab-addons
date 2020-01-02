/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.wizlighting.internal.entities;

import static org.openhab.binding.wizlighting.internal.WizLightingBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * This POJO represents Color Request Param
 *
 * @author Alexander Seeliger - Initial contribution
 *
 */
@NonNullByDefault
public class ColorTemperatureRequestParam implements Param {
    private int colorTemperature;

    public ColorTemperatureRequestParam(PercentType colorPercent) {
        colorTemperature = MIN_COLOR_TEMPERATURE
                + Math.round((COLOR_TEMPERATURE_RANGE * colorPercent.floatValue()) / 100);
    }

    public int getColorTemperature() {
        return colorTemperature;
    }

    public void setColorTemperature(int temp) {
        this.colorTemperature = temp;
    }
}
