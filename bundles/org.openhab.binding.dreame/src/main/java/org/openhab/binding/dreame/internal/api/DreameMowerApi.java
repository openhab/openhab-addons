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

    /** Authenticates the shared cloud session. */
    void login(String username, String password, String country, DreameCloudService cloudService)
            throws DreameCloudException;

    /** Ends the shared cloud session and clears its credentials. */
    void logout();

    /** Returns all devices reported by the authenticated account. */
    List<DreameDevice> getDevices() throws DreameCloudException;

    /** Reads the requested mower properties without substituting missing values. */
    DreameStatus getProperties(DreameDevice device, List<DreameProperty> properties) throws DreameCloudException;

    /** Returns the accumulated mowing statistics. */
    DreameMowingStatistics getMowingStatistics(DreameDevice device) throws DreameCloudException;

    /** Returns maps and mowing zones available to the mower. */
    DreameMapData getMapData(DreameDevice device) throws DreameCloudException;

    /** Writes a boolean mower property. */
    void setProperty(DreameDevice device, DreameProperty property, boolean value) throws DreameCloudException;

    /** Updates the mower's do-not-disturb setting if the lifecycle guard remains current. */
    void setDnd(DreameDevice device, boolean enabled, @Nullable String taskConfiguration, BooleanSupplier isCurrent)
            throws DreameCloudException;

    /** Invokes a mower action if the lifecycle guard remains current. */
    void callAction(DreameDevice device, DreameAction action, BooleanSupplier isCurrent) throws DreameCloudException;

    /** Starts mowing the selected zones if the lifecycle guard remains current. */
    void startZoneMowing(DreameDevice device, List<Integer> zoneIds, BooleanSupplier isCurrent)
            throws DreameCloudException;

    /** Selects a mower map if the lifecycle guard remains current. */
    void selectMap(DreameDevice device, int mapIndex, BooleanSupplier isCurrent) throws DreameCloudException;

    /** Reads the cutting height configured for a map. */
    BigDecimal getCuttingHeight(DreameDevice device, int mapIndex) throws DreameCloudException;

    /** Updates the cutting height for a map if the lifecycle guard remains current. */
    void setCuttingHeight(DreameDevice device, int mapIndex, BigDecimal height, BooleanSupplier isCurrent)
            throws DreameCloudException;

    /** Returns short-lived MQTT connection details for the device. */
    DreameMqttConfiguration mqttConfiguration(DreameDevice device) throws DreameCloudException;
}
