/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.handler.backend;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.MyBMWBridgeConfiguration;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingSessionsContainer;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingStatisticsContainer;
import org.openhab.binding.mybmw.internal.dto.remote.ExecutionStatusContainer;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleBase;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleStateContainer;
import org.openhab.binding.mybmw.internal.handler.enums.RemoteService;
import org.openhab.binding.mybmw.internal.utils.ImageProperties;

/**
 * this is the interface for requesting the myBMW responses
 *
 * @author Martin Grassl - Initial Contribution
 */
@NonNullByDefault
public interface MyBMWProxy {

    void setBridgeConfiguration(MyBMWBridgeConfiguration bridgeConfiguration);

    List<Vehicle> requestVehicles() throws NetworkException;

    /**
     * request all vehicles for one specific brand and their state
     *
     * @param brand
     */
    List<VehicleBase> requestVehiclesBase(String brand) throws NetworkException;

    String requestVehiclesBaseJson(String brand) throws NetworkException;

    /**
     * request vehicles for all possible brands
     *
     * @param callback
     */
    List<VehicleBase> requestVehiclesBase() throws NetworkException;

    /**
     * request the vehicle image
     *
     * @param config
     * @param props
     * @return
     */
    byte[] requestImage(String vin, String brand, ImageProperties props) throws NetworkException;

    /**
     * request the state for one specific vehicle
     *
     * @param baseVehicle
     * @return
     */
    VehicleStateContainer requestVehicleState(String vin, String brand) throws NetworkException;

    String requestVehicleStateJson(String vin, String brand) throws NetworkException;

    /**
     * request charge statistics for electric vehicles
     *
     */
    ChargingStatisticsContainer requestChargeStatistics(String vin, String brand) throws NetworkException;

    String requestChargeStatisticsJson(String vin, String brand) throws NetworkException;

    /**
     * request charge sessions for electric vehicles
     *
     */
    ChargingSessionsContainer requestChargeSessions(String vin, String brand) throws NetworkException;

    String requestChargeSessionsJson(String vin, String brand) throws NetworkException;

    ExecutionStatusContainer executeRemoteServiceCall(String vin, String brand, RemoteService service)
            throws NetworkException;

    ExecutionStatusContainer executeRemoteServiceStatusCall(String brand, String eventId) throws NetworkException;

    Instant getNextQuota();
}
