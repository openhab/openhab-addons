/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dreame.internal.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dreame.internal.model.DreameAction;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.binding.dreame.internal.model.DreameMapData;
import org.openhab.binding.dreame.internal.model.DreameMowingStatistics;
import org.openhab.binding.dreame.internal.model.DreameMqttConfiguration;
import org.openhab.binding.dreame.internal.model.DreameProperty;
import org.openhab.binding.dreame.internal.model.DreameStatus;

/**
 * Operations used by account and mower handlers to communicate with Dreamehome.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public interface DreameMowerApi {

    /**
     * Authenticates the shared cloud session.
     *
     * @param username account user name
     * @param password account password
     * @param country account country code
     * @param cloudService cloud service used for authentication
     * @throws DreameCloudException if authentication fails
     */
    void login(String username, String password, String country, DreameCloudService cloudService)
            throws DreameCloudException;

    /** Ends the shared cloud session and clears its credentials. */
    void logout();

    /**
     * Returns the devices associated with the authenticated account.
     *
     * @return account devices
     * @throws DreameCloudException if the device list cannot be retrieved
     */
    List<DreameDevice> getDevices() throws DreameCloudException;

    /**
     * Reads the requested mower properties. Missing properties are not substituted.
     *
     * @param device mower to query
     * @param properties properties to read
     * @return values returned for the requested properties
     * @throws DreameCloudException if the properties cannot be retrieved
     */
    DreameStatus getProperties(DreameDevice device, List<DreameProperty> properties) throws DreameCloudException;

    /**
     * Returns the mower's accumulated mowing statistics.
     *
     * @param device mower to query
     * @return mowing statistics
     * @throws DreameCloudException if the statistics cannot be retrieved
     */
    DreameMowingStatistics getMowingStatistics(DreameDevice device) throws DreameCloudException;

    /**
     * Returns the maps and mowing zones available to the mower.
     *
     * @param device mower to query
     * @return available maps and zones
     * @throws DreameCloudException if the map data cannot be retrieved
     */
    DreameMapData getMapData(DreameDevice device) throws DreameCloudException;

    /**
     * Writes a boolean mower property.
     *
     * @param device mower to update
     * @param property property to write
     * @param value value to set
     * @throws DreameCloudException if the property cannot be written
     */
    void setProperty(DreameDevice device, DreameProperty property, boolean value) throws DreameCloudException;

    /**
     * Updates the mower's do-not-disturb setting while the lifecycle guard remains current.
     *
     * @param device mower to update
     * @param enabled whether do-not-disturb is enabled
     * @param taskConfiguration optional task configuration used by the mower
     * @param isCurrent returns whether the caller is still active
     * @throws DreameCloudException if the setting cannot be written
     */
    void setDnd(DreameDevice device, boolean enabled, @Nullable String taskConfiguration, BooleanSupplier isCurrent)
            throws DreameCloudException;

    /**
     * Invokes a mower action while the lifecycle guard remains current.
     *
     * @param device mower to control
     * @param action action to invoke
     * @param isCurrent returns whether the caller is still active
     * @throws DreameCloudException if the action cannot be invoked
     */
    void callAction(DreameDevice device, DreameAction action, BooleanSupplier isCurrent) throws DreameCloudException;

    /**
     * Starts mowing the selected zones while the lifecycle guard remains current.
     *
     * @param device mower to control
     * @param zoneIds identifiers of the zones to mow
     * @param isCurrent returns whether the caller is still active
     * @throws DreameCloudException if zone mowing cannot be started
     */
    void startZoneMowing(DreameDevice device, List<Integer> zoneIds, BooleanSupplier isCurrent)
            throws DreameCloudException;

    /**
     * Selects a mower map while the lifecycle guard remains current.
     *
     * @param device mower to update
     * @param mapIndex index of the map to select
     * @param isCurrent returns whether the caller is still active
     * @throws DreameCloudException if the map cannot be selected
     */
    void selectMap(DreameDevice device, int mapIndex, BooleanSupplier isCurrent) throws DreameCloudException;

    /**
     * Reads the cutting height configured for a mower map.
     *
     * @param device mower to query
     * @param mapIndex index of the map
     * @return configured cutting height
     * @throws DreameCloudException if the cutting height cannot be retrieved
     */
    BigDecimal getCuttingHeight(DreameDevice device, int mapIndex) throws DreameCloudException;

    /**
     * Updates the cutting height for a mower map while the lifecycle guard remains current.
     *
     * @param device mower to update
     * @param mapIndex index of the map
     * @param height cutting height to set
     * @param isCurrent returns whether the caller is still active
     * @throws DreameCloudException if the cutting height cannot be written
     */
    void setCuttingHeight(DreameDevice device, int mapIndex, BigDecimal height, BooleanSupplier isCurrent)
            throws DreameCloudException;

    /**
     * Returns short-lived MQTT connection details for a device.
     *
     * @param device device to connect
     * @return MQTT connection details
     * @throws DreameCloudException if the connection details cannot be retrieved
     */
    DreameMqttConfiguration mqttConfiguration(DreameDevice device) throws DreameCloudException;
}
