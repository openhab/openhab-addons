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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingSessionsContainer;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingStatisticsContainer;
import org.openhab.binding.mybmw.internal.dto.remote.ExecutionStatusContainer;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleBase;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleStateContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 *
 * deserialization of a JSON string to a Java Object
 *
 * @author Martin Grassl - initial contribution
 */
@NonNullByDefault
public interface JsonStringDeserializer {

    static final Logger LOGGER = LoggerFactory.getLogger(JsonStringDeserializer.class);

    static final Gson GSON = new Gson();

    public static List<VehicleBase> getVehicleBaseList(String vehicleBaseJson) {
        try {
            VehicleBase[] vehicleBaseArray = deserializeString(vehicleBaseJson, VehicleBase[].class);
            return Arrays.asList(vehicleBaseArray);
        } catch (JsonSyntaxException e) {
            LOGGER.debug("JsonSyntaxException {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public static VehicleStateContainer getVehicleState(String vehicleStateJson) {
        try {
            VehicleStateContainer vehicleState = deserializeString(vehicleStateJson, VehicleStateContainer.class);
            vehicleState.setRawStateJson(vehicleStateJson);
            return vehicleState;
        } catch (JsonSyntaxException e) {
            LOGGER.debug("JsonSyntaxException {}", e.getMessage());
            return new VehicleStateContainer();
        }
    }

    public static ChargingStatisticsContainer getChargingStatistics(String chargeStatisticsJson) {
        try {
            ChargingStatisticsContainer chargeStatistics = deserializeString(chargeStatisticsJson,
                    ChargingStatisticsContainer.class);
            return chargeStatistics;
        } catch (JsonSyntaxException e) {
            LOGGER.debug("JsonSyntaxException {}", e.getMessage());
            return new ChargingStatisticsContainer();
        }
    }

    public static ChargingSessionsContainer getChargingSessions(String chargeSessionsJson) {
        try {
            return deserializeString(chargeSessionsJson, ChargingSessionsContainer.class);
        } catch (JsonSyntaxException e) {
            LOGGER.debug("JsonSyntaxException {}", e.getMessage());
            return new ChargingSessionsContainer();
        }
    }

    public static ExecutionStatusContainer getExecutionStatus(String executionStatusJson) {
        try {
            return deserializeString(executionStatusJson, ExecutionStatusContainer.class);
        } catch (JsonSyntaxException e) {
            LOGGER.debug("JsonSyntaxException {}", e.getMessage());
            return new ExecutionStatusContainer();
        }
    }

    public static <T> T deserializeString(String toBeDeserialized, Class<@NonNull T> deserializedClass) {
        return GSON.fromJson(toBeDeserialized, deserializedClass);
    }
}
