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
package org.openhab.binding.connectedcar.internal.api.carnet;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link CarNetApiGSonDTO} defines helper classes for the Json to GSon mapping
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
public class CarNetApiGSonDTO {
    public static final String CNAPI_DEFAULT_API_URL = "https://msg.volkswagen.de/fs-car";

    // HTTP header attributes
    public static final String CNAPI_HEADER_APP = "X-App-Name";
    public static final String CNAPI_HEADER_VERS = "X-App-Version";
    public static final String CNAPI_HEADER_VERS_VALUE = "1.0.0";
    public static final String CNAPI_HEADER_USER_AGENT = "okhttp/3.11.0";
    public static final String CNAPI_HEADER_AUTHORIZATION = "Authorization";
    public static final String CNAPI_HEADER_CLIENTID = "X-Client-Id";

    // URIs: {0}=brand, {1} = country, {2} = VIN, {3} = userId
    public static final String CNAPI_VWG_MAL_1A_CONNECT = "https://mal-1a.prd.ece.vwg-connect.com/api";

    public static final String CNAPI_VW_TOKEN_URL = "https://mbboauth-1d.prd.ece.vwg-connect.com/mbbcoauth/mobile/oauth2/v1/token";
    public static final String CNAPI_URL_LOGIN = "https://login.apps.emea.vwapps.io/login/v1";

    public static final String CNAPI_SERVICE_APP_MEDIA = "appmedia_v1";
    public static final String CNAPI_SERVICE_CALENDAR = "app_calendar_v1";
    public static final String CNAPI_SERVICE_CAR_FINDER = "carfinder_v1";
    public static final String CNAPI_SERVICE_GEOFENCING = "geofence_v1";
    public static final String CNAPI_SERVICE_MOBILE_KEY = "mobilekey_v1";
    public static final String CNAPI_SERVICE_DESTINATIONS = "zieleinspeisung_v1";
    public static final String CNAPI_SERVICE_PICTURE_NAV1 = "picturenav_v1";
    public static final String CNAPI_SERVICE_PICTURE_NAV3 = "picturenav_v3";
    public static final String CNAPI_SERVICE_REMOTE_BATTERY_CHARGE = "rbatterycharge_v1";
    public static final String CNAPI_SERVICE_REMOTE_DEPARTURE_TIMER = "timerprogramming_v1";
    public static final String CNAPI_SERVICE_REMOTE_HEATING = "rheating_v1";
    public static final String CNAPI_SERVICE_REMOTE_HONK_AND_FLASH = "rhonk_v1";
    public static final String CNAPI_SERVICE_REMOTE_LOCK_UNLOCK = "rlu_v1";
    public static final String CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION = "rclima_v1";
    public static final String CNAPI_SERVICE_REMOTE_TRIP_STATISTICS = "trip_statistic_v1";
    public static final String CNAPI_SERVICE_SPEED_ALERT = "speedalert_v1";
    public static final String CNAPI_SERVICE_THEFT_ALARM = "dwap";
    public static final String CNAPI_SERVICE_TRAVELGUIDE = "travelguide_v1";
    public static final String CNAPI_SERVICE_VALET_ALERT = "valetalert_v1";
    public static final String CNAPI_SERVICE_VEHICLE_STATUS_REPORT = "statusreport_v1";

