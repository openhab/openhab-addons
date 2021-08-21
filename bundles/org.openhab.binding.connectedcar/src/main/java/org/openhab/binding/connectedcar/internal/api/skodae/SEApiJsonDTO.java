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
package org.openhab.binding.connectedcar.internal.api.skodae;

import java.util.ArrayList;

/**
 * {@link SEApiJsonDTO} defines the Skoda-E data formats
 *
 * @author Markus Michels - Initial contribution
 */
public class SEApiJsonDTO {
    public static final String SESERVICE_STATUS = "status";
    public static final String SESERVICE_CLIMATISATION = "air-conditioning";
    public static final String SESERVICE_CHARGING = "charging";

    public static final String SEENDPOINT_STATUS = "status";
    public static final String SEENDPOINT_SETTINGS = "settings";

    public static class SEVehicleList {
        public static class SEVehicle {
            public class SEVehicleSpec {
                public class SEBatterySpec {
                    public Integer capacityInKWh;
                }

                public class SEEngine {
                    public String type;
                    public Integer powerInKW;
                }

                public class SEGearBox {
                    public String type;
                }

                public String title;
                public String brand;
                public String model;
                public String body;
                public String systemCode;
                public String systemModelId;
                public SEEngine engine;
                public SEBatterySpec battery;
                public SEGearBox gearbox;
                public String trimLevel;
                public String manufacturingDate;
                public String devicePlatform;
                public Integer maxChargingPowerInKW;
            }

            public class SEConnectivities {
                public class SEConnDetail {
                    public class SEConnRemote {
                        public String state;
                    }

                    SEConnRemote remote;
                }

                public String type;
                public SEConnDetail detail;
            }

            public class SECapabilities {
                public String id;
                public String serviceExpiration;
                public String[] statuses;
                public Boolean canBeDisabledByUser;
            }

            public String id;
            public String vin;
            public String lastUpdatedAt;
            public SEVehicleSpec specification;
            public ArrayList<SEConnectivities> connectivities;
            public ArrayList<SECapabilities> capabilities;
        }

        public ArrayList<SEVehicle> data;
    }

    public static class SEVehicleSettings {
        public static class SEChargerSettings {
            // {
            // "autoUnlockPlugWhenCharged": "Permanent",
            // "maxChargeCurrentAc": "Maximum",
            // "targetStateOfChargeInPercent": 100
            // }
            public String autoUnlockPlugWhenCharged;
            public String maxChargeCurrentAc;
            public Integer targetStateOfChargeInPercent;
        }

        public static class SEClimaterSettings {
            /*
             * {
             * "targetTemperatureInKelvin":294.15,
             * "temperatureConversionTableUsed":"CELSIUS_KELVIN",
             * "airConditioningAtUnlock":true,
             * "windowHeatingEnabled":false,
             * "zonesSettings":{
             * "frontLeftEnabled":false,
             * "frontRightEnabled":false
             * }
             * }
             */
            public class SEClimaZoneSettings {
                public Boolean frontLeftEnabled;
                public Boolean frontRightEnabled;
            }

            public Double targetTemperatureInKelvin;
            public String temperatureConversionTableUsed;
            public Boolean airConditioningAtUnlock;
            public Boolean windowHeatingEnabled;
            public SEClimaZoneSettings zonesSettings;
        }

        SEChargerSettings charger;
        SEClimaterSettings climater;
    }

    public static class SEVehicleStatusData {
        public static class SEVehicleStatus {
            public static class SEChargerStatus {
                public static class SEPlugStatus {
                    public String connectionState;
                    public String lockState;
                }

                public static class SEChargingStatus {
                    public String state;
                    public Long remainingToCompleteInSeconds;
                    public Double chargingPowerInWatts;
                    public Double chargingRateInKilometersPerHour;
                    public String chargingType;
                    public String chargeMode;
                }

                public static class SEBatteryStatus {
                    public Long cruisingRangeElectricInMeters;
                    public Integer stateOfChargeInPercent;
                }

                public SEPlugStatus plug;
                public SEChargingStatus charging;
                public SEBatteryStatus battery;
            }

            public static class SEClimaterStatus {
                /*
                 * {
                 * "remainingTimeToReachTargetTemperatureInSeconds":0,
                 * "state":"Off","trigger":"OFF",
                 * "windowsHeatingStatuses":[
                 * {"windowLocation":"Front", "state":"Off"},
                 * {"windowLocation":"Rear", "state":"Off"}
                 * ],
                 * "seatHeatingSupport":{
                 * "frontLeftAvailable":true,"frontRightAvailable":true,
                 * "rearLeftAvailable":false,"rearRightAvailable":false
                 * }
                 * }
                 */
                public class SEHeatingSupport {
                    public Boolean frontLeftAvailable, frontRightAvailable, rearLeftAvailable, rearRightAvailable;
                }

                public class SEHeatingStatus {
                    public String windowLocation;
                    public String state;
                }

                public Integer remainingTimeToReachTargetTemperatureInSeconds;
                public String state;
                public String trigger;
                public ArrayList<SEHeatingStatus> windowsHeatingStatuses;
            }

            public SEChargerStatus charger;
            public SEClimaterStatus climatisation;
        }

        public SEVehicleStatus status;
        public SEVehicleSettings settings;

        public SEVehicleStatusData() {
            status = new SEVehicleStatus();
            settings = new SEVehicleSettings();
        }
    }
}
