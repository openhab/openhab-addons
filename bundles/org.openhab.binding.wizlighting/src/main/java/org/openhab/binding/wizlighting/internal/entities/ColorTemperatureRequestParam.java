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

import com.google.gson.annotations.Expose;

/**
 * This POJO represents Color Request Param
 *
 * The outgoing JSON should look like this:
 *
 * {"id": 24, "method": "setPilot", "params": {"temp": 3000}}
 *
 * @author Alexander Seeliger - Initial contribution
 *
 */
@NonNullByDefault
public class ColorTemperatureRequestParam implements Param {
    @Expose(serialize = true, deserialize = true)
    private int temp;

    public ColorTemperatureRequestParam(PercentType colorPercent) {
        // NOTE: 0% is cold (highest K) and 100% is warm (lowest K)
        temp = MAX_COLOR_TEMPERATURE - Math.round((COLOR_TEMPERATURE_RANGE * colorPercent.floatValue()) / 100);
    }

    public int getColorTemperature() {
        return temp;
    }

    public void SingleColorModeTemperature(int temp) {
        this.temp = temp;
    }
}