    // Service actions
    public static final String CNAPI_ACTION_CAR_FINDER_FIND_CAR = "FIND_CAR";
    public static final String CNAPI_ACTION_GEOFENCING_DELETE_VIOLATION = "D_ALERT";
    public static final String CNAPI_ACTION_GEOFENCING_GET_ALERTS = "G_ALERTS";
    public static final String CNAPI_ACTION_GEOFENCING_GET_DEFINITION_LIST = "G_DLIST";
    public static final String CNAPI_ACTION_GEOFENCING_GET_DEFINITION_LIST_STATUS = "G_DLSTATUS";
    public static final String CNAPI_ACTION_GEOFENCING_SAVE_DEFINITION_LIST = "P_DLIST";
    public static final String CNAPI_ACTION_MOBILE_KEY_ACCEPT_PERMISSION = "P_PERMISSION_ACCEPT";
    public static final String CNAPI_ACTION_MOBILE_KEY_CONFIRM_MOBILE_KEY_CREATION = "PU_MKCONFIRM";
    public static final String CNAPI_ACTION_MOBILE_KEY_CREATE_MOBILE_KEY = "P_MKCREATE";
    public static final String CNAPI_ACTION_MOBILE_KEY_GRANT_PERMISSION_CONFIRMATION_BY_VTAN = "P_VTAN";
    public static final String CNAPI_ACTION_MOBILE_KEY_GRANT_PERMISSION_START_PERMISSION = "P_PERMISSION";
    public static final String CNAPI_ACTION_MOBILE_KEY_READ_KEY_SYSTEM_USER_VIEW = "G_KEYSYSTEMUSERVIEW";
    public static final String CNAPI_ACTION_MOBILE_KEY_READ_MOBILE_KEYS = "G_MOBILEKEYS";
    public static final String CNAPI_ACTION_MOBILE_KEY_READ_PERMISSIONS = "G_PERMISSIONS";
    public static final String CNAPI_ACTION_MOBILE_KEY_READ_SERVICE_STATUS_VIEW = "G_SERVICESTATUSVIEW";
    public static final String CNAPI_ACTION_MOBILE_KEY_RETURN_MOBILE_KEY = "D_MOBILEKEY";
    public static final String CNAPI_ACTION_MOBILE_KEY_RETURN_PERMISSION = "D_PERMISSION_NUTZER";
    public static final String CNAPI_ACTION_MOBILE_KEY_REVOKE_PERMISSION = "D_PERMISSION";
    public static final String CNAPI_ACTION_MOBILE_KEY_UPDATE_PERMISSION_FRONTEND_ALIAS_PERMISSIONS = "PU_PERMISSION";
    public static final String CNAPI_ACTION_REMOTE_BATTERY_CHARGE_GET_STATUS = "G_STATUS";
    public static final String CNAPI_ACTION_REMOTE_BATTERY_CHARGE_START_CHARGING = "P_START";
    public static final String CNAPI_ACTION_REMOTE_BATTERY_CHARGE_START_CHARGING_NOW = "START_CHARGING_NOW";
    public static final String CNAPI_ACTION_REMOTE_BATTERY_CHARGE_START_CHARGING_NO_SET = "P_START_NOSET";
    public static final String CNAPI_ACTION_REMOTE_BATTERY_CHARGE_START_CHARGING_TIMER_BASED = "START_CHARGING_TIMER_BASED";
    public static final String CNAPI_ACTION_REMOTE_DEPARTURE_TIMER_GET_STATUS = "G_STATUS";
    public static final String CNAPI_ACTION_REMOTE_HEATING_GET_REQUEST_STATUS = "G_RQSTAT";
    public static final String CNAPI_ACTION_REMOTE_HEATING_GET_STATUS = "G_STAT";
    public static final String CNAPI_ACTION_REMOTE_HEATING_QUICK_START = "P_QSACT";
    public static final String CNAPI_ACTION_REMOTE_HEATING_QUICK_STOP = "P_QSTOPACT";
    public static final String CNAPI_ACTION_REMOTE_HEATING_SET_DEPARTURE_TIMERS = "P_DTACT";
    public static final String CNAPI_ACTION_REMOTE_HONK_AND_FLASH_PERFORM_REQUEST = "P_VREQ";
    public static final String CNAPI_ACTION_REMOTE_HONK_AND_FLASH_REQUEST_HISTORY = "G_RHIST";
    public static final String CNAPI_ACTION_REMOTE_HONK_AND_FLASH_REQUEST_STATUS = "G_REQSTATUS";
    public static final String CNAPI_ACTION_REMOTE_LOCK_UNLOCK_GET_LAST_ACTIONS = "G_LACT";
    public static final String CNAPI_ACTION_REMOTE_LOCK_UNLOCK_GET_REQUEST_STATUS = "G_RQSTAT";
    public static final String CNAPI_ACTION_REMOTE_LOCK_UNLOCK_LOCK = "LOCK";
    public static final String CNAPI_ACTION_REMOTE_LOCK_UNLOCK_UNLOCK = "UNLOCK";
    public static final String CNAPI_ACTION_REMOTE_PRETRIP_CLIMATISATION_GET_STATUS = "G_STATUS";
    public static final String CNAPI_ACTION_REMOTE_PRETRIP_CLIMATISATION_START_AUX_OR_AUTO = "P_START_CLIMA_AU";
    public static final String CNAPI_ACTION_REMOTE_PRETRIP_CLIMATISATION_START_ELECTRIC = "P_START_CLIMA_EL";
    public static final String CNAPI_ACTION_REMOTE_TRIP_STATISTICS_DELETE_STATISTICS = "D_TRIPDATA";
    public static final String CNAPI_ACTION_REMOTE_TRIP_STATISTICS_GET_STATISTICS = "G_TRIPDATA";
    public static final String CNAPI_ACTION_SPEED_ALERT_DELETE_VIOLATION = "D_ALERT";
    public static final String CNAPI_ACTION_SPEED_ALERT_GET_ALERTS = "G_ALERTS";
    public static final String CNAPI_ACTION_SPEED_ALERT_GET_DEFINITION_LIST = "G_DLIST";
    public static final String CNAPI_ACTION_SPEED_ALERT_GET_DEFINITION_LIST_STATUS = "G_DLSTATUS";
    public static final String CNAPI_ACTION_SPEED_ALERT_SAVE_DEFINITION_LIST = "P_DLIST";
    public static final String CNAPI_ACTION_THEFT_ALARM_DELETE_WARNING = "D_NHIST";
    public static final String CNAPI_ACTION_THEFT_ALARM_GET_WARNINGS = "G_NHIST";
    public static final String CNAPI_ACTION_VALET_ALERT_DELETE_DEFINITION = "D_DEF";
    public static final String CNAPI_ACTION_VALET_ALERT_DELETE_VIOLATION = "D_ALERT";
    public static final String CNAPI_ACTION_VALET_ALERT_GET_ALERTS = "G_ALERTS";
    public static final String CNAPI_ACTION_VALET_ALERT_GET_DEFINITION = "G_DEF";
    public static final String CNAPI_ACTION_VALET_ALERT_GET_DEFINITION_STATUS = "G_DSTATUS";
    public static final String CNAPI_ACTION_VALET_ALERT_SAVE_DEFINITION = "P_DEF";
    public static final String CNAPI_ACTION_VEHICLE_STATUS_REPORT_GET_CURRENT_VEHICLE_DATA = "G_CVDATA";
    public static final String CNAPI_ACTION_VEHICLE_STATUS_REPORT_GET_CURRENT_VEHICLE_DATA_BY_ID = "G_CVDATAID";
    public static final String CNAPI_ACTION_VEHICLE_STATUS_REPORT_GET_REQUEST_STATUS = "G_RQSTAT";
    public static final String CNAPI_ACTION_VEHICLE_STATUS_REPORT_GET_STORED_VEHICLE_DATA = "G_SVDATA";

