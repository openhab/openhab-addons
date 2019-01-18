/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.hue.internal;

import static org.eclipse.smarthome.binding.hue.internal.FullSensor.*;

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