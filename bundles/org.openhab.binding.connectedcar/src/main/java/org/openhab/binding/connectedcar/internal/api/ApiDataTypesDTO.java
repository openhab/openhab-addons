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
package org.openhab.binding.connectedcar.internal.api;

import static org.openhab.binding.connectedcar.internal.CarUtils.getString;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNFindCarResponse;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNStoredPosition;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNVehicleDetails.CarNetVehicleDetails;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetCoordinate;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetVehicleStatus;
import org.openhab.binding.connectedcar.internal.api.skodaenyak.SEApiJsonDTO.SEVehicleList.SEVehicle;
import org.openhab.binding.connectedcar.internal.api.skodaenyak.SEApiJsonDTO.SEVehicleStatusData;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleList.WCVehicle;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData.WCVehicleStatus;
import org.openhab.binding.connectedcar.internal.config.CombinedConfig;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;

/**
 * The {@link ApiDataTypesDTO} defines unified data types as mapping layer between the vehicle handlers and the
 * different API formats.
 *
 * @author Markus Michels - Initial contribution
 */
public class ApiDataTypesDTO {
    public static final String API_BRAND_AUDI = "Audi";
    public static final String API_BRAND_VW = "VW";
    public static final String API_BRAND_VWID = "Id";
    public static final String API_BRAND_VWGO = "Go";
    public static final String API_BRAND_WECHARGE = "Wc";
    public static final String API_BRAND_SKODA = "Skoda";
    public static final String API_BRAND_ENYAK = "Enyak";
    public static final String API_BRAND_SEAT = "Seat";
    public static final String API_BRAND_NULL = "NULL";

    public static final String API_REQUEST_SUCCESSFUL = "request_successful";
    public static final String API_REQUEST_SUCCEEDED = "succeeded";
    public static final String API_REQUEST_IN_PROGRESS = "request_in_progress";
    public static final String API_REQUEST_NOT_FOUND = "request_not_found";
    public static final String API_REQUEST_FAIL = "request_fail";
    public static final String API_REQUEST_QUEUED = "queued"; // rclima
    public static final String API_REQUEST_FETCHED = "fetched"; // rclima
    public static final String API_REQUEST_STARTED = "request_started"; // rhonk
    public static final String API_REQUEST_FAILED = "failed";
    public static final String API_REQUEST_ERROR = "api_error";
    public static final String API_REQUEST_TIMEOUT = "timoute";
    public static final String API_REQUEST_REJECTED = "rejected";
    public static final String API_REQUEST_UNSUPPORTED = "unsupported";

    public static final String API_STATUS_MSG_PREFIX = "api-status";
    public static final String API_STATUS_CLASS_SECURUTY = "VSR.security";
    public static final String API_STATUS_GW_ERROR = "gw.error";

    public static class VehicleDetails {
        public String vin = "";
        public String brand = "";
        public String model = "";
        public String color = "";
        public String engine = "";
        public String transmission = "";

        public VehicleDetails() {
        }

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
            model = getString(vehicle.nickname) + "(" + brand + " " + getString(vehicle.model) + ")";
        }

        public VehicleDetails(CombinedConfig config, SEVehicle vehicle) {
            vin = getString(vehicle.vin);
            brand = getString(vehicle.specification.brand);
            model = getString(vehicle.specification.title) + "(" + brand + " " + getString(vehicle.specification.model)
                    + ")";
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
        public VehicleStatus() {
        }

        public VehicleStatus(CarNetVehicleStatus status) {
            cnStatus = status;
        }

        public VehicleStatus(WCVehicleStatus status) {
            wcStatus = status;
        }

        public VehicleStatus(SEVehicleStatusData status) {
            seStatus = status;
        }

        public @Nullable CarNetVehicleStatus cnStatus;
        public @Nullable WCVehicleStatus wcStatus;
        public @Nullable SEVehicleStatusData seStatus;
    }

    public static class CarPosition {
        public CarNetCoordinate coordinate = new CarNetCoordinate();
        public String parkingTimeUTC = "";
        public String timestampCarSent = "";
        public String timestampTssReceived = "";

        public CarPosition() {
        }

        public CarPosition(CNFindCarResponse position) {
            if (position != null && position.findCarResponse != null) {
                coordinate = position.findCarResponse.carPosition.carCoordinate;
                timestampCarSent = position.findCarResponse.carPosition.timestampCarSent;
                timestampTssReceived = position.findCarResponse.carPosition.timestampTssReceived;
                parkingTimeUTC = position.findCarResponse.parkingTimeUTC;
            }
        }

        public CarPosition(CNStoredPosition position) {
            if (position != null && position.storedPositionResponse != null) {
                coordinate = position.storedPositionResponse.position.carCoordinate;
                parkingTimeUTC = position.storedPositionResponse.parkingTimeUTC;
            }
        }

        public CarPosition(PointType point) {
            coordinate.latitude = point.getLatitude().intValue() * 1000000;
            coordinate.longitude = point.getLongitude().intValue() * 1000000;
        }

        public double getLattitude() {
            return coordinate.latitude / 1000000.0;
        }

        public double getLongitude() {
            return coordinate.longitude / 1000000.0;
        }

        public PointType getAsPointType() {
            return new PointType(new DecimalType(getLattitude()), new DecimalType(getLongitude()));
        }

        public String getCarSentTime() {
            return !timestampTssReceived.isEmpty() ? timestampTssReceived : timestampCarSent;
        }

        public String getParkingTime() {
            return parkingTimeUTC;
        }
    }

    public static class JwtToken {
        /*
         * "at_hash":"9wYmNBTSKQ8bJV7F2f4otQ",
         * "sub":"c3ab56e9-XXXX-41c8-XXXX-XXXXXXXX",
         * "email_verified":true,
         * "cor":"DE",
         * "iss":"https:\/\/identity.vwgroup.io",
         * "jtt":"id_token",
         * "type":"identity",
         * "nonce":"MTYyMjMxNzA0MTQ5OA==",
         * "lee":[
         * "AUDI"
         * ],
         * "aud":[
         * "09b6cbec-cd19-4589-82fd-363dfa8c24da@apps_vw-dilab_com",
         * "VWGMBB01DELIV1",
         * "https:\/\/api.vas.eu.dp15.vwg-connect.com",
         * "https:\/\/api.vas.eu.wcardp.io"
         * ],
         * "acr":"https:\/\/identity.vwgroup.io\/assurance\/loa-2",
         * "updated_at":1617052457793,
         * "aat":"identitykit",
         * "exp":1622320642,
         * "iat":1622317042,
         * "jti":"1cb4abb3-497d-4f46-a300-669223f830ee",
         * "email":"user@me.com"
         *
         */
        public String sub;
        public Boolean email_verified;
        public String cor;
        public String type;
        public String nonce;
    }
}