    public static final String CNAPI_CMD_FLASH = "FLASH_ONLY";
    public static final String CNAPI_CMD_HONK_FLASH = "HONK_AND_FLASH";

    public static final String CNAPI_HEATER_SOURCE_AUX = "auxiliary";
    public static final String CNAPI_HEATER_SOURCE_ELECTRIC = "electric";
    public static final String CNAPI_HEATER_SOURCE_AUTOMATIC = "automatic";

    public static final String CNAPI_SERVICE_TRIPSTATS = "tripstatistics";
    public static final String CNAPI_TRIP_SHORT_TERM = "shortTerm";
    public static final String CNAPI_TRIP_LONG_TERM = "longTerm";

    public static class CNApiError1 {
        /*
         * {
         * "error":"invalid_request",
         * "error_description": "Missing Username"
         * }
         */
        public @Nullable String error;
        @SerializedName("error_code")
        public @Nullable String code;
        @SerializedName("error_description")
        public @Nullable String description;
    }

    public static class CNApiError2 {
        /*
         * {"error":{"errorCode":"gw.error.validation","description":"Invalid Request"}}
         * "error": { "errorCode": "mbbc.rolesandrights.invalidSecurityPin", "description":
         * "The Security PIN is invalid.", "details": { "challenge": "", "user": "dYeJ7CoMzqV0obHyRZJSyzkb9d11",
         * "reason": "SECURITY_PIN_INVALID", "delay": "0" } }}
         */
        public class CNErrorMessage2 {
            public String error = "";
            @SerializedName("errorCode")
            public String code = "";
            @SerializedName("description")
            public String description = "";
            public CNErrorMessage2Details details = new CNErrorMessage2Details();;
        }

        public @Nullable CNErrorMessage2 error;
    }

    public static class CNErrorMessage2Details {
        public @Nullable String challenge = "";
        public @Nullable String user = "";
        public @Nullable String reason = "";
        public @Nullable String delay = "";
    }

    public class CarNetClientRegisterResult {
        @SerializedName("client_id")
        public String clientId;
    }

    public static class CarNetSecurityPinAuthInfo {
        public static class CNSecurityPinAuthInfo {
            public class CNSecurityPinTransmission {
                public Integer hashProcedureVersion;
                public String challenge;
            }

            public String securityToken;
            public Integer remainingTries;
            public CNSecurityPinTransmission securityPinTransmission;
        }

        public CNSecurityPinAuthInfo securityPinAuthInfo;
    }

    public static class CarNetPersonalData {
        // {"businessIdentifierType":"BUSINESS_IDENTIFIER_TYPE:MBB_ID","businessIdentifierValue":"dYeJ7CoMzqV0obHyRZJSXXXXXXXX"}
        public String businessIdentifierType;
        public String businessIdentifierValue;
    }

    public static class CarNetMbbStatus {
        // {"profileCompleted":true,"spinDefined":false,"carnetEnrollmentCountry":"DE","mbbUserId":"dYeJ7CoMzqV0obHyRZJSXXXXXXXX"}
        public Boolean profileCompleted = false;
        public Boolean spinDefined = false;
        public String carnetEnrollmentCountry = "";
        public String mbbUserId = "";
    }

    public static class CarNetSecurityPinAuthentication {
        public static class CNSecuritxPinAuth {
            public class CNSecurityPin {
                public String challenge;
                public String securityPinHash;
            }

            public CNSecurityPin securityPin = new CNSecurityPin();
            public String securityToken = "";
        }

        public CNSecuritxPinAuth securityPinAuthentication = new CNSecuritxPinAuth();
    }

    public static class CNContentString {
        public String timestamp;
        public String content;
    }

    public static class CNContentInt {
        public String timestamp;
        public Integer content;
    }

    public static class CNContentDouble {
        public String timestamp;
        public Double content;
    }

    public static class CNContentBool {
        public String timestamp;
        public Boolean content;
    }

    public static class CarNetOidcConfig {
        // OpenID Connect Configuration
        public String issuer;
        public String authorization_endpoint;
        public String token_endpoint;
        public String revocation_endpoint;
        public String end_session_endpoint;
        public String jwks_uri;
        public String userinfo_endpoint;
        public String[] response_types_supported;
        public String[] subject_types_supported;
        public String[] id_token_signing_alg_values_supported;
        public String[] code_challenge_methods_supported;
        public String[] scopes_supported;
        public String[] claims_supported;
        public String[] ui_locales_supported;
        public String[] grant_types_supported;
        public String[] acr_values_supported;
        public String[] token_endpoint_auth_methods_supported;
    }

