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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwconnecteddrive.internal.dto.compat.VehicleAttributes;
import org.openhab.binding.bmwconnecteddrive.internal.dto.compat.VehicleAttributesContainer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.compat.VehicleMessages;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.CBSMessage;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.CCMMessage;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Position;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatusContainer;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link Converter} Conversion Helpers
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Converter {
    public static final Logger LOGGER = LoggerFactory.getLogger(Converter.class);

    public static final DateTimeFormatter SERVICE_DATE_INPUT_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter SERVICE_DATE_OUTPUT_PATTERN = DateTimeFormatter.ofPattern("MMM yyyy");

    public static final String LOCAL_DATE_INPUT_PATTERN_STRING = "dd.MM.yyyy HH:mm";
    public static final DateTimeFormatter LOCAL_DATE_INPUT_PATTERN = DateTimeFormatter
            .ofPattern(LOCAL_DATE_INPUT_PATTERN_STRING);

    public static final String DATE_INPUT_PATTERN_STRING = "yyyy-MM-dd'T'HH:mm:ss";
    public static final DateTimeFormatter DATE_INPUT_PATTERN = DateTimeFormatter.ofPattern(DATE_INPUT_PATTERN_STRING);

    public static final String DATE_INPUT_ZONE_PATTERN_STRING = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final DateTimeFormatter DATE_INPUT_ZONE_PATTERN = DateTimeFormatter
            .ofPattern(DATE_INPUT_ZONE_PATTERN_STRING);

    public static final DateTimeFormatter DATE_OUTPUT_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final Gson GSON = new Gson();
    private static final double SCALE = 10;
    public static final double MILES_TO_KM_RATIO = 1.60934;
    private static final String SPLIT_HYPHEN = "-";
    private static final String SPLIT_BRACKET = "\\(";

    public static Optional<TimeZoneProvider> timeZoneProvider = Optional.empty();

    public static double round(double value) {
        return Math.round(value * SCALE) / SCALE;
    }

    public static String getLocalDateTimeWithoutOffest(@Nullable String input) {
        if (input == null) {
            return Constants.NULL_DATE;
        }
        LocalDateTime ldt;
        if (input.contains(Constants.PLUS)) {
            ldt = LocalDateTime.parse(input, Converter.DATE_INPUT_ZONE_PATTERN);
        } else {
            ldt = LocalDateTime.parse(input, Converter.DATE_INPUT_PATTERN);
        }
        return ldt.format(Converter.DATE_INPUT_PATTERN);
    }

    public static String getLocalDateTime(@Nullable String input) {
        if (input == null) {
            return Constants.NULL_DATE;
        }

        LocalDateTime ldt;
        if (input.contains(Constants.PLUS)) {
            ldt = LocalDateTime.parse(input, Converter.DATE_INPUT_ZONE_PATTERN);
        } else {
            try {
                ldt = LocalDateTime.parse(input, Converter.DATE_INPUT_PATTERN);
            } catch (DateTimeParseException dtpe) {
                ldt = LocalDateTime.parse(input, Converter.LOCAL_DATE_INPUT_PATTERN);
            }
        }
        ZonedDateTime zdtUTC = ldt.atZone(ZoneId.of("UTC"));
        ZonedDateTime zdtLZ;
        zdtLZ = zdtUTC.withZoneSameInstant(ZoneId.systemDefault());
        if (timeZoneProvider.isPresent()) {
            zdtLZ = zdtUTC.withZoneSameInstant(timeZoneProvider.get().getTimeZone());
        } else {
            zdtLZ = zdtUTC.withZoneSameInstant(ZoneId.systemDefault());
        }
        return zdtLZ.format(Converter.DATE_INPUT_PATTERN);
    }

    public static void setTimeZoneProvider(TimeZoneProvider tzp) {
        timeZoneProvider = Optional.of(tzp);
    }

    public static String toTitleCase(@Nullable String input) {
        if (input == null) {
            return toTitleCase(Constants.UNDEF);
        } else {
            String lower = input.replaceAll(Constants.UNDERLINE, Constants.SPACE).toLowerCase();
            String converted = toTitleCase(lower, Constants.SPACE);
            converted = toTitleCase(converted, SPLIT_HYPHEN);
            converted = toTitleCase(converted, SPLIT_BRACKET);
            return converted;
        }
    }

    private static String toTitleCase(String input, String splitter) {
        String[] arr = input.split(splitter);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(splitter.replaceAll("\\\\", Constants.EMPTY));
            }
            sb.append(Character.toUpperCase(arr[i].charAt(0))).append(arr[i].substring(1));
        }
        return sb.toString().trim();
    }

    public static String capitalizeFirst(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static Gson getGson() {
        return GSON;
    }

    /**
     * Measure distance between 2 coordinates
     *
     * @param sourceLatitude
     * @param sourceLongitude
     * @param destinationLatitude
     * @param destinationLongitude
     * @return distance
     */
    public static double measureDistance(double sourceLatitude, double sourceLongitude, double destinationLatitude,
            double destinationLongitude) {
        double earthRadius = 6378.137; // Radius of earth in KM
        double dLat = destinationLatitude * Math.PI / 180 - sourceLatitude * Math.PI / 180;
        double dLon = destinationLongitude * Math.PI / 180 - sourceLongitude * Math.PI / 180;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(sourceLatitude * Math.PI / 180)
                * Math.cos(destinationLatitude * Math.PI / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    /**
     * Easy function but there's some measures behind:
     * Guessing the range of the Vehicle on Map. If you can drive x kilometers with your Vehicle it's not feasible to
     * project this x km Radius on Map. The roads to be taken are causing some overhead because they are not a straight
     * line from Location A to B.
     * I've taken some measurements to calculate the overhead factor based on Google Maps
     * Berlin - Dresden: Road Distance: 193 air-line Distance 167 = Factor 87%
     * Kassel - Frankfurt: Road Distance: 199 air-line Distance 143 = Factor 72%
     * After measuring more distances you'll find out that the outcome is between 70% and 90%. So
     *
     * This depends also on the roads of a concrete route but this is only a guess without any Route Navigation behind
     *
     * In future it's foreseen to replace this with BMW RangeMap Service which isn't running at the moment.
     *
     * @param range
     * @return mapping from air-line distance to "real road" distance
     */
    public static double guessRangeRadius(double range) {
        return range * 0.8;
    }

    public static State getMiles(QuantityType<Length> qtLength) {
        if (qtLength.intValue() == -1) {
            return UnDefType.UNDEF;
        }
        QuantityType<Length> qt = qtLength.toUnit(ImperialUnits.MILE);
        if (qt != null) {
            return qt;
        } else {
            LOGGER.debug("Cannot convert {} to miles", qt);
            return UnDefType.UNDEF;
        }
    }

    public static int getIndex(String fullString) {
        int index = -1;
        try {
            index = Integer.parseInt(fullString);
        } catch (NumberFormatException nfe) {
        }
        return index;
    }

    public static String transformLegacyStatus(@Nullable VehicleAttributesContainer vac) {
        if (vac != null) {
            if (vac.attributesMap != null && vac.vehicleMessages != null) {
                VehicleAttributes attributesMap = vac.attributesMap;
                VehicleMessages vehicleMessages = vac.vehicleMessages;
                // create target objects
                VehicleStatusContainer vsc = new VehicleStatusContainer();
                VehicleStatus vs = new VehicleStatus();
                vsc.vehicleStatus = vs;

                vs.mileage = attributesMap.mileage;
                vs.doorLockState = attributesMap.doorLockState;

                vs.doorDriverFront = attributesMap.doorDriverFront;
                vs.doorDriverRear = attributesMap.doorDriverRear;
                vs.doorPassengerFront = attributesMap.doorPassengerFront;
                vs.doorPassengerRear = attributesMap.doorPassengerRear;
                vs.hood = attributesMap.hoodState;
                vs.trunk = attributesMap.trunkState;

                vs.windowDriverFront = attributesMap.winDriverFront;
                vs.windowDriverRear = attributesMap.winDriverRear;
                vs.windowPassengerFront = attributesMap.winPassengerFront;
                vs.windowPassengerRear = attributesMap.winPassengerRear;
                vs.sunroof = attributesMap.sunroofState;

                vs.remainingFuel = attributesMap.remainingFuel;
                vs.remainingRangeElectric = attributesMap.beRemainingRangeElectricKm;
                vs.remainingRangeElectricMls = attributesMap.beRemainingRangeElectricMile;
                vs.remainingRangeFuel = attributesMap.beRemainingRangeFuelKm;
                vs.remainingRangeFuelMls = attributesMap.beRemainingRangeFuelMile;
                vs.remainingFuel = attributesMap.remainingFuel;
                vs.chargingLevelHv = attributesMap.chargingLevelHv;
                vs.maxRangeElectric = attributesMap.beMaxRangeElectric;
                vs.maxRangeElectricMls = attributesMap.beMaxRangeElectricMile;
                vs.chargingStatus = attributesMap.chargingHVStatus;
                vs.connectionStatus = attributesMap.connectorStatus;
                vs.lastChargingEndReason = attributesMap.lastChargingEndReason;

                vs.updateTime = attributesMap.updateTimeConverted;
                vs.updateReason = attributesMap.lastUpdateReason;

                Position p = new Position();
                p.lat = attributesMap.gpsLat;
                p.lon = attributesMap.gpsLon;
                p.heading = attributesMap.heading;
                vs.position = p;

                final List<CCMMessage> ccml = new ArrayList<CCMMessage>();
                if (vehicleMessages != null) {
                    if (vehicleMessages.ccmMessages != null) {
                        vehicleMessages.ccmMessages.forEach(entry -> {
                            CCMMessage ccmM = new CCMMessage();
                            ccmM.ccmDescriptionShort = entry.text;
                            ccmM.ccmDescriptionLong = Constants.HYPHEN;
                            ccmM.ccmMileage = entry.unitOfLengthRemaining;
                            ccml.add(ccmM);
                        });
                    }
                }
                vs.checkControlMessages = ccml;

                final List<CBSMessage> cbsl = new ArrayList<CBSMessage>();
                if (vehicleMessages != null) {
                    if (vehicleMessages.cbsMessages != null) {
                        vehicleMessages.cbsMessages.forEach(entry -> {
                            CBSMessage cbsm = new CBSMessage();
                            cbsm.cbsType = entry.text;
                            cbsm.cbsDescription = entry.description;
                            cbsm.cbsDueDate = entry.date;
                            cbsm.cbsRemainingMileage = entry.unitOfLengthRemaining;
                            cbsl.add(cbsm);
                        });
                    }
                }
                vs.cbsData = cbsl;
                return Converter.getGson().toJson(vsc);
            }
        }
        return Constants.EMPTY_JSON;
    }
}
