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
            DateTimeType dt = DateTimeType.valueOf(serviceDate.format(Converter.DATE_INPUT_PATTERN));
            return dt;
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
        } else if (Constants.CONV.equals(driveTrain)) {
            return VehicleType.CONVENTIONAL;
        }
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
                            LOGGER.info("Ambigious Unit declarations. Found {} before {}", ret, Constants.KM_JSON);
                        } // else - fine!
                    } else {
                        ret = Constants.KILOMETRE_UNIT;
                    }
                    break;
                case Constants.MI_JSON:
                    if (ret != null) {
                        if (!ret.equals(ImperialUnits.MILE)) {
                            LOGGER.info("Ambigious Unit declarations. Found {} before {}", ret, Constants.MI_JSON);
                        } // else - fine!
                    } else {
                        ret = ImperialUnits.MILE;
                    }
                    break;
                default:
                    LOGGER.info("Cannot evaluate Unit for {}", unitAbbrev);
                    break;
            }
        }
        return ret;
    }

    /**
     * The range values delivered by BMW are quite ambiguous!
     * - status fuel indicators are missing a unique identifier
     * - properties ranges delivering wrong values for hybrid range
     * - properties ranges are not reflecting mi / km - every time km
     *
     * So getRange will try
     * 1) fuel indicator
     * 2) ranges from properties, except combined range
     * 3) take a guess from fuel indicators
     *
     * @param unitJson
     * @param indicators
     * @return
     */
    public static int getRange(String unitJson, Vehicle vehicle) {
        // First: fuel indicators based on unit
        int range = getFuelIndicatorRange(unitJson, vehicle.status.fuelIndicators);

        // if not successful try ranges in properties section
        if (range == -1) {
            if (unitJson.equals(Constants.UNIT_LITER_JSON)) {
                if (vehicle.properties.combustionRange != null) {
                    range = vehicle.properties.combustionRange.distance.value;
                }
            } else if (unitJson.equals(Constants.UNIT_PRECENT_JSON)) {
                if (vehicle.properties.electricRange != null) {
                    range = vehicle.properties.electricRange.distance.value;
                }
            }
        }

        // still not valid - take a guess
        if (range == -1) {
            range = VehicleStatusUtils.guessRange(unitJson, vehicle);
        }
        return range;
    }

    public static int getFuelIndicatorRange(String unitJson, List<FuelIndicator> indicators) {
        String rangeString = Constants.EMPTY;
        for (FuelIndicator fuelIndicator : indicators) {
            if (fuelIndicator.levelUnits == null) {
                // combined range doesn't contain a valid unit
                if (unitJson.equals(Constants.PHEV)) {
                    rangeString = fuelIndicator.rangeValue;
                    break;
                }
            } else if (fuelIndicator.levelUnits.equals(unitJson)) {
                rangeString = fuelIndicator.rangeValue;
            }
        }
        int range = Constants.INT_UNDEF;
        try {
            range = Integer.parseInt(rangeString);

        } catch (Exception e) {
            LOGGER.info("Unable to convert range {} into int value", rangeString);
        }
        return range;
    }

    public static int guessRange(String unitJson, Vehicle vehicle) {
        int electricGuess = Constants.INT_UNDEF;
        int fuelGuess = Constants.INT_UNDEF;
        int hybridGuess = Constants.INT_UNDEF;
        int undefinedGuess = Constants.INT_UNDEF;
        for (FuelIndicator fuelIndicator : vehicle.status.fuelIndicators) {
            if (fuelIndicator.levelUnits == null) {
                if (undefinedGuess == Constants.INT_UNDEF) {
                    // found value - store!
                    undefinedGuess = Integer.parseInt(fuelIndicator.rangeValue);
                } else {
                    // there's already a value! Compare and guess!
                    int newGuess = Integer.parseInt(fuelIndicator.rangeValue);
                    if (newGuess > undefinedGuess) {
                        hybridGuess = newGuess;
                        fuelGuess = undefinedGuess;
                    } else {
                        hybridGuess = undefinedGuess;
                        fuelGuess = newGuess;
                    }
                }
            } else if (fuelIndicator.levelUnits.equals(Constants.UNIT_PRECENT_JSON)) {
                // found electric
                electricGuess = Integer.parseInt(fuelIndicator.rangeValue);
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
}
