/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * @author Sriram Balakrishnan - Initial contribution
 */
public class ColorTemperatureRequestParam implements Param {
    private int temp;

    public ColorTemperatureRequestParam(PercentType colorTemperature) {
        temp = 2200 + (colorTemperature.intValue() * (6500 - 2200) / 100);
    }
}
