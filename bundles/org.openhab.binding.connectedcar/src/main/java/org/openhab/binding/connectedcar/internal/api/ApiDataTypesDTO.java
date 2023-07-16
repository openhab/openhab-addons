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

import static org.openhab.binding.connectedcar.internal.BindingConstants.API_REQUEST_TIMEOUT_SEC;
import static org.openhab.binding.connectedcar.internal.util.Helpers.getString;

import java.util.Date;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNFindCarResponse;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNStoredPosition;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNVehicleDetails.CarNetVehicleDetails;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetCoordinate;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetVehicleStatus;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetPendingRequest;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPActionRequest;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPVehicleStatusData;
import org.openhab.binding.connectedcar.internal.api.skodae.SEApiJsonDTO.SEVehicleList.SEVehicle;
import org.openhab.binding.connectedcar.internal.api.skodae.SEApiJsonDTO.SEVehicleStatusData;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WeChargeStationDetails;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WeChargeStatus;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCParkingPosition.WeConnectParkingPosition;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleList.WCVehicle;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData;
import org.openhab.binding.connectedcar.internal.config.CombinedConfig;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;

/**
 * The {@link ApiDataTypesDTO} defines unified data types as mapping layer between the vehicle handlers and the
 * different API formats.
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
public class ApiDataTypesDTO {
    public static final String API_BRAND_AUDI = "Audi";
    public static final String API_BRAND_VW = "VW";
    public static final String API_BRAND_VWID = "Id";
    public static final String API_BRAND_WECHARGE = "WeCharge";
    public static final String API_BRAND_SKODA = "Skoda";
    public static final String API_BRAND_SKODA_E = "Skoda-E";
    public static final String API_BRAND_SEAT = "Seat";
    public static final String API_BRAND_FORD = "Ford";
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
    public static final String API_REQUEST_REJECTED = "rejected";
    public static final String API_REQUEST_ERROR = "api_error";
    public static final String API_REQUEST_TIMEOUT = "timout";
    public static final String API_REQUEST_SECURITY = "request_security";
    public static final String API_REQUEST_UNSUPPORTED = "unsupported";

    public static final String API_SERVICE_VEHICLE_STATUS_REPORT = "status";

    public static final String API_STATUS_MSG_PREFIX = "api-status";
    public static final String API_STATUS_CLASS_SECURUTY = "VSR.security";
    public static final String API_STATUS_GW_ERROR = "gw.error";

    public static final int API_FUEL_TYPE_NATGAS = 2;
    public static final int API_FUEL_TYPE_ELECTRIC = 3;
    public static final int API_FUEL_TYPE_FUEL = 5;
    public static final int API_FUEL_TYPE_DIESEL = 6;

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
            model = getString(vehicle.nickname) + " (" + brand + " " + getString(vehicle.model) + ")";
        }

        public VehicleDetails(CombinedConfig config, SEVehicle vehicle) {
            vin = getString(vehicle.vin);
            brand = getString(vehicle.specification.brand);
            model = getString(vehicle.specification.title) + " (" + brand + " " + getString(vehicle.specification.model)
                    + ")";
        }

        public VehicleDetails(CombinedConfig config, WeChargeStationDetails station) {
            vin = station.id;
            brand = config.api.brand;
            model = station.name + " (" + station.location.description + ", " + station.model + ")";
        }

        public String getId() {
            return vin;
        }
    }

    public static class VehicleStatus {
        public @Nullable CarNetVehicleStatus cnStatus;
        public @Nullable WCVehicleStatusData wcStatus;
        public @Nullable SEVehicleStatusData seStatus;
        public @Nullable FPVehicleStatusData fpStatus;
        public @Nullable WeChargeStatus weChargeStatus;
        public GeoPosition vehicleLocation = new GeoPosition();
        public GeoPosition parkingPosition = new GeoPosition();

        public VehicleStatus() {
        }

        public VehicleStatus(CarNetVehicleStatus status) {
            cnStatus = status;
        }

        public VehicleStatus(WCVehicleStatusData status) {
            wcStatus = status;
        }

        public VehicleStatus(SEVehicleStatusData status) {
            seStatus = status;
        }

        public VehicleStatus(FPVehicleStatusData status) {
            fpStatus = status;
        }

        public VehicleStatus(WeChargeStatus status) {
            this.weChargeStatus = status;
        }
    }

    public static class GeoPosition {
        public CarNetCoordinate coordinate = new CarNetCoordinate();
        public String parkingTimeUTC = "";
        public String timestampCarSent = "";
        public String timestampTssReceived = "";

        public GeoPosition() {
        }

        public GeoPosition(CNFindCarResponse position) {
            if (position != null && position.findCarResponse != null) {
                coordinate = position.findCarResponse.carPosition.carCoordinate;
                timestampCarSent = position.findCarResponse.carPosition.timestampCarSent;
                timestampTssReceived = position.findCarResponse.carPosition.timestampTssReceived;
                parkingTimeUTC = position.findCarResponse.parkingTimeUTC;
            }
        }

        public GeoPosition(CNStoredPosition position) {
            if (position != null && position.storedPositionResponse != null) {
                coordinate = position.storedPositionResponse.position.carCoordinate;
                parkingTimeUTC = position.storedPositionResponse.parkingTimeUTC;
            }
        }

        public GeoPosition(String sLongitude, String sLatitude) {
            if (!sLongitude.isEmpty() || !sLatitude.isEmpty()) {
                coordinate.longitude = (int) (Double.parseDouble(sLongitude) * 1000000);
                coordinate.latitude = (int) (Double.parseDouble(sLatitude) * 1000000);
            }
        }

        public GeoPosition(WeConnectParkingPosition pos) {
            this(pos.lon, pos.lat);
            parkingTimeUTC = getString(pos.carCapturedTimestamp);
        }

        public GeoPosition(PointType point) {
            coordinate.latitude = point.getLatitude().intValue() * 1000000;
            coordinate.longitude = point.getLongitude().intValue() * 1000000;
        }

        public boolean isValid() {
            return coordinate.latitude > 0 && coordinate.longitude > 0;
        }

        public double getLattitude() {
            return coordinate.latitude / 1000000.0;
        }

        public double getLongitude() {
            return coordinate.longitude / 1000000.0;
        }

        public PointType asPointType() {
            return new PointType(new DecimalType(getLattitude()), new DecimalType(getLongitude()));
        }

        public String getCarSentTime() {
            return !timestampTssReceived.isEmpty() ? timestampTssReceived : timestampCarSent;
        }

        public String getParkingTime() {
            return parkingTimeUTC;
        }
    }

    public static class ApiActionRequest {
        public String vin = "";
        public String service = "";
        public String action = "";
        public String requestId = ""; // request id returned from API
        public String checkUrl = ""; // URL to query request status
        public String status = ""; // status code, usualle API_REQUEST_xxx
        public String error = ""; // error message/details
        public Date creationTime = new Date();
        public long timeout = API_REQUEST_TIMEOUT_SEC;

        public ApiActionRequest() {
        }

        public ApiActionRequest(CarNetPendingRequest req) {
            this();
            this.vin = req.vin;
            this.service = req.service;
            this.action = req.action;
            this.checkUrl = req.checkUrl;
            this.requestId = req.requestId;
        }

        public ApiActionRequest(FPActionRequest req) {
            this();
            this.service = req.service;
            this.action = req.action;
            this.checkUrl = req.checkUrl;
            this.requestId = req.requestId;
        }

        public static boolean isInProgress(String status) {
            String st = status;
            return API_REQUEST_IN_PROGRESS.equals(st) || API_REQUEST_QUEUED.equals(st) || API_REQUEST_FETCHED.equals(st)
                    || API_REQUEST_STARTED.equals(st);
        }

        public boolean isInProgress() {
            return isInProgress(this.status);
        }

        public boolean isExpired() {
            Date currentTime = new Date();
            long diff = currentTime.getTime() - creationTime.getTime();
            return (diff / 1000) > timeout;
        }
    }
}
