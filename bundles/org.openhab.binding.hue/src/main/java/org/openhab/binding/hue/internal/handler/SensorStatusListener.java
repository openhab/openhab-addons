/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.FullSensor;

/**
 * The {@link SensorStatusListener} is notified when a sensor status has changed or a sensor has been removed or added.
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public interface SensorStatusListener {

    /**
     * This method returns the sensor Id
     * 
     * @return sensor id of thing or DISCOVERY for discovery service
     */
    String getSensorId();

    /**
     * This method is called whenever the state of the given sensor has changed. The new state can be obtained by
     * {@link FullSensor#getState()}.
     *
     * @param sensor The sensor which received the state update.
     * @return The sensor handler returns true if it accepts the new state.
     */
    boolean onSensorStateChanged(FullSensor sensor);

    /**
     * This method is called whenever a sensor is removed.
     */
    void onSensorRemoved();

    /**
     * This method is called whenever a sensor is reported as gone.
     */
    void onSensorGone();

    /**
     * This method is called whenever a sensor is added.
     *
     * @param sensor The added sensor
     */
    void onSensorAdded(FullSensor sensor);
}
