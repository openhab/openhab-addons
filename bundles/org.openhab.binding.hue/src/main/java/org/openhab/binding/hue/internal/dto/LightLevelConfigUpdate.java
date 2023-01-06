/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.dto;

import static org.openhab.binding.hue.internal.dto.FullSensor.*;

/**
 * Updates the configuration of a light level sensor
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
public class LightLevelConfigUpdate extends SensorConfigUpdate {
    /**
     *
     * @param onOff
     */
    public void setLedIndication(boolean onOff) {
        commands.add(new Command(CONFIG_LED_INDICATION, onOff));
    }

    /**
     *
     * @param threshold
     */
    public void setThresholdDark(int threshold) {
        commands.add(new Command(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK, threshold));
    }

    /**
     *
     * @param offset
     */
    public void setThresholdOffset(int offset) {
        commands.add(new Command(CONFIG_LIGHT_LEVEL_THRESHOLD_OFFSET, offset));
    }
}