    public static class CarNetHomeRegion {
        public class CNHomeRegion {
            public class CNBaseUri {
                public String systemId;
                public String content;
            }

            CNBaseUri baseUri;
        }

        public CNHomeRegion homeRegion;
    }

    public static class CNPairingInfo {
        public static class CarNetPairingInfo {
            public String pairingStatus;
            public String xmlns;
            public String userId;
            public String pairingCode;
            public String vin;

            public boolean isPairingCompleted() {
                return pairingStatus != null && "PAIRINGCOMPLETE".equalsIgnoreCase(pairingStatus);
            }
        }

        public CarNetPairingInfo pairingInfo;
    }

    public static class CNRoleRights {
        /*
         * "role":{
         * "content":"PRIMARY_USER",
         * "status":"ENABLED",
         * "securityLevel":"HG_2b"
         * }
         */
        public class CarNetUserRoleRights {
            public String content;
            public String status;
            public String securityLevel;
        }

        public CarNetUserRoleRights role;
    }

    public static class CNVehicleData {
        public class CarNetVehicleData {
            public class CNVehicleDeviceList {
                public class CNVecileDevice {
                    public class CNEmbeddedSIM {
                        public class CNSimIdentification {
                            public String type;
                            public String content;
                        }

                        CNSimIdentification identification;
                    }

                    public String deviceType;
                    public String deviceId;
                    public String ecuGeneration;
                    public CNEmbeddedSIM embeddedSim;
                    public String imei;
                    public String mno;
                }

                public ArrayList<CNVecileDevice> vehicleDevice;
            }

            public String systemId;
            public String requestId;
            public String brand;
            public String country;
            public String vin;
            public Boolean isConnect;
            public Boolean isConnectSorglosReady;
            public CNVehicleDeviceList vehicleDevices;
        }

        CarNetVehicleData vehicleData;
    }

    public static class CarNetVehicleList {
        public static class CNVehicles {
            public ArrayList<String> vehicle;
        }

        public CNVehicles userVehicles;
    }

    public static class CNVehicleDetails {
        public static class CarNetVehicleDetails {
            public String systemId;
            public String requestId;
            public String brand;
            public String vin;
            public String country;
            public Boolean isConnect;
            public Boolean isConnectSorglosReady;

            public static class CNCarPortData {
                public String modelCode;
                public String modelName;
                public String modelYear;
                public String color;
                public String countryCode;
                public String engine;
                public String mmi;
                public String transmission;
            }

            public CNVehicleData.CarNetVehicleData.CNVehicleDeviceList.CNVecileDevice vehicleDevices;
            public CNCarPortData carportData;
        }

        CarNetVehicleDetails vehicleDataDetail;
    }

    public static class CarNetVehicleStatus {
        public static class CNStoredVehicleDataResponse {
            public static class CNVehicleData {
                public static class CNStatusData {
                    public static class CNStatusField {
                        public String id;
                        public String tsCarSentUtc;
                        public String tsCarSent;
                        public String tsCarCaptured;
                        public String tsTssReceivedUtc;
                        public Integer milCarCaptured;
                        public Integer milCarSent;
                        public String value;
                        public String unit;
                    }

                    public String id;
                    @SerializedName("field")
                    public ArrayList<CNStatusField> fields;
                }

                public ArrayList<CNStatusData> data;
            }

            public String vin;
            public CNVehicleData vehicleData;
        }

        @SerializedName("StoredVehicleDataResponse")
        public CNStoredVehicleDataResponse storedVehicleDataResponse;
    }

    public static class CarNetCoordinate {
        public Integer latitude = 0;
        public Integer longitude = 0;
    }

    public static class CNFindCarResponse {
        public static class CarNetVehiclePosition {
            public static class CNPosition {

                public CarNetCoordinate carCoordinate;
                public String timestampCarSent;
                public String timestampTssReceived;
            }

            @SerializedName("Position")
            public CNPosition carPosition;
            public String parkingTimeUTC;
        }

        public CarNetVehiclePosition findCarResponse;

        public CarNetCoordinate getCoordinate() {
            return findCarResponse.carPosition != null && findCarResponse.carPosition.carCoordinate != null
                    ? findCarResponse.carPosition.carCoordinate
                    : new CarNetCoordinate();
        }
    }

    public static class CNStoredPosition {
        public static class CarNetStoredPosition {
            public static class CNPosition {
                public static class CNPositionHeading {
                    public Integer direction;
                }

                public CNPositionHeading heading;
                public CarNetCoordinate carCoordinate;
            }

            public String parkingTimeUTC;
            public CNPosition position;
        }

        public CarNetStoredPosition storedPositionResponse;

        public CarNetCoordinate getCoordinate() {
            return storedPositionResponse.position != null && storedPositionResponse.position.carCoordinate != null
                    ? storedPositionResponse.position.carCoordinate
                    : new CarNetCoordinate();
        }
    }

