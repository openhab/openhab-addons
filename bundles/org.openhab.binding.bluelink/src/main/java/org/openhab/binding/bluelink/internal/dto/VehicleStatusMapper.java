/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bluelink.internal.dto;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.openhab.binding.bluelink.internal.dto.ChargeLimitsRequest.PLUG_TYPE_AC;
import static org.openhab.binding.bluelink.internal.dto.ChargeLimitsRequest.PLUG_TYPE_DC;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluelink.internal.api.VehicleStatus;
import org.openhab.binding.bluelink.internal.dto.eu.VehicleStatusEU;
import org.openhab.binding.bluelink.internal.dto.us.VehicleStatusUS;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapper class for mapping region-specific DTOs to generic {@link VehicleStatus}.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class VehicleStatusMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleStatusMapper.class);

    private static final DateTimeFormatter EU_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Map from the EU-specific DTO
     */
    public static @Nullable VehicleStatus map(@Nullable VehicleStatusEU eu) {
        if (eu == null) {
            return null;
        }

        var status = eu.info().status();
        var loc = eu.info().location();
        var odometer = eu.info().odometer();

        Instant lastUpdate = null;
        if (status.dateTime() != null) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(status.dateTime(), EU_DATETIME_FORMATTER);
                lastUpdate = ldt.atZone(ZoneId.systemDefault()).toInstant();
            } catch (final DateTimeParseException e) {
                LOGGER.warn("unexpected lastUpdate format: {}", status.dateTime());
            }
        }

        return new VehicleStatus(lastUpdate,
                loc != null ? new VehicleStatus.VehicleLocation(loc.coord().lat(), loc.coord().lon(), loc.coord().alt())
                        : null,
                status.engine(), status.doorLock(),
                new VehicleStatus.DoorState(status.doorOpen().frontLeft() > 0, status.doorOpen().frontRight() > 0,
                        status.doorOpen().backLeft() > 0, status.doorOpen().backRight() > 0),
                null, status.trunkOpen(), status.hoodOpen(), status.airCtrlOn(), status.airTemp().getTemperature(),
                status.defrost(), status.steerWheelHeat() > 0, status.sideBackWindowHeat() > 0, null,
                status.seatHeaterVentState(), status.battery12V().stateOfCharge(), mapEvStatus(status.evStatus()),
                status.dte() != null ? status.dte().getRange() : UnDefType.UNDEF, status.fuelLevel(),
                status.lowFuelLevel(), status.washerFluidStatus(), status.brakeOilStatus(),
                status.smartKeyBatteryWarning(),
                new VehicleStatus.TirePressureWarning(status.tirePressure().all() > 0,
                        status.tirePressure().frontLeft() > 0, status.tirePressure().frontRight() > 0,
                        status.tirePressure().rearLeft() > 0, status.tirePressure().rearRight() > 0),
                odometer != null ? odometer.getRange() : null);
    }

    private static VehicleStatus.@Nullable EvStatus mapEvStatus(VehicleStatusEU.@Nullable EvStatus evStatus) {
        if (evStatus == null) {
            return null;
        }

        List<VehicleStatus.TargetSoC> targetSoCs = new ArrayList<>();
        var reservChargeInfos = evStatus.reserveChargeInfos();
        if (reservChargeInfos != null && reservChargeInfos.targetSocList() != null) {
            for (final VehicleStatusEU.TargetSOC target : reservChargeInfos.targetSocList()) {
                if (target.plugType() == PLUG_TYPE_DC) {
                    targetSoCs.add(new VehicleStatus.TargetSoC("DC", target.targetSocLevel()));
                } else if (target.plugType() == PLUG_TYPE_AC) {
                    targetSoCs.add(new VehicleStatus.TargetSoC("AC", target.targetSocLevel()));
                }
            }
        }

        VehicleStatus.RangeByFuel rangeByFuel = mapRangeByFuel(evStatus.drivingDistance());

        return new VehicleStatus.EvStatus(evStatus.isCharging(), evStatus.batteryPercentage(),
                evStatus.plugStatus() > 0, targetSoCs, rangeByFuel,
                new VehicleStatus.ChargeRemainingTime(evStatus.remainTime().current().value(),
                        evStatus.remainTime().fast().value(), evStatus.remainTime().portable().value(),
                        evStatus.remainTime().station().value()));
    }

    /**
     * Map from the US-specific DTO
     */
    public static @Nullable VehicleStatus map(@Nullable VehicleStatusUS us, QuantityType<Length> odometer) {
        if (us == null) {
            return null;
        }

        var status = us.vehicleStatus();
        var loc = us.vehicleStatus().vehicleLocation().coord();

        Instant lastUpdate = null;
        if (status.dateTime() != null) {
            try {
                lastUpdate = ISO_ZONED_DATE_TIME.parse(status.dateTime(), Instant::from);
            } catch (final DateTimeParseException e) {
                LOGGER.warn("Unexpected lastUpdate format: {}", status.dateTime());
            }
        }

        return new VehicleStatus(lastUpdate,
                new VehicleStatus.VehicleLocation(loc.latitude(), loc.longitude(), loc.altitude()), status.engine(),
                status.doorLock(),
                new VehicleStatus.DoorState(status.doorOpen().frontLeft() > 0, status.doorOpen().frontRight() > 0,
                        status.doorOpen().backLeft() > 0, status.doorOpen().backRight() > 0),
                new VehicleStatus.WindowState(status.windowOpen().frontLeft() > 0, status.windowOpen().frontRight() > 0,
                        status.windowOpen().backLeft() > 0, status.windowOpen().backRight() > 0),
                status.trunkOpen(), status.hoodOpen(), status.airCtrlOn(), status.airTemp().getTemperature(),
                status.defrost(), status.steerWheelHeat() > 0, status.sideBackWindowHeat() > 0,
                status.sideMirrorHeat() > 0, status.seatHeaterVentState(), status.battery().stateOfCharge(),
                mapEvStatus(status.evStatus()), status.dte() != null ? status.dte().getRange() : UnDefType.UNDEF,
                status.fuelLevel(), status.lowFuelLight(), status.washerFluidStatus(), status.brakeOilStatus(),
                status.smartKeyBatteryWarning(),
                new VehicleStatus.TirePressureWarning(status.tirePressureWarning().all() > 0,
                        status.tirePressureWarning().frontLeft() > 0, status.tirePressureWarning().frontRight() > 0,
                        status.tirePressureWarning().rearLeft() > 0, status.tirePressureWarning().rearRight() > 0),
                odometer);
    }

    private static VehicleStatus.@Nullable EvStatus mapEvStatus(VehicleStatusUS.@Nullable EvStatus evStatus) {
        if (evStatus == null) {
            return null;
        }

        List<VehicleStatus.TargetSoC> targetSoCs = new ArrayList<>();
        var reservChargeInfos = evStatus.reservChargeInfos();
        if (reservChargeInfos != null && reservChargeInfos.targetSocList() != null) {
            for (final VehicleStatusUS.TargetSOC target : reservChargeInfos.targetSocList()) {
                if (target.plugType() == PLUG_TYPE_DC) {
                    targetSoCs.add(new VehicleStatus.TargetSoC("DC", target.targetSocLevel()));
                } else if (target.plugType() == PLUG_TYPE_AC) {
                    targetSoCs.add(new VehicleStatus.TargetSoC("AC", target.targetSocLevel()));
                }
            }
        }

        VehicleStatus.RangeByFuel rangeByFuel = mapRangeByFuel(evStatus.drvDistance());

        return new VehicleStatus.EvStatus(evStatus.batteryCharge(), evStatus.batteryStatus(),
                evStatus.batteryPlugin() > 0, targetSoCs, rangeByFuel,
                new VehicleStatus.ChargeRemainingTime(evStatus.remainTime2().current().value(),
                        evStatus.remainTime2().fast().value(), evStatus.remainTime2().portable().value(),
                        evStatus.remainTime2().station().value()));
    }

    private static VehicleStatus.@Nullable RangeByFuel mapRangeByFuel(
            @Nullable List<VehicleStatusUS.DrivingRange> drivingRange) {
        if (drivingRange != null && !drivingRange.isEmpty()) {
            var driveDistance1 = drivingRange.getFirst();
            var rangeInfos = driveDistance1.rangeByFuel();
            return VehicleStatus.RangeByFuel.from(rangeInfos.total(), rangeInfos.ev(), rangeInfos.gas());
        }
        return null;
    }
}
