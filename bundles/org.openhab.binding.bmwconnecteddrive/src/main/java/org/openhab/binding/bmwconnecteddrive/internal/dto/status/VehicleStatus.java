/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto.status;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VehicleStatus} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class VehicleStatus {
    public static final String OK = "Ok";
    public static final String OPEN = "OPEN";
    public static final String INVALID = "INVALID";
    public static final String CLOSED = "CLOSED";
    public static final String UNKNOWN = "UNKOWN";
    public static final String NO_SERVICE_REQUEST = "No Service Requests";
    public static final String APPENDIX_DAY = "-01"; // needed to complete Service Date
    public static final String MILES_SHORT = "mi";
    public static final String KM_SHORT = "km";

    public int mileage;// ": 17273,
    public float remainingFuel;// ": 4,
    public float remainingRangeElectric;// ": 148,
    public float remainingRangeElectricMls;// ": 91,
    public float remainingRangeFuel;// ": 70,"
    public float remainingRangeFuelMls;// ":43,"
    public float maxRangeElectric;// ":216,"
    public float maxRangeElectricMls;// ":134,"
    public float maxFuel;// ":8.5,
    public float chargingLevelHv;// ":71,
    public String vin;// : "WBY1Z81040V905639",
    public String updateReason;// ": "VEHICLE_SHUTDOWN_SECURED",
    public String updateTime;// ": "2020-08-24 T15:55:32+0000",
    public String doorDriverFront;// ": "CLOSED",
    public String doorDriverRear;// ": "CLOSED",
    public String doorPassengerFront;// ": "CLOSED",
    public String doorPassengerRear;// ": "CLOSED",
    public String windowDriverFront;// ": "CLOSED",
    public String windowDriverRear;// ": "CLOSED",
    public String windowPassengerFront;// ": "CLOSED",
    public String windowPassengerRear;// ": "CLOSED",
    public String sunroof;// ": "CLOSED",
    public String trunk;// ": "CLOSED",
    public String rearWindow;// ": "INVALID",
    public String hood;// ": "CLOSED",
    public String doorLockState;// ": "SECURED",
    public String parkingLight;// ": "OFF",
    public String positionLight;// ": "ON",
    public String connectionStatus;// ": "DISCONNECTED",
    public String chargingStatus;// ": "INVALID","
    public String lastChargingEndReason;// ": "CHARGING_GOAL_REACHED",
    public String lastChargingEndResult;// ": "SUCCESS","
    public Position position;
    public String internalDataTimeUTC;// ": "2020-08-24 T15:55:32",
    public boolean singleImmediateCharging;// ":false,
    public String chargingConnectionType;// ": "CONDUCTIVE",
    public String chargingInductivePositioning;// ": "NOT_POSITIONED",
    public String vehicleCountry;// ": "DE","+"
    @SerializedName("DCS_CCH_Activation")
    public String dcsCchActivation;// ": "NA",
    @SerializedName("DCS_CCH_Ongoing")
    public boolean dcsCchOngoing;// ":false
    public List<CCMMessage> checkControlMessages;// ":[],
    public List<CBSMessage> cbsData;

    /**
     * Get Next Service for Date and / or Mileage
     *
     * @param imperial
     * @return
     */
    public String getNextService(boolean imperial) {
        if (cbsData == null) {
            return Converter.toTitleCase(UNKNOWN);
        }
        if (cbsData.isEmpty()) {
            return NO_SERVICE_REQUEST;
        } else {
            int serviceMileage = Integer.MAX_VALUE;
            LocalDate serviceDate = LocalDate.now().plusYears(100);
            String service = null;

            for (int i = 0; i < cbsData.size(); i++) {
                CBSMessage entry = cbsData.get(i);
                String serviceDescription = Converter.toTitleCase(entry.cbsType);
                if (entry.cbsRemainingMileage != 0 && entry.cbsDueDate != null) {
                    LocalDate d = LocalDate.parse(entry.cbsDueDate + APPENDIX_DAY,
                            Converter.SERVICE_DATE_INPUT_PATTERN);
                    if ((entry.cbsRemainingMileage < serviceMileage) || (d.isBefore(serviceDate))) {
                        serviceDate = d;
                        serviceMileage = entry.cbsRemainingMileage;
                        service = new StringBuffer(serviceDate.format(Converter.SERVICE_DATE_OUTPUT_PATTERN))
                                .append(" or in ").append(serviceMileage).append(" ").append(getUnit(imperial))
                                .append(" - ").append(serviceDescription).toString();
                    }
                } else if (entry.cbsRemainingMileage != 0) {
                    if (entry.cbsRemainingMileage < serviceMileage) {
                        serviceMileage = entry.cbsRemainingMileage;
                        service = new StringBuffer("In ").append(serviceMileage).append(" ").append(getUnit(imperial))
                                .append(" - ").append(serviceDescription).toString();
                    }
                } else if (entry.cbsDueDate != null) {
                    LocalDate d = LocalDate.parse(entry.cbsDueDate + APPENDIX_DAY,
                            Converter.SERVICE_DATE_INPUT_PATTERN);
                    if (d.isBefore(serviceDate)) {
                        serviceDate = d;
                        service = new StringBuffer(serviceDate.format(Converter.SERVICE_DATE_OUTPUT_PATTERN))
                                .append(" - ").append(serviceDescription).toString();
                    }
                }
            }
            if (service != null) {
                return service;
            } else {
                return "Unknown";
            }
        }
    }

    private Object getUnit(boolean imperial) {
        if (imperial) {
            return MILES_SHORT;
        } else {
            return KM_SHORT;
        }
    }

    public String getCheckControl() {
        if (checkControlMessages == null) {
            return Converter.toTitleCase(UNKNOWN);
        }
        if (checkControlMessages.isEmpty()) {
            return OK;
        } else {
            return Converter.toTitleCase(checkControlMessages.get(0).ccmDescriptionShort);
        }
    }

    public static String checkClosed(Object dto) {
        boolean validDoorFound = false;
        for (Field field : dto.getClass().getDeclaredFields()) {
            try {
                if (field.get(dto) != null) {
                    if (field.get(dto).equals(OPEN)) {
                        // report the first door which is still open
                        return Converter
                                .toTitleCase(new StringBuffer(field.getName()).append(" ").append(OPEN).toString());
                        // Ignore INVALID fields == Door not present } else if (field.get(dto).equals(INVALID)) {
                    } else if (field.get(dto).equals(CLOSED)) {
                        validDoorFound = true;
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                return Converter.toTitleCase(UNKNOWN);
            }
        }
        if (validDoorFound) {
            return Converter.toTitleCase(CLOSED);
        } else {
            return Converter.toTitleCase(UNKNOWN);
        }
    }

}