    public static class CarNetHistory {
        public String destinations;
    }

    public static class CarNetActionResponse {
        public static class CNActionResponse {
            public class CNRluActionResponse {
                public String requestId;
                public String vin;
            }

            public class CNRclimaActionResponse {
                public class CNRclimaSettings {
                    public boolean climatisationWithoutHVpower;
                    public String heaterSource;
                }

                public String type;
                public String actionId;
                public String actionState;
                CNRclimaSettings settings;
            }

            public class CNRheatActionResponse {
                public String requestId;
                public String vin;
            }

            public class CarNetCurrentVehicleData {
                public String requestId;
                public String vin;
            }

            @SerializedName("CurrentVehicleDataResponse")
            public CarNetCurrentVehicleData currentVehicleDataResponse;
            public CNRluActionResponse rluActionResponse;
            public CNRclimaActionResponse action;
            public CNRheatActionResponse performActionResponse;
            public CNHonkFlashResponse.CarNetHonkFlashResponse honkAndFlashRequest;
        }

        public String requestId;
        public String vin;
    }

    public static class CNHonkFlashResponse {
        /*
         * {"honkAndFlashRequest":{"lastUpdated":"2021-05-26T20:11:37Z","serviceDuration":15,"userPosition":{"latitude":
         * 59222078,"longitude":18001326},"id":4937451,"serviceOperationCode":"FLASH_ONLY","status":{"statusCode":
         * "REQUEST_IN_PROGRESS"}}}
         */
        public static class CarNetHonkFlashResponse {
            public static class CNFonkFlashStatusCode {
                // {'status': {'statusCode': 'REQUEST_IN_PROGRESS'}}
                public String statusCode;
            }

            public String id;
            public String lastUpdated;
            public Integer serviceDuration;
            public String serviceOperationCode;
            public CarNetCoordinate userPosition;
            public CNFonkFlashStatusCode status;
        }

        public CarNetHonkFlashResponse honkAndFlashRequest;
    }

    public static class CNEluActionHistory {
        public class CarNetRluHistory {
            public class CarNetRluLockActionList {
                public class CarNetRluLockAction {
                    public class CNRluLockStatus {
                        public class CNRluLockEntry {
                            public Boolean valid;
                            public Boolean locked;
                            public Boolean open;
                            public Boolean safe; // maybe empty
                        }

                        CNRluLockEntry driverDoor;
                        CNRluLockEntry coDriverDoor;
                        CNRluLockEntry driverRearDoor;
                        CNRluLockEntry coDriverRearDoor;
                        CNRluLockEntry frontLid;
                        CNRluLockEntry boot;
                        CNRluLockEntry flap;
                    }

                    public String operation;
                    public String timestamp;
                    public String channel;
                    public String rluResult;
                    CNRluLockStatus lockStatus;
                }

                public ArrayList<CarNetRluLockAction> action;
            }

            public String vin;
            public String steeringWheelSide;
            public String doorModel;
            public CarNetRluLockActionList actions;
        }

        public CarNetRluHistory actionsResponse;
    }

    public static class CNOperationList {
        public class CarNetOperationList {
            public String vin;
            public String channelClient;
            public String userId;
            public String role;
            public String securityLevel;
            public String status;

            public class CarNetServiceInfo {
                public class CNServiceStatus {
                    public String status;
                }

                public class CNServiceOperation {
                    public String id;
                    public String version;
                    public String permission;
                    public String requiredRole;
                    public String requiredSecurityLevel;
                }

                public class CNServiceUrl {
                    public String content;
                }

                public class CNComulativeLicense {
                    public String status;
                }

                public String serviceId;
                public String serviceType;
                public CNServiceStatus serviceStatus;
                public Boolean licenseRequired;
                public CNComulativeLicense cumulatedLicense;
                public Boolean primaryUserRequired;
                public String serviceEol;
                public Boolean rolesAndRightsRequired;
                public CNServiceUrl invocationUrl;
                public ArrayList<CNServiceOperation> operation;
            }

            public ArrayList<CarNetServiceInfo> serviceInfo;
        }

        public CarNetOperationList operationList;
    }

    public static class CarNetServiceAvailability {
        public boolean statusData = true;
        public boolean rlu = true;
        public boolean clima = true;
        public boolean charger = true;
        public boolean carFinder = true;
        public boolean tripData = true;
        public boolean destinations = true;
    }

    public static class CNDestinations {
        public class CarNetDestination {
            public class CNDestinationAddress {
                public String addressType;
                public String city;
                public String country;
                public String street;
                public String zipCode;
            }

            public class CNDestinationGeo {
                public Double latitude;
                public Double longitude;

                public double getLattitude() {
                    // return (latitude != null) ? latitude / 1000000.0 : 0;
                    return (latitude != null) ? latitude : 0;
                }

                public double getLongitude() {
                    // return (longitude != null) ? longitude / 1000000.0 : 0;
                    return (longitude != null) ? longitude : 0;
                }
            }

            public class CNDestinationPOI {
                public String lastName;
                // "phoneData":[{"phoneType":"2"} ]}
            }

