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

import java.util.List;
import java.util.function.BooleanSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.binding.dreame.internal.model.DreameVacuumAction;
import org.openhab.binding.dreame.internal.model.DreameVacuumProperties;
import org.openhab.binding.dreame.internal.model.DreameVacuumSetting;

/**
 * Vacuum controls using the shared cloud session.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public interface DreameVacuumApi {
    /** Reads the five status properties without substituting missing or failed values. */
    DreameVacuumProperties getVacuumProperties(DreameDevice device, BooleanSupplier isCurrent)
            throws DreameCloudException;

    /** Reads the saved-map list reference independently of MQTT updates. */
    @Nullable
    String getVacuumMapListObjectName(DreameDevice device, BooleanSupplier isCurrent) throws DreameCloudException;

    /** Requests a complete current map frame directly from the device. */
    @Nullable
    String getVacuumMap(DreameDevice device, BooleanSupplier isCurrent) throws DreameCloudException;

    /** Downloads the saved-map list referenced by the device's MAP_LIST property. */
    String getVacuumMapList(DreameDevice device, String objectName, BooleanSupplier isCurrent)
            throws DreameCloudException;

    /** Checks the lifecycle guard after authentication and before dispatch; commands are never retried. */
    void callVacuumAction(DreameDevice device, DreameVacuumAction action, BooleanSupplier isCurrent)
            throws DreameCloudException;

    /** Writes a validated cleaning setting; writes are never retried. */
    void setVacuumSetting(DreameDevice device, DreameVacuumSetting setting, int value, BooleanSupplier isCurrent)
            throws DreameCloudException;

    /** Starts one cleaning pass in the selected map segments. */
    void cleanVacuumRooms(DreameDevice device, List<Integer> roomIds, int suctionLevel, int waterVolume,
            BooleanSupplier isCurrent) throws DreameCloudException;
}
