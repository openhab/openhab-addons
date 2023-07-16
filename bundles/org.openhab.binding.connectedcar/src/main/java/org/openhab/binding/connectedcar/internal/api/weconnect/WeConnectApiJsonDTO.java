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
package org.openhab.binding.connectedcar.internal.api.weconnect;

import static org.openhab.binding.connectedcar.internal.BindingConstants.API_REQUEST_TIMEOUT_SEC;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_REQUEST_QUEUED;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_REQUEST_STARTED;

import java.util.ArrayList;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 * {@link WeConnectApiJsonDTO} defines the We Connect API data formats
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
public class WeConnectApiJsonDTO {
    public static final String WCAPI_BASE_URL = "https://emea.bff.cariad.digital/vehicle/v1";

    public static final String WCSERVICE_STATUS = "status";
    public static final String WCSERVICE_CLIMATISATION = "climatisation";
    public static final String WCSERVICE_CHARGING = "charging";

    public static final String WCCAPABILITY_PARKINGPOS = "parkingPosition";

    public static class WCCapability {
        /*
         * {
         * "id": "automation",
         * "expirationDate": "2024-05-09T00:00:00Z",
         * "userDisablingAllowed": true
         * }
         */
        public String id;
        public String expirationDate;
        public Boolean userDisablingAllowed;
        public ArrayList<Integer> status;
    }

    public static class WCVehicleList {
        public static class WCVehicle {
            /*
             * "vin": "WVWZZZE1ZMP053898",
             * "role": "PRIMARY_USER",
             * "enrollmentStatus": "COMPLETED",
             * "model": "ID.3",
             * "nickname": "ID.3",
             * "capabilities": []
             */

            public String vin;
            public String role;
            public String enrollmentStatus;
            public String vehicle;
            public String model;
            public String nickname;
            public ArrayList<WCCapability> capabilities;
        }

        public ArrayList<WCVehicle> data;
    }

    public static class WCVehicleStatusData {
        public class WCSingleStatusItem {
            /*
             * {
             * "name": "left",
             * "status": "on"
             * },
             */
            public String name;
            public String status;
        }

        public class WCMultiStatusItem {
            /*
             * {
             * "name": "bonnet",
             * "status": [
             * "closed"
             * ]
             * },
             */
            public String name;
            public ArrayList<String> status;
        }

        // public class WCVehicleStatus {
        public class WCAccessStatus {

            public String carCapturedTimestamp;
            public String overallStatus;
            public ArrayList<WCMultiStatusItem> doors;
            public ArrayList<WCMultiStatusItem> windows;
        }

        public class WCBatteryStatus {
            /*
             * "batteryStatus": {
             * "carCapturedTimestamp": "2021-06-25T14:01:35Z",
             * "currentSOC_pct": 52,
             * "cruisingRangeElectric_km": 221
             * },
             */
            public String carCapturedTimestamp;
            public Integer currentSOC_pct;
            public Integer cruisingRangeElectric_km;
        }

        public class WCChargingStatus {
            /*
             * "chargingStatus": {
             * "carCapturedTimestamp": "2021-06-25T14:01:35Z",
             * "remainingChargingTimeToComplete_min": 0,
             * "chargingState": "readyForCharging",
             * "chargeMode": "manual",
             * "chargePower_kW": 0,
             * "chargeRate_kmph": 0
             * },
             */
            public String carCapturedTimestamp;
            public Integer remainingChargingTimeToComplete_min;
            public String chargingState;
            public String chargeMode;
            public Integer chargePower_kW;
            public Integer chargeRate_kmph;
        }

        public class WCChargingSettings {
            /*
             * "chargingSettings": {
             * "carCapturedTimestamp": "2021-06-25T23:06:41Z",
             * "maxChargeCurrentAC": "maximum",
             * "autoUnlockPlugWhenCharged": "permanent",
             * "targetSOC_pct": 90
             * },
             */
            public String carCapturedTimestamp;
            public String maxChargeCurrentAC;
            public String autoUnlockPlugWhenCharged;
            public Integer targetSOC_pct;
        }

        public class WCChargeMode {
            /*
             * "chargeMode":{
             * "preferredChargeMode":"manual",
             * "availableChargeModes":[
             * "invalid"
             * ]
             * },
             *
             */
            public String preferredChargeMode;
            public ArrayList<String> availableChargeModes;
        }

        public class WCPlugStatus {
            /*
             * "plugStatus": {
             * "carCapturedTimestamp": "2021-06-25T23:06:41Z",
             * "plugConnectionState": "disconnected",
             * "plugLockState": "unlocked"
             * },
             */
            public String carCapturedTimestamp;
            public String plugConnectionState;
            public String plugLockState;
        }