            public String destinationName = "";
            public Boolean immediateDestination;
            public String id;
            public CNDestinationAddress address;
            public String destinationSource;
            public CNDestinationGeo geoCoordinate;
            public CNDestinationPOI POIContact;
            public String fetchStatus;
        }

        public static class CarNetDestinationList {
            public ArrayList<CarNetDestination> destination;
        }

        public CarNetDestinationList destinations;
    }

    public static class CarNetTripData {
        public class CarNetTripDataList {
            public class CarNetTripDataEntry {
                public String tripType;
                public String tripID;
                public Integer averageElectricEngineConsumption;
                public Double averageFuelConsumption;
                public Integer averageSpeed;
                public Integer mileage;
                public Integer startMileage;
                public Double traveltime;
                public String timestamp;
                public String reportReason;
                public Integer overallMileage;
            }

            public ArrayList<CarNetTripDataEntry> tripData;
        }

        public CarNetTripDataList tripDataList;
    }

    public static class CNChargerInfo {
        public class CarNetChargerStatus {
            public class CNChargerSettings {
                public class CNCargerModeSel {
                    public CNContentString modificationState;
                    public CNContentString modificationReason;
                    public CNContentString value;
                }

                public CNContentInt maxChargeCurrent;
                public CNCargerModeSel chargeModeSelection;
            }

            public class CNChargerStatus {
                public class CarNetChargerStatusData {
                    public CNContentString chargingMode;
                    public CNContentInt chargingStateErrorCode;
                    public CNContentString chargingReason;
                    public CNContentString externalPowerSupplyState;
                    public CNContentString energyFlow;
                    public CNContentString chargingState;
                }

                public class CNChargerRangeStatusData {
                    public CNContentString engineTypeFirstEngine;
                    public CNContentInt primaryEngineRange;
                    public CNContentInt hybridRange;
                    public CNContentString engineTypeSecondEngine;
                    public CNContentInt secondaryEngineRange;
                }

                public class CNChargerLedStatusData {
                    public CNContentString ledColor;
                    public CNContentString ledState;
                }

                public class CNBatteryStatusData {
                    public CNContentInt stateOfCharge;
                    public CNContentInt remainingChargingTime;
                    public CNContentString remainingChargingTimeTargetSOC;
                }

                public class CNPlugStatusData {
                    public CNContentString plugState;
                    public CNContentString lockState;
                }

                public CarNetChargerStatusData chargingStatusData;
                public CNChargerRangeStatusData cruisingRangeStatusData;
                public CNChargerLedStatusData ledStatusData;
                public CNBatteryStatusData batteryStatusData;
                public CNPlugStatusData plugStatusData;
            }

            public CNChargerSettings settings;
            public CNChargerStatus status;
        }

        CarNetChargerStatus charger;
    }

    public static class CarNetClimaterTimer {
        public class CNTimerProfileList {
            public class CNTimerProfile {
                public class CNTimerProfileEntry {
                    public String timestamp;
                    public String profileName;
                    public String profileID;
                    public Boolean operationCharging;
                    public Boolean operationClimatisation;
                    public String targetChargeLevel;
                    public Boolean nightRateActive;
                    public String nightRateTimeStart;
                    public String nightRateTimeEnd;
                    public String chargeMaxCurrent;
                    public String heaterSource;
                }

                public ArrayList<CNTimerProfileEntry> timerProfile;
            }

            CNTimerProfile timerProfileList;
        }

        public class CNTimerList {
            public class CNTimerEntryList {
                public class CNTimerEntry {
                    public String timestamp;
                    public String timerID;
                    public String timerProgrammedStatus;
                    public String timerFrequency;
                    public String departureDateTime;
                }

                public ArrayList<CNTimerEntry> timer;
            }

            CNTimerEntryList timerList;
        }

        public class CNStatusTimerList {
            public class CNStatusTimerEntry {
                CNContentString timerChargeScheduleStatus;
                CNContentString timerClimateScheduleStatus;
                CNContentString timerExpiredStatus;
                CNContentString instrumentClusterTime;
            }

            public ArrayList<CNStatusTimerEntry> timer;
        }

        public class CNTimerAndProfileList {
            public class CNZoneSettings {
                public class CNZoneSettingList {
                    public class CNZoneSettingEntry {
                        public class CNZoneValue {
                            Boolean isEnabled;
                            String position;
                        }

                        public String timestamp;
                        public CNZoneValue value;
                    }

                    public ArrayList<CNZoneSettingEntry> zoneSetting;
                }

                CNZoneSettingList zoneSettings;
            }

            public class CNClimateElementSettings {
                CNContentBool isClimatisationAtUnlock;
                CNContentBool isMirrorHeatingEnabled;
                CNZoneSettings zoneSettings;
            }

            public class CNBasicTimerSettings {
                public String timestamp;
                public String heaterSource;
                public String chargeMinLimit;
                CNClimateElementSettings climaterElementSettings;
            }

            public class CNStatusTimer {
                public String timerID;
            }

            CNTimerProfileList timerProfileList;
            CNTimerList timerList;
            CNBasicTimerSettings timerBasicSetting;
            CNStatusTimer status;
        }
    }

