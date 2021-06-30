/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.api;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.carnet.internal.api.carnet.CarNetPendingRequest;
import org.openhab.binding.carnet.internal.config.CombinedConfig;
import org.openhab.core.library.types.PointType;

/**
 * The {@link ApiBrandInterface} defines the internal API interface, which then gets adapted to the varios Connected Car
 * APIs
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public interface ApiBrandInterface {
    abstract void initialize(CombinedConfig configIn) throws ApiException;

    abstract CombinedConfig initialize(String vin, CombinedConfig configIn) throws ApiException;

    abstract Map<String, CarNetPendingRequest> getPendingRequests();

    abstract void checkPendingRequests();

    abstract public ArrayList<String> getVehicles() throws ApiException;

    abstract VehicleDetails getVehicleDetails(String vin) throws ApiException;

    abstract String refreshVehicleStatus() throws ApiException;

    abstract boolean isAccessTokenValid() throws ApiException;

    abstract String controlLock(boolean lock) throws ApiException;

    abstract String controlClimater(boolean start, String heaterSource) throws ApiException;

    abstract String controlClimaterTemp(double tempC, String heaterSource) throws ApiException;

    abstract String controlPreHeating(boolean start, int duration) throws ApiException;

    abstract String controlVentilation(boolean start, int duration) throws ApiException;

    abstract String controlCharger(boolean start) throws ApiException;

    abstract String controlMaxCharge(int maxCurrent) throws ApiException;

    abstract String controlWindowHeating(boolean start) throws ApiException;

    abstract String controlHonkFlash(boolean honk, PointType position, int duration) throws ApiException;
}
