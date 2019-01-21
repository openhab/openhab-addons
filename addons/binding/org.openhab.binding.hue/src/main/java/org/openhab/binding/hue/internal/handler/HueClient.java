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
package org.openhab.binding.hue.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.ConfigUpdate;
import org.openhab.binding.hue.internal.FullLight;
import org.openhab.binding.hue.internal.FullSensor;
import org.openhab.binding.hue.internal.StateUpdate;

/**
 * Access to the Hue system for light handlers.
 *
 * @author Simon Kaufmann - initial contribution and API
 * @author Samuel Leisering - Added support for sensor API
 * @author Christoph Weitkamp - Added support for sensor API
 */
@NonNullByDefault
public interface HueClient {

    /**
     * Register a light status listener.
     *
     * @param lightStatusListener the light status listener
     * @return {@code true} if the collection of listeners has changed as a result of this call
     */
    boolean registerLightStatusListener(LightStatusListener lightStatusListener);

    /**
     * Unregister a light status listener.
     *
     * @param lightStatusListener the light status listener
     * @return {@code true} if the collection of listeners has changed as a result of this call
     */
    boolean unregisterLightStatusListener(LightStatusListener lightStatusListener);

    /**
     * Register a sensor status listener.
     *
     * @param sensorStatusListener the sensor status listener
     * @return {@code true} if the collection of listeners has changed as a result of this call
     */
    boolean registerSensorStatusListener(SensorStatusListener sensorStatusListener);

    /**
     * Unregister a sensor status listener.
     *
     * @param sensorStatusListener the sensor status listener
     * @return {@code true} if the collection of listeners has changed as a result of this call
     */
    boolean unregisterSensorStatusListener(SensorStatusListener sensorStatusListener);

    /**
     * Get the light by its ID.
     *
     * @param lightId the light ID
     * @return the full light representation of {@code null} if it could not be found
     */
    @Nullable
    FullLight getLightById(String lightId);

    /**
     * Get the sensor by its ID.
     *
     * @param sensorId the sensor ID
     * @return the full sensor representation of {@code null} if it could not be found
     */
    @Nullable
    FullSensor getSensorById(String sensorId);

    /**
     * Updates the given light.
     *
     * @param light the light to be updated
     * @param stateUpdate the state update
     */
    void updateLightState(FullLight light, StateUpdate stateUpdate);

    /**
     * Updates the given sensors config.
     *
     * @param sensor the light to be updated
     * @param configUpdate the config update
     */
    void updateSensorConfig(FullSensor sensor, ConfigUpdate configUpdate);
}
