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

    void login(String username, String password, String country, DreameCloudService cloudService)
            throws DreameCloudException;

    void logout();

    List<DreameDevice> getDevices() throws DreameCloudException;

    DreameStatus getProperties(DreameDevice device, List<DreameProperty> properties) throws DreameCloudException;

    DreameMowingStatistics getMowingStatistics(DreameDevice device) throws DreameCloudException;

    DreameMapData getMapData(DreameDevice device) throws DreameCloudException;

    void setProperty(DreameDevice device, DreameProperty property, boolean value) throws DreameCloudException;

    void setDnd(DreameDevice device, boolean enabled, @Nullable String taskConfiguration) throws DreameCloudException;

    void callAction(DreameDevice device, DreameAction action) throws DreameCloudException;

    void startZoneMowing(DreameDevice device, List<Integer> zoneIds) throws DreameCloudException;

    void selectMap(DreameDevice device, int mapIndex) throws DreameCloudException;

    BigDecimal getCuttingHeight(DreameDevice device, int mapIndex) throws DreameCloudException;

    void setCuttingHeight(DreameDevice device, int mapIndex, BigDecimal height) throws DreameCloudException;

    DreameMqttConfiguration mqttConfiguration(DreameDevice device) throws DreameCloudException;
}
