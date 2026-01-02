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
package org.openhab.binding.bluelink.internal.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;

/**
 * HTTP client implementation for the Bluelink API.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public interface BluelinkApi {
    /**
     * Returns properties to store as Thing properties so they are persisted independent of the {@link BluelinkApi}
     * lifecycle.
     * 
     * @return
     */
    default Map<String, String> getProperties() {
        return Collections.emptyMap();
    }

    boolean login() throws BluelinkApiException;

    /**
     * Get list of enrolled vehicles.
     */
    List<Vehicle> getVehicles() throws BluelinkApiException;

    @Nullable
    VehicleStatus getVehicleStatus(Vehicle vehicle, boolean forceRefresh) throws BluelinkApiException;

    boolean lockVehicle(Vehicle vehicle) throws BluelinkApiException;

    boolean unlockVehicle(Vehicle vehicle) throws BluelinkApiException;

    boolean climateStart(Vehicle vehicle, QuantityType<Temperature> temperature, boolean heat, boolean defrost)
            throws BluelinkApiException;

    boolean climateStop(Vehicle vehicle) throws BluelinkApiException;

    boolean startCharging(Vehicle vehicle) throws BluelinkApiException;

    boolean stopCharging(Vehicle vehicle) throws BluelinkApiException;

    boolean setChargeLimitDC(Vehicle vehicle, int limit) throws BluelinkApiException;

    boolean setChargeLimitAC(Vehicle vehicle, int limit) throws BluelinkApiException;
}
