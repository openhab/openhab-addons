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
package org.openhab.binding.connectedcar.internal.api;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.connectedcar.internal.config.CombinedConfig;
import org.openhab.core.library.types.PointType;

/**
 * The {@link ApiBrandInterface} defines the internal API interface, which then gets adapted to the varios Connected Car
 * APIs
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public interface ApiBrandInterface {

    boolean isInitialized();

    void initialize(CombinedConfig configIn) throws ApiException;

    CombinedConfig initialize(String vin, CombinedConfig configIn) throws ApiException;

    ApiBrandProperties getProperties() throws ApiException;

    @Nullable
    ApiBrandProperties getProperties2();

    void setConfig(CombinedConfig config);

    String getApiUrl() throws ApiException;

    boolean refreshTokens() throws ApiException;

    public ArrayList<String> getVehicles() throws ApiException;

    VehicleDetails getVehicleDetails(String vin) throws ApiException;

    String refreshVehicleStatus() throws ApiException;

    boolean isAccessTokenValid() throws ApiException;

    String controlEngine(boolean start) throws ApiException;

    String controlLock(boolean lock) throws ApiException;

    String controlClimater(boolean start, String heaterSource) throws ApiException;

    String controlClimaterTemp(double tempC, String heaterSource) throws ApiException;

    String controlPreHeating(boolean start, int duration) throws ApiException;

    String controlVentilation(boolean start, int duration) throws ApiException;

    String controlWindowHeating(boolean start) throws ApiException;

    String controlCharger(boolean start) throws ApiException;

    String controlMaxCharge(int maxCurrent) throws ApiException;

    String controlTargetChgLevel(int targetLevel) throws ApiException;

    String controlHonkFlash(boolean honk, PointType position, int duration) throws ApiException;
}
