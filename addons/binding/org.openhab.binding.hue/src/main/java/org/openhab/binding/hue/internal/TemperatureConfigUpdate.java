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
package org.openhab.binding.hue.internal;

import static org.openhab.binding.hue.internal.FullSensor.CONFIG_LED_INDICATION;

/**
 * Updates the configuration of a temperature sensor
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class TemperatureConfigUpdate extends SensorConfigUpdate {
    /**
     *
     * @param onOff
     */
    public void setLedIndication(boolean onOff) {
        commands.add(new Command(CONFIG_LED_INDICATION, onOff));
    }
}
