/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.smartthings.internal.dto;

import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;

/**
 * Data object for a color
 *
 * @author Laurent ARNAL - Initial contribution
 */
public class ColorObject {
    public Double hue = 0.0;
    public Double saturation = 0.0;

    @Override
    public String toString() {
        return String.format("%s : %s, %s : %s", SmartthingsBindingConstants.CHANNEL_NAME_HUE, hue,
                SmartthingsBindingConstants.CHANNEL_NAME_SATURATION, saturation);
    }
}
