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

import static org.eclipse.smarthome.binding.hue.internal.FullSensor.CONFIG_ON;

/**
 * Collection of updates to the sensor configuration.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class SensorConfigUpdate extends ConfigUpdate {
    /**
     *
     * @param onOff
     */
    public void setOn(boolean onOff) {
        commands.add(new Command(CONFIG_ON, onOff));
    }
}
