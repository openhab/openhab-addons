/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.hue.internal.api.dto.clip1.ConfigUpdate;
import org.openhab.binding.hue.internal.api.dto.clip1.FullGroup;
import org.openhab.binding.hue.internal.api.dto.clip1.FullLight;
import org.openhab.binding.hue.internal.api.dto.clip1.FullSensor;
import org.openhab.binding.hue.internal.api.dto.clip1.StateUpdate;
import org.openhab.binding.hue.internal.discovery.HueDeviceDiscoveryService;

/**
 * Access to the Hue system for light handlers.
 *
 * @author Simon Kaufmann - initial contribution and API
 * @author Samuel Leisering - Added support for sensor API
 * @author Christoph Weitkamp - Added support for sensor API
 * @author Laurent Garnier - Added support for groups
 */
@NonNullByDefault
public interface HueClient {

    /**
     * Register {@link HueDeviceDiscoveryService} to bridge handler
     *
     * @param listener the discovery service
     * @return {@code true} if the new discovery service is accepted
     */
    boolean registerDiscoveryListener(HueDeviceDiscoveryService listener);

    /**
     * Unregister {@link HueDeviceDiscoveryService} from bridge handler
     *
     * @return {@code true} if the discovery service was removed
     */
    boolean unregisterDiscoveryListener();

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
     * Register a group status listener.
     *
     * @param groupStatusListener the group status listener
     * @return {@code true} if the collection of listeners has changed as a result of this call
     */
    boolean registerGroupStatusListener(GroupStatusListener groupStatusListener);

    /**
     * Unregister a group status listener.
     *
     * @param groupStatusListener the group status listener
     * @return {@code true} if the collection of listeners has changed as a result of this call
     */
    boolean unregisterGroupStatusListener(GroupStatusListener groupStatusListener);

    /**
     * Get the light by its ID.
     *
     * @param lightId the light ID
     * @return the full light representation or {@code null} if it could not be found
     */
    @Nullable
    FullLight getLightById(String lightId);

    /**
     * Get the sensor by its ID.
     *
     * @param sensorId the sensor ID
     * @return the full sensor representation or {@code null} if it could not be found
     */
    @Nullable
    FullSensor getSensorById(String sensorId);

    /**
     * Get the group by its ID.
     *
     * @param groupId the group ID
     * @return the full group representation or {@code null} if it could not be found
     */
    @Nullable
    FullGroup getGroupById(String groupId);

    /**
     * Updates the given light.
     *
     * @param listener the light status listener to block it for state updates
     * @param light the light to be updated
     * @param stateUpdate the state update
     * @param fadeTime the status listener will be blocked for this duration after command
     */
    void updateLightState(LightStatusListener listener, FullLight light, StateUpdate stateUpdate, long fadeTime);

    /**
     * Updates the given sensors config.
     *
     * @param sensor the light to be updated
     * @param configUpdate the config update
     */
    void updateSensorConfig(FullSensor sensor, ConfigUpdate configUpdate);

    /**
     * Updates the given sensor.
     *
     * @param sensor the sensor to be updated
     * @param stateUpdate the state update
     */
    void updateSensorState(FullSensor sensor, StateUpdate stateUpdate);

    /**
     * Updates the given group.
     *
     * @param group the group to be updated
     * @param stateUpdate the state update
     */
    void updateGroupState(FullGroup group, StateUpdate stateUpdate, long fadeTime);

    /**
     * Recall scene to all lights that belong to the scene.
     *
     * @param id the ID of the scene to be recalled
     */
    void recallScene(String id);
}