    public static class CNClimater {
        public class CarNetClimaterStatus {
            public class CNClimaterSettings {
                public class CNClimaterElementSettings {
                    public class CNClimaterZoneSettingsList {
                        public class CNClimaterZoneSetting {
                            public class CNClZoneSetValue {
                                public Boolean isEnabled;
                                public String position;
                            }

                            public String timestamp;
                            public CNClZoneSetValue value;
                        }

                        public CNContentBool isClimatisationAtUnlock;
                        public CNContentBool isMirrorHeatingEnabled;
                        public ArrayList<CNClimaterZoneSetting> zoneSetting;
                    }

                    CNClimaterElementSettings zoneSettings;
                }

                public CNContentDouble targetTemperature;
                public CNContentBool climatisationWithoutHVpower;
                public CNContentString heaterSource;
            }

            public class CNClimaterStatus {
                public class CarNetClimaterStatusData {
                    public class CarNetClimaterZoneState {
                        public class CNClimaterZonState {
                            public Boolean isActive;
                            public String position;
                        }

                        public String timestamp;
                        public CNClimaterZonState value;
                    }

                    public class CNClimaterElementState {
                        public class CarNetClimaterZoneStateList {
                            public ArrayList<CarNetClimaterZoneState> zoneState;
                        }

                        public CNContentBool isMirrorHeatingActive;
                        public CNContentBool extCondAvailableFL;
                        public CNContentBool extCondAvailableFR;
                        public CNContentBool extCondAvailableRL;
                        public CNContentBool extCondAvailableRR;
                        public CarNetClimaterZoneStateList zoneStates;
                    }

                    public CNContentString climatisationState;
                    public CNContentInt climatisationStateErrorCode;
                    public CNContentInt remainingClimatisationTime;
                    public CNContentString climatisationReason;
                    public CNClimaterElementState climatisationElementStates;
                }

                public class CNTemperatureStatusData {
                    public CNContentInt outdoorTemperature;
                }

                public class CNParkingClockStatusData {
                    public CNContentString vehicleParkingClock;
                }

                public CarNetClimaterStatusData climatisationStatusData;
                public CNTemperatureStatusData temperatureStatusData;
                public CNParkingClockStatusData vehicleParkingClockStatusData;
            }

            public CNClimaterSettings settings;
            public CNClimaterStatus status;
        }

        CarNetClimaterStatus climater;
    }

    public static class CNSpeedAlertConfig {
        public static class CarNetSpeedAlertConfig {
            /*
             * "minimumSpeedLimit":"0",
             * "debouncePostTime":"30",
             * "maximumSpeedLimit":"161",
             * "timeoutJobProcessing":"120",
             * "persistenceTimeHistoryEntries":"60",
             * "maximalNumberDefinitions":"10",
             * "timeoutJobDelivery":"100",
             * "fnsUserTypes":"PRIMARY_USER",
             * "reviewLocation":false,
             * "maximalNumberHistoryEntries":"100",
             * "maximalNumberActiveDefinitions":"2",
             * "debouncePreTime":"10"
             */
            public String minimumSpeedLimit;
            public String debouncePostTime;
            public String maximumSpeedLimit;
            public String timeoutJobProcessing;
            public String persistenceTimeHistoryEntries;
            public String maximalNumberDefinitions;
            public String timeoutJobDelivery;
            public String fnsUserTypes;
            public Boolean reviewLocation;
            public String maximalNumberHistoryEntries;
            public String maximalNumberActiveDefinitions;
            public String debouncePreTime;
        }

        public CarNetSpeedAlertConfig speedAlertConfiguration;
    }

    public static class CNSpeedAlerts {
        public static class CarNetSpeedAlerts {
            public static class CarNetpeedAlertEntry {
                /*
                 * "id":"1548989",
                 * "alertType":"START_EXCEEDING",
                 * "definitionId":"135965",
                 * "definitionName":"Benachrichtigung 50 km/h",
                 * "occurenceDateTime":"2021-05-26T16:16:54Z",
                 * "speedLimit":"50"
                 *
                 */
                public String id;
                public String alertType;
                public String definitionName;
                public String occurenceDateTime;
                public String speedLimit;
            }

            public ArrayList<CarNetpeedAlertEntry> speedAlert = new ArrayList<>();
        }

        public CarNetSpeedAlerts speedAlerts;
    }

    public static class CNGeoFenceAlertConfig {
        public class CarNetGeoFenceConfig {
            /*
             * "debouncePostTime":"10",
             * "timeoutJobProcessing":"120",
             * "persistenceTimeHistoryEntries":"60",
             * "maximalNumberDefinitions":"10",
             * "timeoutJobDelivery":"100",
             * "fnsUserTypes":"PRIMARY_USER",
             * "reviewLocation":false,
             * "maximalNumberHistoryEntries":"100",
             * "spatialTolerance":"25",
             * "maximalNumberActiveDefinitions":"4",
             * "debouncePreTime":"10"
             */
            public String debouncePostTime;
            public String persistenceTimeHistoryEntries;
            public String maximalNumberDefinitions;
            public String timeoutJobDelivery;
            public String fnsUserTypes;
            public Boolean reviewLocation;
            public String maximalNumberHistoryEntries;
            public String spatialTolerance;
            public String maximalNumberActiveDefinitions;
            public String debouncePreTime;
        }

