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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.mybmw.internal.MyBMWBridgeConfiguration;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingSessionsContainer;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingStatisticsContainer;
import org.openhab.binding.mybmw.internal.dto.remote.ExecutionStatusContainer;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleBase;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleStateContainer;
import org.openhab.binding.mybmw.internal.handler.enums.RemoteService;
import org.openhab.binding.mybmw.internal.utils.BimmerConstants;
import org.openhab.binding.mybmw.internal.utils.ImageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is for local testing. You have to configure a connected account with username = "testuser" and password =
 * vehicle to be tested (e.g. BEV, ICE, BEV2, MILD_HYBRID,...)
 * The respective files are loaded from the resources folder
 *
 * You have to set the environment variable "ENVIRONMENT" to the value "test"
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactoring
 */
@NonNullByDefault
public class MyBMWFileProxy implements MyBMWProxy {
    private final Logger logger = LoggerFactory.getLogger(MyBMWFileProxy.class);
    private String vehicleToBeTested;

    private static final String RESPONSES = "responses" + File.separator;
    private static final String VEHICLES_BASE = File.separator + "vehicles_base.json";
    private static final String VEHICLES_STATE = File.separator + "vehicles_state.json";
    private static final String CHARGING_SESSIONS = File.separator + "charging_sessions.json";
    private static final String CHARGING_STATISTICS = File.separator + "charging_statistics.json";
    private static final String REMOTE_SERVICES_CALL = File.separator + "remote_service_call.json";
    private static final String REMOTE_SERVICES_STATE = File.separator + "remote_service_status.json";

    public MyBMWFileProxy(HttpClient httpClient, MyBMWBridgeConfiguration bridgeConfiguration) {
        logger.trace("MyBMWFileProxy - initialize");
        vehicleToBeTested = bridgeConfiguration.getPassword();
    }

    @Override
    public void setBridgeConfiguration(MyBMWBridgeConfiguration bridgeConfiguration) {
        logger.trace("MyBMWFileProxy - update bridge");
        vehicleToBeTested = bridgeConfiguration.getPassword();
    }

    @Override
    public List<Vehicle> requestVehicles() throws NetworkException {
        List<Vehicle> vehicles = new ArrayList<>();
        List<VehicleBase> vehiclesBase = requestVehiclesBase();

        for (VehicleBase vehicleBase : vehiclesBase) {
            VehicleStateContainer vehicleState = requestVehicleState(vehicleBase.getVin(),
                    vehicleBase.getAttributes().getBrand());

            Vehicle vehicle = new Vehicle();
            vehicle.setVehicleBase(vehicleBase);
            vehicle.setVehicleState(vehicleState);
            vehicles.add(vehicle);
        }

        return vehicles;
    }

    /**
     * request all vehicles for one specific brand and their state
     *
     * @param brand
     */
    @Override
    public List<VehicleBase> requestVehiclesBase(String brand) throws NetworkException {
        String vehicleResponseString = requestVehiclesBaseJson(brand);
        return JsonStringDeserializer.getVehicleBaseList(vehicleResponseString);
    }

    @Override
    public String requestVehiclesBaseJson(String brand) throws NetworkException {
        String vehicleResponseString = fileToString(VEHICLES_BASE);
        return vehicleResponseString;
    }

    /**
     * request vehicles for all possible brands
     *
     * @param callback
     */
    @Override
    public List<VehicleBase> requestVehiclesBase() throws NetworkException {
        List<VehicleBase> vehicles = new ArrayList<>();

        for (String brand : BimmerConstants.REQUESTED_BRANDS) {
            vehicles.addAll(requestVehiclesBase(brand));
        }

        return vehicles;
    }

    /**
     * request the vehicle image
     *
     * @param config
     * @param props
     * @return
     */
    @Override
    public byte[] requestImage(String vin, String brand, ImageProperties props) throws NetworkException {
        return "".getBytes();
    }

    /**
     * request the state for one specific vehicle
     *
     * @param baseVehicle
     * @return
     */
    @Override
    public VehicleStateContainer requestVehicleState(String vin, String brand) throws NetworkException {
        String vehicleStateResponseString = requestVehicleStateJson(vin, brand);
        return JsonStringDeserializer.getVehicleState(vehicleStateResponseString);
    }

    @Override
    public String requestVehicleStateJson(String vin, String brand) throws NetworkException {
        String vehicleStateResponseString = fileToString(VEHICLES_STATE);
        return vehicleStateResponseString;
    }

    /**
     * request charge statistics for electric vehicles
     *
     */
    @Override
    public ChargingStatisticsContainer requestChargeStatistics(String vin, String brand) throws NetworkException {
        String chargeStatisticsResponseString = requestChargeStatisticsJson(vin, brand);
        return JsonStringDeserializer.getChargingStatistics(new String(chargeStatisticsResponseString));
    }

    @Override
    public String requestChargeStatisticsJson(String vin, String brand) throws NetworkException {
        String chargeStatisticsResponseString = fileToString(CHARGING_STATISTICS);
        return chargeStatisticsResponseString;
    }

    /**
     * request charge sessions for electric vehicles
     *
     */
    @Override
    public ChargingSessionsContainer requestChargeSessions(String vin, String brand) throws NetworkException {
        String chargeSessionsResponseString = requestChargeSessionsJson(vin, brand);
        return JsonStringDeserializer.getChargingSessions(chargeSessionsResponseString);
    }

    @Override
    public String requestChargeSessionsJson(String vin, String brand) throws NetworkException {
        String chargeSessionsResponseString = fileToString(CHARGING_SESSIONS);
        return chargeSessionsResponseString;
    }

    @Override
    public ExecutionStatusContainer executeRemoteServiceCall(String vin, String brand, RemoteService service)
            throws NetworkException {
        return JsonStringDeserializer.getExecutionStatus(fileToString(REMOTE_SERVICES_CALL));
    }

    @Override
    public ExecutionStatusContainer executeRemoteServiceStatusCall(String brand, String eventId)
            throws NetworkException {
        return JsonStringDeserializer.getExecutionStatus(fileToString(REMOTE_SERVICES_STATE));
    }

    private String fileToString(String filename) {
        logger.trace("reading file {}", RESPONSES + vehicleToBeTested + filename);
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(MyBMWFileProxy.class.getClassLoader())
                        .getResourceAsStream(RESPONSES + vehicleToBeTested + filename), "UTF-8"))) {
            StringBuilder buf = new StringBuilder();
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                buf.append(sCurrentLine);
            }
            logger.trace("successful");
            return buf.toString();
        } catch (IOException e) {
            logger.error("file {} could not be loaded: {}", filename, e.getMessage());
            return "";
        }
    }

    @Override
    public Instant getNextQuota() {
        return Instant.now();
    }
}
