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

import static org.openhab.binding.bmwconnecteddrive.internal.utils.Constants.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VehicleStatus} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class VehicleStatus {
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
    public CBSMessage getNextService(boolean imperial) {
        CBSMessage cbs = new CBSMessage();
        if (cbsData == null) {
            return cbs;
        }
        if (cbsData.isEmpty()) {
            return cbs;
        } else {
            int serviceMileage = Integer.MAX_VALUE;
            LocalDateTime serviceDate = LocalDateTime.now().plusYears(100);

            for (int i = 0; i < cbsData.size(); i++) {
                CBSMessage entry = cbsData.get(i);
                if (entry.cbsRemainingMileage != 0 && entry.cbsDueDate != null) {
                    LocalDateTime d = LocalDateTime.parse(entry.cbsDueDate + Constants.UTC_APPENDIX);
                    // LocalDate d = LocalDate.parse(entry.cbsDueDate + APPENDIX_DAY,
                    // Converter.SERVICE_DATE_INPUT_PATTERN);
                    if ((entry.cbsRemainingMileage < serviceMileage) || (d.isBefore(serviceDate))) {
                        serviceDate = d;
                        serviceMileage = entry.cbsRemainingMileage;
                        cbs = entry;
                    }
                } else if (entry.cbsRemainingMileage != 0) {
                    if (entry.cbsRemainingMileage < serviceMileage) {
                        serviceMileage = entry.cbsRemainingMileage;
                        cbs = entry;
                    }
                } else if (entry.cbsDueDate != null) {
                    LocalDateTime d = LocalDateTime.parse(entry.cbsDueDate + Constants.UTC_APPENDIX);
                    // LocalDate d = LocalDate.parse(entry.cbsDueDate + APPENDIX_DAY,
                    // Converter.SERVICE_DATE_INPUT_PATTERN);
                    if (d.isBefore(serviceDate)) {
                        serviceDate = d;
                        cbs = entry;
                    }
                }
            }
        }
        return cbs;
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

    public String getUpdateTime() {
        if (internalDataTimeUTC != null) {
            return internalDataTimeUTC;
        } else if (updateTime != null) {
            return updateTime;
        } else {
            return Constants.NULL_DATE;
        }
    }

    /**
     * Check for certain Windows or Doors DTO object the "Closed" Status
     * INVALID values will be ignored
     * 
     * @param dto
     * @return Closed if all "Closed", "Open" otherwise
     */
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