        public CarNetGeoFenceConfig geofencingConfiguration;
    }

    public static class CNGeoFenceAlerts {
        public static class CarNetGeoFenceAlerts {
            public static class CarNetGeoFenceAlertEntry {
                /*
                 * "id":"1776965",
                 * "alertType":"ENTER_REDZONE",
                 * "definitionId":"182818",
                 * "definitionName":"Nach Hause",
                 * "occurenceDateTime":"2021-05-26T16:18:18Z"
                 */
                public String id;
                public String alertType;
                public String definitionName;
                public String occurenceDateTime;
            }

            public ArrayList<CarNetGeoFenceAlertEntry> geofencingAlert = new ArrayList<>();
        }

        public CarNetGeoFenceAlerts geofencingAlerts;
    }

    public static class CNHeaterVentilation {
        public class CarNetHeaterVentilationStatus {
            public class ClimatisationStateReport {
                public String climatisationState;
                public Integer climatisationDuration;
                public Integer remainingClimateTime;
                public Integer climateStatusCode;
            }

            public ClimatisationStateReport climatisationStateReport;
        }

        CarNetHeaterVentilationStatus statusResponse;
    }

    /*
     * public static class CNVehicleSpec {
     * public class CarNetVehicleSpec {
     * public class CarNetUserVehicles {
     * public class CNUserRole {
     * String role;
     * }
     *
     * public class CarNetVehicle {
     * public class CNVehicleCore {
     * public String commissionNumber;
     * public String modelYear;
     * public String exteriorColorId;
     * }
     *
     * public class CNVehicleClassification {
     * public String driveTrain;
     * public String modelRange;
     * }
     *
     * public class CarNetVehicleMedia {
     * public String shortName;
     * public String longName;
     * public String exteriorColor;
     * public String interiorColor;
     * }
     *
     * public class CarNetVehiclePicture {
     * public String mediaType;
     * public String url;
     * }
     *
     * public class CNVehicleHifa {
     * public String factoryPickupDateFrom;
     * public String factoryPickupDateTill;
     * public String fbDestination;
     * }
     *
     * public class CNVehiclePdw {
     * Boolean pdwVehicle;
     * }
     *
     * public class CarNetEquipment {
     * public String code;
     * public String name;
     * public String categoryId;
     * public String categoryName;
     * public String subCategoryId;
     * public String subCategoryName;
     * public String teaserImage;
     * public Boolean standard;
     * }
     *
     * public class CarNetTechSpec {
     * public String key;
     * public String name;
     * public String value;
     * public String groupId;
     * public String groupName;
     * }
     *
     * public class CNConsumption {
     * public class CNWltps {
     * public class CNAttribute {
     * public String attributeId;
     * public String scaleUnit;
     * public String value;
     * }
     *
     * public String attributeGroup;
     * public ArrayList<CNAttribute> attributes;
     * }
     *
     * public class CNTechnicalSpecification {
     * public String key;
     * public String name;
     * public String value;
     * public String unit;
     * public String groupId;
     * public String groupName;
     * }
     *
     * public ArrayList<CNWltps> wltps;
     * public ArrayList<CNTechnicalSpecification> technicalSpecifications;
     * }
     *
     * public CNVehicleCore core;
     * public CNVehicleClassification classification;
     * public CarNetVehicleMedia media;
     * public ArrayList<CarNetVehiclePicture> renderPictures;
     * public CNVehicleHifa hifa;
     * public CNVehiclePdw pdw;
     * public ArrayList<CarNetEquipment> equipments;
     * public ArrayList<CarNetTechSpec> techSpecs;
     * public CNConsumption consumption;
     * }
     *
     * public String csid;
     * public String vin;
     * public Boolean owner;
     * public String type;
     * public String devicePlatform;
     * public Boolean mbbConnect;
     * public CNUserRole userRole;
     * }
     *
     * public ArrayList<CarNetUserVehicles> userVehicles;
     * }
     *
     * public CarNetVehicleSpec data;
     * }
     */
    public static class CNRequestStatus {
        // {"requestStatusResponse":{"vin":"WAUZZZF21LN046449","status":"request_not_found"}}
        // {"requestStatusResponse":{"vin":"WAUZZZF21LN046449","status":"request_fail","error":200}}
        public class CarNetRequestStatus {
            public String vin;
            public String status;
            public String resultData;
            public Integer error;
        }

        // {"action":{"type":"startClimatisation","actionId":26713297,"actionState":"queued"}}
        public class CarNetActionStatus {
            public String type;
            public String actionId;
            public String actionState;
            public Integer errorCode;
        }

        public CarNetRequestStatus requestStatusResponse;
        public CarNetActionStatus action;
        public CNHonkFlashResponse.CarNetHonkFlashResponse.CNFonkFlashStatusCode status;
    }

    public static class CarNetImageUrlsVW {
        public static class CNImageUrl {
            public String url;
            public String viewDirection;
            public String angle;
        }

        public String service;
        public String[] imageUrls;
        public ArrayList<CNImageUrl> images;
    }
}
