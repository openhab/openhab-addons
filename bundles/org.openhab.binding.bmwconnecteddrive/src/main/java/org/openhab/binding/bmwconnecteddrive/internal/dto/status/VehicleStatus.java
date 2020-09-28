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
import java.util.ArrayList;
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
    public double remainingFuel;// ": 4,
    public double remainingRangeElectric;// ": 148,
    public double remainingRangeElectricMls;// ": 91,
    public double remainingRangeFuel;// ": 70,"
    public double remainingRangeFuelMls;// ":43,"
    public double maxRangeElectric;// ":216,"
    public double maxRangeElectricMls;// ":134,"
    public double maxFuel;// ":8.5,
    public double chargingLevelHv;// ":71,
    public String vin;// : "ANONYMOUS",
    public String updateReason;// ": "VEHICLE_SHUTDOWN_SECURED",
    public String updateTime;// ": "2020-08-24 T15:55:32+0000",
    public String doorDriverFront = Constants.UNKNOWN;// ": "CLOSED",
    public String doorDriverRear = Constants.UNKNOWN;// ": "CLOSED",
    public String doorPassengerFront = Constants.UNKNOWN;// ": "CLOSED",
    public String doorPassengerRear = Constants.UNKNOWN;// ": "CLOSED",
    public String windowDriverFront = Constants.UNKNOWN;// ": "CLOSED",
    public String windowDriverRear = Constants.UNKNOWN;// ": "CLOSED",
    public String windowPassengerFront = Constants.UNKNOWN;// ": "CLOSED",
    public String windowPassengerRear = Constants.UNKNOWN;// ": "CLOSED",
    public String sunroof = Constants.UNKNOWN;// ": "CLOSED",
    public String trunk = Constants.UNKNOWN;// ": "CLOSED",
    public String rearWindow = Constants.UNKNOWN;// ": "INVALID",
    public String hood = Constants.UNKNOWN;// ": "CLOSED",
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
    public List<CCMMessage> checkControlMessages = new ArrayList<CCMMessage>();// ":[],
    public List<CBSMessage> cbsData = new ArrayList<CBSMessage>();

    /**
     * Get Next Service for Date and / or Mileage
     *
     * @param imperial
     * @return
     */
    public CBSMessage getNextService() {
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

    public String getNextServiceDate() {
        if (cbsData == null) {
            return Constants.NULL_DATE;
        }
        if (cbsData.isEmpty()) {
            return Constants.NULL_DATE;
        } else {
            LocalDateTime farFuture = LocalDateTime.now().plusYears(100);
            LocalDateTime serviceDate = farFuture;
            for (int i = 0; i < cbsData.size(); i++) {
                CBSMessage entry = cbsData.get(i);
                if (entry.cbsDueDate != null) {
                    LocalDateTime d = LocalDateTime.parse(entry.cbsDueDate + Constants.UTC_APPENDIX);
                    // LocalDate d = LocalDate.parse(entry.cbsDueDate + APPENDIX_DAY,
                    // Converter.SERVICE_DATE_INPUT_PATTERN);
                    if (d.isBefore(serviceDate)) {
                        serviceDate = d;
                    }
                }
            }
            if (serviceDate.equals(farFuture)) {
                return Constants.NULL_DATE;
            } else {
                return serviceDate.format(Converter.DATE_INPUT_PATTERN);
            }
        }
    }

    public int getNextServiceMileage() {
        if (cbsData == null) {
            return -1;
        }
        if (cbsData.isEmpty()) {
            return -1;
        } else {
            int serviceMileage = Integer.MAX_VALUE;
            for (int i = 0; i < cbsData.size(); i++) {
                CBSMessage entry = cbsData.get(i);
                if (entry.cbsRemainingMileage != 0) {
                    if (entry.cbsRemainingMileage < serviceMileage) {
                        serviceMileage = entry.cbsRemainingMileage;
                    }
                }
            }
            if (serviceMileage != Integer.MAX_VALUE) {
                return serviceMileage;
            } else {
                return -1;
            }
        }
    }

    public String checkControlActive() {
        if (checkControlMessages == null) {
            return UNKNOWN;
        }
        if (checkControlMessages.isEmpty()) {
            return NOT_ACTIVE;
        } else {
            return ACTIVE;
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
        String overallState = Constants.UNKNOWN;
        for (Field field : dto.getClass().getDeclaredFields()) {
            try {
                Object d = field.get(dto);
                if (d != null) {
                    String state = d.toString();
                    // skip invalid entries - they don't apply to this Vehicle
                    if (!state.equalsIgnoreCase(INVALID)) {
                        if (state.equalsIgnoreCase(OPEN)) {
                            overallState = OPEN;
                            // stop searching for more open items - overall Doors / Windows are OPEN
                            break;
                        } else if (state.equalsIgnoreCase(INTERMEDIATE)) {
                            if (!overallState.equalsIgnoreCase(OPEN)) {
                                overallState = INTERMEDIATE;
                                // continue searching - maybe another Door / Window is OPEN
                            }
                        } else if (state.equalsIgnoreCase(CLOSED)) {
                            // at least one valid object needs to be found in order to reply "CLOSED"
                            if (overallState.equalsIgnoreCase(UNKNOWN)) {
                                overallState = CLOSED;
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                return UNKNOWN;
            }
        }
        return overallState;
    }
}