        public class WCClimatisationStatus {
            /*
             * "climatisationStatus": {
             * "carCapturedTimestamp": "2021-06-25T23:06:40Z",
             * "remainingClimatisationTime_min": 0,
             * "climatisationState": "off"
             * },
             */
            public String carCapturedTimestamp;
            public Integer remainingClimatisationTime_min;
            public String climatisationState;
        }

        public class WCClimatisationSettings {
            /*
             * "climatisationSettings": {
             * "carCapturedTimestamp": "2021-06-25T23:06:47Z",
             * "targetTemperature_K": 295.15,
             * "targetTemperature_C": 22,
             * "climatisationWithoutExternalPower": true,
             * "climatizationAtUnlock": false,
             * "windowHeatingEnabled": false,
             * "zoneFrontLeftEnabled": false,
             * "zoneFrontRightEnabled": false,
             * "unitInCar": "celsius"
             * },
             */
            public String carCapturedTimestamp;
            public Double targetTemperature_K;
            public Double targetTemperature_C;
            public Boolean climatisationWithoutExternalPower;
            public Boolean climatizationAtUnlock;
            public Boolean windowHeatingEnabled;
            public Boolean zoneFrontLeftEnabled;
            public Boolean zoneFrontRightEnabled;
            public String unitInCar;
        }

        public class WCClimatisationTimer {
            /*
             * {
             * "id": 1,
             * "enabled": false,
             * "singleTimer": {
             * "startDateTime": "1999-12-31T22:00:00Z"
             * }
             */
            public class WCClimaTimer {
                public class WCSingleTimer {
                    public String startDateTime;
                }

                public String id;
                public Boolean enabled;
                public WCSingleTimer singleTimer;
            }

            public ArrayList<WCClimaTimer> timers;
            public String carCapturedTimestamp;
            public String timeInCar;
        }

        public class WCWindowHeatingStatus {
            /*
             * "windowHeatingStatus": {
             * "carCapturedTimestamp": "2021-06-25T14:01:37Z",
             * "windowHeatingStatus": [
             * {
             * "windowLocation": "front",
             * "windowHeatingState": "off"
             * },
             * {
             * "windowLocation": "rear",
             * "windowHeatingState": "off"
             * }
             * ]
             * },
             */

            public class WCHeatingStatus {
                public String windowLocation;
                public String windowHeatingState;
            }

            public String carCapturedTimestamp;
            public ArrayList<WCHeatingStatus> windowHeatingStatus;
        }

        public class WCLightStatus {
            /*
             * "lightsStatus": {
             * "carCapturedTimestamp": "2021-09-04T16:59:11Z",
             * "lights": [
             * {
             * "name": "right",
             * "status": "off"
             * },
             * {
             * "name": "left",
             * "status": "off"
             * }
             * ]
             * },
             */
            public String carCapturedTimestamp;
            public ArrayList<WCSingleStatusItem> lights;
        }

        public class WCRangeStatus {
            /*
             * "rangeStatus":
             * {
             * "carCapturedTimestamp": "2021-06-25T14:01:35Z",
             * "carType": "electric",
             * "primaryEngine": {
             * "type": "electric",
             * "currentSOC_pct": 52,
             * "remainingRange_km": 221
             * },
             * "totalRange_km": 221
             * },
             */
            public class WCEngine {
                public String type;
                public Integer currentSOC_pct;
                public Integer remainingRange_km;
            }

            public String carCapturedTimestamp;
            public String carType;
            public WCEngine primaryEngine;
            public Integer totalRange_km;
        }

        public class WCMaintenanceStatus {
            public String carCapturedTimestamp;
            @SerializedName("inspectionDue_days")
            public Integer inspectionDueDays;
            @SerializedName("inspectionDue_km")
            public Integer inspectionDueKm;
            @SerializedName("mileage_km")
            public Integer mileageKm;
            @SerializedName("oilServiceDue_days")
            public Integer oilServiceDueDays;
            @SerializedName("oilServiceDue_km")
            public Integer oilServiceDueKm;
        }

        public class WCCapabilityStatus {
            /*
             * "capabilityStatus":
             * {
             * "capabilities": [
             * {
             * "id": "automation",
             * "expirationDate": "2024-05-09T00:00:00Z",
             * "userDisablingAllowed": true
             * },
             */
            public ArrayList<WCCapability> capabilities;
        }

        public class WCAccessStatusValue {
            public class WCAccessStatusStatus {
                public WCAccessStatus value;
            }

            public WCAccessStatusStatus accessStatus;
        }

        public class WCVehicleLightsValue {
            public class WCVehicleLightsStatus {
                public WCLightStatus value;
            }

            public WCVehicleLightsStatus lightsStatus;
        }

