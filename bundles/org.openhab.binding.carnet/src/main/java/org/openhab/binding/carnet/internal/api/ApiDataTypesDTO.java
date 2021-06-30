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

import static org.openhab.binding.carnet.internal.CarUtils.getString;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.api.carnet.CarNetApiGSonDTO.CNVehicleDetails.CarNetVehicleDetails;
import org.openhab.binding.carnet.internal.api.carnet.CarNetApiGSonDTO.CarNetVehicleStatus;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleList.WCVehicle;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData.WCVehicleStatus;
import org.openhab.binding.carnet.internal.config.CombinedConfig;

/**
 * The {@link ApiDataTypesDTO} defines unified data types as mapping layer between the vehicle handlers and the
 * different API formats.
 *
 * @author Markus Michels - Initial contribution
 */
public class ApiDataTypesDTO {

    public static class VehicleDetails {
        public String vin = "";
        public String brand = "";
        public String model = "";
        public String color = "";
        public String engine = "";
        public String transmission = "";

        public VehicleDetails(CombinedConfig config, CarNetVehicleDetails vehicle) {
            vin = getString(vehicle.vin);
            brand = config.api.brand;
            if (vehicle.carportData.modelName != null) {
                model = getString(vehicle.carportData.modelYear) + " " + getString(vehicle.brand) + " "
                        + getString(vehicle.carportData.modelName) + " (" + getString(vehicle.carportData.countryCode)
                        + "-" + getString(vehicle.carportData.modelCode) + ")";
            } else {
                model = getString(vehicle.brand);
            }
            color = getString(vehicle.carportData.color);
            engine = getString(vehicle.carportData.engine);
            transmission = getString(vehicle.carportData.transmission);
        }

        public VehicleDetails(CombinedConfig config, WCVehicle vehicle) {
            vin = vehicle.vin;
            brand = config.api.brand;
            model = getString(vehicle.model) + "(" + getString(vehicle.nickname) + ")";
        }

        public Map<String, String> getProperties() {
            Map<String, String> properties = new TreeMap<String, String>();
            return properties;
        }

        public String getId() {
            return vin;
        }
    }

    public static class VehicleStatus {
        public VehicleStatus(CarNetVehicleStatus status) {
            cnStatus = status;
        }

        public VehicleStatus(WCVehicleStatus status) {
            wcStatus = status;
        }

        public @Nullable CarNetVehicleStatus cnStatus;
        public @Nullable WCVehicleStatus wcStatus;
    }
}
