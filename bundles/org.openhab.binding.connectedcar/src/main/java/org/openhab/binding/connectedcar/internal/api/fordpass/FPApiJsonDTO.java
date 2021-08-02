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
package org.openhab.binding.connectedcar.internal.api.fordpass;

import java.util.ArrayList;

import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPVehicleStatusData.FPVehicleStatus.FPStatusBattery.FPStatusBatHealth;

import com.google.gson.annotations.SerializedName;

/**
 * {@link FPApiJsonDTO} defines data formats for Ford Connect API
 *
 * @author Markus Michels - Initial contribution
 */
public class FPApiJsonDTO {
    public final static String FPSERVICE_ENGINE = "engine";
    public final static String FPSERVICE_DOORS = "doors";
    public final static String FPSERVICE_CLIMATISATION = "climatisation";
    public final static String FPSERVICE_CHARGER = "charger";

    public static class FPVehicleListData {
        public class FPVehicleData {
            public class FPVehicle {
                public String vin, nickName, vehicleType, color;
                public String modelName, modelCode, modelYear, make;
                public Integer tcuEnabled;
                public Integer ngSdnManaged;
                public Integer vehicleAuthorizationIndicator;
            }

            @SerializedName("$values")
            public ArrayList<FPVehicle> values;
        }

        public FPVehicleData vehicles;
        public String status;
        public String version;
    }

    public class FPVehicleStatusData {
        public class FPVehicleStatus {
            public class FPStatusStringValue {
                public String value;
                public String status;
                public String timestamp;
            }

            public class FPStatusIntValue {
                public Integer value;
                public String status;
                public String timestamp;
            }

            public class FPStatusDoubleValue {
                public Double value;
                public String status;
                public String timestamp;
            }

            public class FPStatusBooleanValue {
                public Boolean value;
                public String status;
                public String timestamp;
            }

            public class FPStatusGPS {
                /*
                 * "latitude":"26.9543817",
                 * "longitude":"-82.3306200",
                 * "gpsState":"UNSHIFTED",
                 * "status":"CURRENT",
                 * "timestamp":"07-29-2021 01:03:40"
                 */
                public Double latitude;
                public Double longitude;
                public String gpsState;
                public String status;
                public String timestamp;
            }

            public class FPStatusRemoteStart {
                /*
                 * "remoteStartDuration":0,
                 * "remoteStartTime":0,
                 * "status":"CURRENT",
                 * "timestamp":"07-29-2021 01:03:40"
                 */
                public Integer remoteStartDuration;
                public Integer remoteStartTime;
                public String status;
                public String timestamp;
            }

            public class FPStatusBattery {
                /*
                 * "batteryHealth":{
                 * "value":"STATUS_GOOD",
                 * "timestamp":"07-29-2021 01:03:21"
                 * },
                 * "batteryStatusActual":{
                 * "value":13,
                 * "status":"CURRENT",
                 * "timestamp":"07-29-2021 01:03:40"
                 * }
                 */

                public class FPStatusBatHealth {
                    public String value;
                    public String timestamp;
                }

                public class FPStatusBatActual {
                    public Integer value;
                    public String status;
                    public String timestamp;
                }

                public FPStatusBatHealth batteryHealth;
                public FPStatusBatActual batteryStatusActual;
            }

            public class FPStatusFuel {
                public Double fuelLevel;
                public Double distanceToEmpty;
                public String status;
                public String timestamp;
            }

            public class FPStatusOil {
                /*
                 * "oilLife":"STATUS_GOOD",
                 * "oilLifeActual":98,
                 * "status":"CURRENT",
                 * "timestamp":"07-29-2021 01:03:40"
                 */
                public String oilLife;
                public Integer oilLifeActual;
                public String status;
                public String timestamp;
            }

            public class FPStatusTirePres {
                /*
                 * "value":"STATUS_GOOD",
                 * "timestamp":"07-29-2021 01:03:21"
                 */
                public String value;
                public String timestamp;
            }

