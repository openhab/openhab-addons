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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.MyBMWConstants.VehicleType;
import org.openhab.binding.mybmw.internal.dto.properties.CBS;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link VehicleStatusUtils} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleStatusUtils {

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
}
