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
package org.openhab.binding.bmwconnecteddrive.internal.utils;

import static org.openhab.binding.bmwconnecteddrive.internal.utils.Constants.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.CBSMessage;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatus;

/**
 * The {@link VehicleStatusUtils} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleStatusUtils {

    public static String getNextServiceDate(VehicleStatus vStatus) {
        if (vStatus.cbsData == null) {
            return Constants.NULL_DATE;
        }
        if (vStatus.cbsData.isEmpty()) {
            return Constants.NULL_DATE;
        } else {
            LocalDateTime farFuture = LocalDateTime.now().plusYears(100);
            LocalDateTime serviceDate = farFuture;
            for (int i = 0; i < vStatus.cbsData.size(); i++) {
                CBSMessage entry = vStatus.cbsData.get(i);
                if (entry.cbsDueDate != null) {
                    LocalDateTime d = LocalDateTime.parse(entry.cbsDueDate + Constants.UTC_APPENDIX);
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

    public static int getNextServiceMileage(VehicleStatus vStatus) {
        if (vStatus.cbsData == null) {
            return -1;
        }
        if (vStatus.cbsData.isEmpty()) {
            return -1;
        } else {
            int serviceMileage = Integer.MAX_VALUE;
            for (int i = 0; i < vStatus.cbsData.size(); i++) {
                CBSMessage entry = vStatus.cbsData.get(i);
                if (entry.cbsRemainingMileage != -1) {
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

    public static String checkControlActive(VehicleStatus vStatus) {
        if (vStatus.checkControlMessages == null) {
            return UNDEF;
        }
        if (vStatus.checkControlMessages.isEmpty()) {
            return NOT_ACTIVE;
        } else {
            return ACTIVE;
        }
    }

    public static String getUpdateTime(VehicleStatus vStatus) {
        if (vStatus.internalDataTimeUTC != null) {
            return vStatus.internalDataTimeUTC;
        } else if (vStatus.updateTime != null) {
            return vStatus.updateTime;
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
        String overallState = Constants.UNDEF;
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
                            if (overallState.equalsIgnoreCase(UNDEF)) {
                                overallState = CLOSED;
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
            }
        }
        return Converter.toTitleCase(overallState);
    }
}