        public class WCFuelStatus {
            public class WCFuelStatusValue {
                public WCRangeStatus value;
            }

            public WCFuelStatusValue rangeStatus;
        }

        public class WCUserCapabilities {
            public class WCCapcabilitiesStatus {
                public ArrayList<WCCapability> value;
            }

            public WCCapcabilitiesStatus capabilitiesStatus;
        }

        public class WCVehicleHealthInspection {
            public class WCVehicleHealthInspectionStatus {
                public WCMaintenanceStatus value;
            }

            public WCVehicleHealthInspectionStatus maintenanceStatus;
        }

        public class WCCharging {
            public class WCBatteryStatusValue {
                public WCBatteryStatus value;
            }

            public class WCChargeModeStatus {
                public WCChargeMode value;
            }

            public class WCChargingSettingsValue {
                WCChargingSettings value;
            }

            public class WCChargingStatusValue {
                public WCChargingStatus value;
            }

            public class WCPlugStatusValue {
                public WCPlugStatus value;
            }

            public class WCChargingCareSettingsValue {
                public class WCChargingCareSettings {
                    /*
                     * "batteryCareMode": "activated"
                     */
                    public String batteryCareMode;
                }

                public WCChargingCareSettings value;
            }

            public WCBatteryStatusValue batteryStatus;
            public WCChargingCareSettingsValue chargingCareSettings;
            public WCChargeModeStatus chargeMode;
            public WCChargingSettingsValue chargingSettings;
            public WCChargingStatusValue chargingStatus;
            public WCPlugStatusValue plugStatus;
        }

        public class WCClimatistation {
            public class WCClimatisationSettingsValue {
                public WCClimatisationSettings value;
            }

            public class WCClimatisationStatusValue {
                public WCClimatisationStatus value;
            }

            public class WCClimatisationTimerValue {
                public WCClimatisationTimer value;
            }

            public class WCWindowHeatingStatusValue {
                public WCWindowHeatingStatus value;
            }

            public WCClimatisationSettingsValue climatisationSettings;
            public WCClimatisationStatusValue climatisationStatus;
            public WCClimatisationTimerValue climatisationTimer;
            public WCWindowHeatingStatusValue windowHeatingStatus;
        }

        public WCAccessStatusValue access;
        public WCVehicleLightsValue vehicleLights;
        public WCFuelStatus fuelStatus;
        public WCUserCapabilities userCapabilities;
        public WCVehicleHealthInspection vehicleHealthInspection;
        public WCCharging charging;
        public WCClimatistation climatisation;
    }

    public static class WCActionResponse {
        public class WCApiError {
            /*
             * "error": {
             * "code": 2105,
             * "message": "The provided request is incorrect",
             * "group": 2,
             * "info": "Internal error, please try again later. If the problem persists, please contact our support.",
             * "retry": true
             * }
             */
            public class WCApiErrorDetails {
                public Integer code;
                public String message;
                public Integer group;
                public String info;
                public Boolean retry;
            }

            public WCApiErrorDetails error;
        }

        public class WCApiError2 {
            public String uri;
            public String status;
            public String message;
        }

        /*
         * {
         * "data": {
         * "requestID": "a1d78f9c-90be-4adb-8ce8-b0d196c239d8"
         * }
         * }
         */
        public class WCActionResponseData {
            public String requestID;
        }

        public WCActionResponseData data;
        public WCApiError error;
    }

    public class WCParkingPosition {
        /*
         * {
         * "data": {
         * "lon": 6.83788,
         * "lat": 50.960526,
         * "carCapturedTimestamp": "2021-09-07T08:53:28Z"
         * }
         * }
         */
        public class WeConnectParkingPosition {
            public String carCapturedTimestamp;
            public String lon, lat;
        }

        WeConnectParkingPosition data;
    }

    public static class WCPendingRequest {
        public String vin = "";
        public String service = "";
        public String action = "";
        public String requestId = "";
        public String status = "";
        public String checkUrl = "";
        public Date creationTime = new Date();
        public long timeout = API_REQUEST_TIMEOUT_SEC;

        public WCPendingRequest(String vin, String service, String action, String requestId) {
            this.vin = vin;
            this.service = service;
            this.action = action;
            this.requestId = requestId;
            this.checkUrl = WCAPI_BASE_URL + "/vehicles/{2}/requests/" + requestId + "/status";
        }

        public static boolean isInProgress(String status) {
            String st = status.toLowerCase();
            return API_REQUEST_QUEUED.equals(st) || API_REQUEST_STARTED.equals(st);
        }

        public boolean isExpired() {
            Date currentTime = new Date();
            long diff = currentTime.getTime() - creationTime.getTime();
            return (diff / 1000) > timeout;
        }
    }
}