            public class FPTpms {
                FPStatusDoubleValue tirePressureByLocation;
                FPStatusStringValue tirePressureSystemStatus;
                FPStatusDoubleValue dualRearWheel;
                FPStatusStringValue leftFrontTireStatus;
                FPStatusStringValue leftFrontTirePressure;
                FPStatusStringValue rightFrontTireStatus;
                FPStatusDoubleValue rightFrontTirePressure;
                FPStatusStringValue outerLeftRearTireStatus;
                FPStatusStringValue outerLeftRearTirePressure;
                FPStatusStringValue outerRightRearTireStatus;
                FPStatusStringValue outerRightRearTirePressure;
                FPStatusStringValue innerLeftRearTireStatus;
                FPStatusStringValue innerLeftRearTirePressure;
                FPStatusStringValue innerRightRearTireStatus;
                FPStatusStringValue innerRightRearTirePressure;
                FPStatusStringValue recommendedRearTirePressure;
            }

            public class FPCssSettings {
                public String timestamp;
                public Integer location;
                public Integer vehicleConnectivity;
                public Integer vehicleData;
                public Integer drivingCharacteristics;
                public Integer contacts;
            }

            public class FPWindowPosition {
                FPStatusStringValue driverWindowPosition;
                FPStatusStringValue passWindowPosition;
                FPStatusStringValue rearDriverWindowPos;
                FPStatusStringValue rearPassWindowPos;
            }

            public class FPDoorStatus {
                FPStatusStringValue rightRearDoor;
                FPStatusStringValue leftRearDoor;
                FPStatusStringValue driverDoor;
                FPStatusStringValue passengerDoor;
                FPStatusStringValue hoodDoor;
                FPStatusStringValue tailgateDoor;
                FPStatusStringValue innerTailgateDoor;
            }

            public class FPDieselSystemStatus {
                /*
                 * Formats for those are missing, to be added
                 * "exhaustFluidLevel":null,
                 * "filterSoot":null,
                 * "ureaRange":null,
                 * "metricType":null,
                 * "filterRegenerationStatus":null
                 */
            }

            public String vin;
            public FPStatusStringValue lockStatus;
            public FPStatusStringValue alarm;
            @SerializedName("PrmtAlarmEvent")
            public FPStatusStringValue prmtAlarmEvent;
            public FPStatusDoubleValue odometer;
            public FPStatusFuel fuel;
            public FPStatusGPS gps;
            public FPStatusRemoteStart remoteStart;
            public FPStatusDoubleValue remoteStartStatus;
            public FPStatusBatHealth battery;
            public FPStatusOil oil;
            public FPStatusTirePres tirePressure;
            public String authorization;
            @SerializedName("TPMS")
            public FPTpms tpms;
            public FPStatusBooleanValue firmwareUpgInProgress;
            public FPStatusBooleanValue deepSleepInProgress;
            public FPCssSettings cssSettings;
            public String lastRefresh;
            public String lastModifiedDate;
            public String serverTime;
            public FPStatusStringValue outandAbout;
            public FPWindowPosition windowPosition;
            public FPDoorStatus doorStatus;
            public FPStatusStringValue ignitionStatus;
            public FPDieselSystemStatus dieselSystemStatus;
            public FPStatusDoubleValue batteryFillLevel;
            public FPStatusDoubleValue elVehDTE;
            public FPStatusStringValue chargingStatus;
            public FPStatusIntValue plugStatus;
            public FPStatusStringValue chargeStartTime;
            public FPStatusStringValue chargeEndTime;

            /*
             * Formats for those are missing, to be added
             * "hybridModeStatus":null,
             * "preCondStatusDsply":null,
             * "chargerPowertype":null,
             * "batteryPerfStatus":null,
             * "batteryChargeStatus":null,
             * "dcFastChargeData":null,
             * "batteryTracLowChargeThreshold":null,
             * "battTracLoSocDDsply":null,
             */
        }

        public FPVehicleStatus vehiclestatus;
        public String version;
        public String status;
    }
}
