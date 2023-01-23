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
 */package org.openhab.binding.mybmw.internal.handler.backend;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingSessionsContainer;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingStatisticsContainer;
import org.openhab.binding.mybmw.internal.dto.network.NetworkException;
import org.openhab.binding.mybmw.internal.dto.remote.ExecutionStatusContainer;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleBase;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleStateContainer;
import org.openhab.binding.mybmw.internal.handler.enums.RemoteService;
import org.openhab.binding.mybmw.internal.utils.ImageProperties;

/**
 * this is the interface for requesting the myBMW responses
 * 
 * @author Martin Grassl - extract interface
 */
@NonNullByDefault
public interface MyBMWProxy {
    List<@NonNull Vehicle> requestVehicles() throws NetworkException;

    /**
     * request all vehicles for one specific brand and their state
     *
     * @param brand
     */
    public List<VehicleBase> requestVehiclesBase(String brand) throws NetworkException;

    public String requestVehiclesBaseJson(String brand) throws NetworkException;

    /**
     * request vehicles for all possible brands
     *
     * @param callback
     */
    public List<VehicleBase> requestVehiclesBase() throws NetworkException;

    /**
     * request the vehicle image
     *
     * @param config
     * @param props
     * @return
     */
    public byte[] requestImage(String vin, String brand, ImageProperties props) throws NetworkException;

    /**
     * request the state for one specific vehicle
     *
     * @param baseVehicle
     * @return
     */
    public VehicleStateContainer requestVehicleState(String vin, String brand) throws NetworkException;

    public String requestVehicleStateJson(String vin, String brand) throws NetworkException;

    /**
     * request charge statistics for electric vehicles
     *
     */
    public ChargingStatisticsContainer requestChargeStatistics(String vin, String brand) throws NetworkException;

    public String requestChargeStatisticsJson(String vin, String brand) throws NetworkException;

    /**
     * request charge sessions for electric vehicles
     *
     */
    public ChargingSessionsContainer requestChargeSessions(String vin, String brand) throws NetworkException;

    public String requestChargeSessionsJson(String vin, String brand) throws NetworkException;

    public ExecutionStatusContainer executeRemoteServiceCall(String vin, String brand, RemoteService service)
            throws NetworkException;

    public ExecutionStatusContainer executeRemoteServiceStatusCall(String brand, String eventId)
            throws NetworkException;

}
