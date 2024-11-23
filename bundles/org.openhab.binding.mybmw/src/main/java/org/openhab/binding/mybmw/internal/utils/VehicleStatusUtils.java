/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.MyBMWConstants.VehicleType;
import org.openhab.binding.mybmw.internal.dto.vehicle.RequiredService;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleStatusUtils} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactor for v2 and extract some methods to other classes
 */
@NonNullByDefault
public class VehicleStatusUtils {
    public static final Logger LOGGER = LoggerFactory.getLogger(VehicleStatusUtils.class);

    /**
     * the date can be empty
     * 
     * @param requiredServices
     * @return
     */
    public static State getNextServiceDate(List<RequiredService> requiredServices) {
        ZonedDateTime farFuture = ZonedDateTime.now().plusYears(100);
        ZonedDateTime serviceDate = farFuture;
        for (RequiredService requiredService : requiredServices) {
            if (requiredService.getDateTime() != null && !requiredService.getDateTime().isEmpty()) {
                ZonedDateTime d = ZonedDateTime.parse(requiredService.getDateTime());
                if (d.isBefore(serviceDate)) {
                    serviceDate = d;
                } // else skip
            }
        }
        if (serviceDate.equals(farFuture)) {
            return UnDefType.UNDEF;
        } else {
            return DateTimeType.valueOf(serviceDate.format(DateTimeFormatter.ISO_INSTANT));
        }
    }

    /**
     * the mileage can be empty
     * 
     * @param requiredServices
     * @return
     */
    public static State getNextServiceMileage(List<RequiredService> requiredServices) {
        int serviceMileage = Integer.MAX_VALUE;
        for (RequiredService requiredService : requiredServices) {
            if (requiredService.getMileage() > 0) {
                if (requiredService.getMileage() < serviceMileage) {
                    serviceMileage = requiredService.getMileage();
                }
            }
        }
        if (serviceMileage != Integer.MAX_VALUE) {
            return QuantityType.valueOf(serviceMileage, Constants.KILOMETRE_UNIT);
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
        if (Constants.DRIVETRAIN_BEV.equals(driveTrain)) {
            if (model.endsWith(Constants.DRIVETRAIN_REX_EXTENSION)) {
                return VehicleType.ELECTRIC_REX;
            } else {
                return VehicleType.ELECTRIC;
            }
        } else if (Constants.DRIVETRAIN_PHEV.equals(driveTrain)) {
            return VehicleType.PLUGIN_HYBRID;
        } else if (Constants.DRIVETRAIN_CONV.equals(driveTrain)
                || Constants.DRIVETRAIN_MILD_HYBRID.equals(driveTrain)) {
            return VehicleType.CONVENTIONAL;
        }
        LOGGER.warn("Unknown Vehicle Type: {} | {}", model, driveTrain);
        return VehicleType.UNKNOWN;
    }
}
