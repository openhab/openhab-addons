/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.utils;

import java.time.ZonedDateTime;
import java.util.List;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.MyBMWConstants.VehicleType;
import org.openhab.binding.mybmw.internal.dto.properties.CBS;
import org.openhab.binding.mybmw.internal.dto.status.FuelIndicator;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleStatusUtils} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleStatusUtils {
    public static final Logger LOGGER = LoggerFactory.getLogger(VehicleStatusUtils.class);

    public static State getNextServiceDate(List<CBS> cbsMessageList) {
        ZonedDateTime farFuture = ZonedDateTime.now().plusYears(100);
        ZonedDateTime serviceDate = farFuture;
        for (CBS service : cbsMessageList) {
            if (service.dateTime != null) {
                ZonedDateTime d = ZonedDateTime.parse(service.dateTime);
                if (d.isBefore(serviceDate)) {
                    serviceDate = d;
                } // else skip
            }
        }
        if (serviceDate.equals(farFuture)) {
            return UnDefType.UNDEF;
        } else {
            return DateTimeType.valueOf(serviceDate.format(Converter.DATE_INPUT_PATTERN));
        }
    }

    public static State getNextServiceMileage(List<CBS> cbsMessageList) {
        boolean imperial = false;
        int serviceMileage = Integer.MAX_VALUE;
        for (CBS service : cbsMessageList) {
            if (service.distance != null) {
                if (service.distance.value < serviceMileage) {
                    serviceMileage = service.distance.value;
                    imperial = !Constants.KILOMETERS_JSON.equals(service.distance.units);
                }
            }
        }
        if (serviceMileage != Integer.MAX_VALUE) {
            if (imperial) {
                return QuantityType.valueOf(serviceMileage, ImperialUnits.MILE);
            } else {
                return QuantityType.valueOf(serviceMileage, Constants.KILOMETRE_UNIT);
            }
        } else {
            return UnDefType.UNDEF;
        }
    }

    /**
     * calculates the mapping of thing type
     *
     * @param driveTrain
     * @param model
     * @return
     */
    public static VehicleType vehicleType(String driveTrain, String model) {
        if (Constants.BEV.equals(driveTrain)) {
            if (model.endsWith(Constants.REX_EXTENSION)) {
                return VehicleType.ELECTRIC_REX;
            } else {
                return VehicleType.ELECTRIC;
            }
        } else if (Constants.PHEV.equals(driveTrain)) {
            return VehicleType.PLUGIN_HYBRID;
        } else if (Constants.CONV.equals(driveTrain) || Constants.HYBRID.equals(driveTrain)) {
            return VehicleType.CONVENTIONAL;
        }
        LOGGER.warn("Unknown Vehicle Type: {} | {}", model, driveTrain);
        return VehicleType.UNKNOWN;
    }

    public static @Nullable Unit<Length> getLengthUnit(List<FuelIndicator> indicators) {
        Unit<Length> ret = null;
        for (FuelIndicator fuelIndicator : indicators) {
            String unitAbbrev = fuelIndicator.rangeUnits;
            switch (unitAbbrev) {
                case Constants.KM_JSON:
                    if (ret != null) {
                        if (!ret.equals(Constants.KILOMETRE_UNIT)) {
                            LOGGER.debug("Ambigious Unit declarations. Found {} before {}", ret, Constants.KM_JSON);
                        } // else - fine!
                    } else {
                        ret = Constants.KILOMETRE_UNIT;
                    }
                    break;
                case Constants.MI_JSON:
                    if (ret != null) {
                        if (!ret.equals(ImperialUnits.MILE)) {
                            LOGGER.debug("Ambigious Unit declarations. Found {} before {}", ret, Constants.MI_JSON);
                        } // else - fine!
                    } else {
                        ret = ImperialUnits.MILE;
                    }
                    break;
                default:
                    LOGGER.debug("Cannot evaluate Unit for {}", unitAbbrev);
                    break;
            }
        }
        return ret;
    }

    /**
     * The range values delivered by BMW are quite ambiguous!
     * - status fuel indicators are missing a unique identifier
     * - properties ranges delivering wrong values for hybrid and fuel range
     * - properties ranges are not reflecting mi / km - every time km
     *
     * So getRange will try
     * 1) fuel indicator
     * 2) ranges from properties, except combined range
     * 3) take a guess from fuel indicators
     *
     * @param unitJson
     * @param vehicle
     * @return
     */
    public static int getRange(String unitJson, Vehicle vehicle) {
        if (vehicle.status.fuelIndicators.size() == 1) {
            return Converter.stringToInt(vehicle.status.fuelIndicators.get(0).rangeValue);
        } else {
            return guessRange(unitJson, vehicle);
        }
    }

    /**
     * Guesses the range from 3 fuelindicators
     * - electric range calculation is correct
     * - for the 2 other values:
     * -- smaller one is assigned to fuel range
     * -- bigger one is assigned to hybrid range
     *
     * @see org.openhab.binding.mybmw.internal.dto.VehicleStatusTest testGuessRange
     *
     * @param unitJson
     * @param vehicle
     * @return
     */
    public static int guessRange(String unitJson, Vehicle vehicle) {
        int electricGuess = Constants.INT_UNDEF;
        int fuelGuess = Constants.INT_UNDEF;
        int hybridGuess = Constants.INT_UNDEF;
        for (FuelIndicator fuelIndicator : vehicle.status.fuelIndicators) {
            // electric range - this fits 100%
            if (Constants.UNIT_PRECENT_JSON.equals(fuelIndicator.levelUnits)
                    && fuelIndicator.chargingStatusType != null) {
                // found electric
                electricGuess = Converter.stringToInt(fuelIndicator.rangeValue);
            } else {
                if (fuelGuess == Constants.INT_UNDEF) {
                    // fuel not set? then assume it's fuel
                    fuelGuess = Converter.stringToInt(fuelIndicator.rangeValue);
                } else {
                    // fuel already guessed - take smaller value for fuel, bigger for hybrid
                    int newGuess = Converter.stringToInt(fuelIndicator.rangeValue);
                    hybridGuess = Math.max(fuelGuess, newGuess);
                    fuelGuess = Math.min(fuelGuess, newGuess);
                }
            }
        }
        switch (unitJson) {
            case Constants.UNIT_PRECENT_JSON:
                return electricGuess;
            case Constants.UNIT_LITER_JSON:
                return fuelGuess;
            case Constants.PHEV:
                return hybridGuess;
            default:
                return Constants.INT_UNDEF;
        }
    }

    public static String getChargStatus(Vehicle vehicle) {
        FuelIndicator fi = getElectricFuelIndicator(vehicle);
        if (fi.chargingStatusType != null) {
            if (fi.chargingStatusType.equals(Constants.DEFAULT)) {
                return Constants.NOT_CHARGING_STATE;
            } else {
                return fi.chargingStatusType;
            }
        }
        return Constants.UNDEF;
    }

    public static String getChargeInfo(Vehicle vehicle) {
        FuelIndicator fi = getElectricFuelIndicator(vehicle);
        if (fi.chargingStatusType != null && fi.infoLabel != null) {
            if (fi.chargingStatusType.equals(Constants.CHARGING_STATE)
                    || fi.chargingStatusType.equals(Constants.PLUGGED_STATE)) {
                return fi.infoLabel;
            }
        }
        return Constants.HYPHEN;
    }

    private static FuelIndicator getElectricFuelIndicator(Vehicle vehicle) {
        for (FuelIndicator fuelIndicator : vehicle.status.fuelIndicators) {
            if (Constants.UNIT_PRECENT_JSON.equals(fuelIndicator.levelUnits)
                    && fuelIndicator.chargingStatusType != null) {
                return fuelIndicator;
            }
        }
        return new FuelIndicator();
    }
}
