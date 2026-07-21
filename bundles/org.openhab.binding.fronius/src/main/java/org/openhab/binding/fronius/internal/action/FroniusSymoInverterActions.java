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
package org.openhab.binding.fronius.internal.action;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fronius.internal.api.FroniusBatteryControl;
import org.openhab.binding.fronius.internal.api.FroniusCommunicationException;
import org.openhab.binding.fronius.internal.api.FroniusUnauthorizedException;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.ScheduleType;
import org.openhab.binding.fronius.internal.handler.FroniusSymoInverterHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deprecated variant of {@link FroniusBatteryActions} bound to the powerinverter thing, kept so that existing scripts
 * retrieving the battery control actions through the powerinverter thing keep working. The action labels are marked
 * DEPRECATED and a warning is logged on usage - please retrieve the actions through the battery thing instead.
 * <p>
 * All schedule actions accept an optional trailing weekdays parameter, given as a comma-separated list of three-letter
 * English weekday abbreviations or full names (e.g. "MON,TUE" or "MONDAY,TUESDAY"). Omitting it or passing null applies
 * the schedule to all days.
 *
 * @author Florian Hotze - Initial contribution
 * @author Christian Jonak-Möchel - Add battery charging/discharging limit actions and weekdays parameter
 * @deprecated retrieve the actions through the battery thing instead
 */
@Deprecated
@Component(scope = ServiceScope.PROTOTYPE, service = FroniusSymoInverterActions.class)
@ThingActionsScope(name = "fronius")
@NonNullByDefault
public class FroniusSymoInverterActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(FroniusSymoInverterActions.class);
    private @Nullable FroniusSymoInverterHandler handler;
    private boolean deprecationWarned;

    private static FroniusSymoInverterActions toFroniusSymoInverterActions(ThingActions actions) {
        if (actions instanceof FroniusSymoInverterActions froniusBatteryActions) {
            return froniusBatteryActions;
        }
        throw new IllegalArgumentException("The 'actions' argument is not an instance of FroniusSymoInverterActions");
    }

    public static boolean resetBatteryControl(ThingActions actions) {
        return toFroniusSymoInverterActions(actions).resetBatteryControl();
    }

    public static boolean addSchedule(ThingActions actions, LocalTime from, LocalTime until, ScheduleType scheduleType,
            QuantityType<Power> power) {
        return toFroniusSymoInverterActions(actions).addSchedule(from, until, scheduleType, power);
    }

    public static boolean addSchedule(ThingActions actions, LocalTime from, LocalTime until, ScheduleType scheduleType,
            QuantityType<Power> power, @Nullable String weekdays) {
        return toFroniusSymoInverterActions(actions).addSchedule(from, until, scheduleType, power, weekdays);
    }

    public static boolean addSchedule(ThingActions actions, LocalTime from, LocalTime until, String scheduleType,
            QuantityType<Power> power) {
        return addSchedule(actions, from, until, ScheduleType.valueOf(scheduleType), power);
    }

    public static boolean addSchedule(ThingActions actions, LocalTime from, LocalTime until, String scheduleType,
            QuantityType<Power> power, @Nullable String weekdays) {
        return addSchedule(actions, from, until, ScheduleType.valueOf(scheduleType), power, weekdays);
    }

    public static boolean addSchedule(ThingActions actions, ZonedDateTime from, ZonedDateTime until,
            ScheduleType scheduleType, QuantityType<Power> power) {
        return addSchedule(actions, from.toLocalTime(), until.toLocalTime(), scheduleType, power);
    }

    public static boolean addSchedule(ThingActions actions, ZonedDateTime from, ZonedDateTime until,
            ScheduleType scheduleType, QuantityType<Power> power, @Nullable String weekdays) {
        return addSchedule(actions, from.toLocalTime(), until.toLocalTime(), scheduleType, power, weekdays);
    }

    public static boolean addSchedule(ThingActions actions, ZonedDateTime from, ZonedDateTime until,
            String scheduleType, QuantityType<Power> power) {
        return addSchedule(actions, from.toLocalTime(), until.toLocalTime(), ScheduleType.valueOf(scheduleType), power);
    }

    public static boolean addSchedule(ThingActions actions, ZonedDateTime from, ZonedDateTime until,
            String scheduleType, QuantityType<Power> power, @Nullable String weekdays) {
        return addSchedule(actions, from.toLocalTime(), until.toLocalTime(), ScheduleType.valueOf(scheduleType), power,
                weekdays);
    }

    public static boolean holdBatteryCharge(ThingActions actions) {
        return toFroniusSymoInverterActions(actions).holdBatteryCharge();
    }

    public static boolean addHoldBatteryChargeSchedule(ThingActions actions, LocalTime from, LocalTime until) {
        return toFroniusSymoInverterActions(actions).addHoldBatteryChargeSchedule(from, until);
    }

    public static boolean addHoldBatteryChargeSchedule(ThingActions actions, LocalTime from, LocalTime until,
            @Nullable String weekdays) {
        return toFroniusSymoInverterActions(actions).addHoldBatteryChargeSchedule(from, until, weekdays);
    }

    public static boolean addHoldBatteryChargeSchedule(ThingActions actions, ZonedDateTime from, ZonedDateTime until) {
        return addHoldBatteryChargeSchedule(actions, from.toLocalTime(), until.toLocalTime());
    }

    public static boolean addHoldBatteryChargeSchedule(ThingActions actions, ZonedDateTime from, ZonedDateTime until,
            @Nullable String weekdays) {
        return addHoldBatteryChargeSchedule(actions, from.toLocalTime(), until.toLocalTime(), weekdays);
    }

    public static boolean forceBatteryCharging(ThingActions actions, QuantityType<Power> power) {
        return toFroniusSymoInverterActions(actions).forceBatteryCharging(power);
    }

    public static boolean addForcedBatteryChargingSchedule(ThingActions actions, LocalTime from, LocalTime until,
            QuantityType<Power> power) {
        return toFroniusSymoInverterActions(actions).addForcedBatteryChargingSchedule(from, until, power);
    }

    public static boolean addForcedBatteryChargingSchedule(ThingActions actions, LocalTime from, LocalTime until,
            QuantityType<Power> power, @Nullable String weekdays) {
        return toFroniusSymoInverterActions(actions).addForcedBatteryChargingSchedule(from, until, power, weekdays);
    }

    public static boolean addForcedBatteryChargingSchedule(ThingActions actions, ZonedDateTime from,
            ZonedDateTime until, QuantityType<Power> power) {
        return addForcedBatteryChargingSchedule(actions, from.toLocalTime(), until.toLocalTime(), power);
    }

    public static boolean addForcedBatteryChargingSchedule(ThingActions actions, ZonedDateTime from,
            ZonedDateTime until, QuantityType<Power> power, @Nullable String weekdays) {
        return addForcedBatteryChargingSchedule(actions, from.toLocalTime(), until.toLocalTime(), power, weekdays);
    }

    public static boolean preventBatteryCharging(ThingActions actions) {
        return toFroniusSymoInverterActions(actions).preventBatteryCharging();
    }

    public static boolean addPreventBatteryChargingSchedule(ThingActions actions, LocalTime from, LocalTime until) {
        return toFroniusSymoInverterActions(actions).addPreventBatteryChargingSchedule(from, until);
    }

    public static boolean addPreventBatteryChargingSchedule(ThingActions actions, LocalTime from, LocalTime until,
            @Nullable String weekdays) {
        return toFroniusSymoInverterActions(actions).addPreventBatteryChargingSchedule(from, until, weekdays);
    }

    public static boolean addPreventBatteryChargingSchedule(ThingActions actions, ZonedDateTime from,
            ZonedDateTime until) {
        return addPreventBatteryChargingSchedule(actions, from.toLocalTime(), until.toLocalTime());
    }

    public static boolean addPreventBatteryChargingSchedule(ThingActions actions, ZonedDateTime from,
            ZonedDateTime until, @Nullable String weekdays) {
        return addPreventBatteryChargingSchedule(actions, from.toLocalTime(), until.toLocalTime(), weekdays);
    }

    public static boolean forceBatteryDischarging(ThingActions actions, QuantityType<Power> power) {
        return toFroniusSymoInverterActions(actions).forceBatteryDischarging(power);
    }

    public static boolean addForcedBatteryDischargingSchedule(ThingActions actions, LocalTime from, LocalTime until,
            QuantityType<Power> power) {
        return toFroniusSymoInverterActions(actions).addForcedBatteryDischargingSchedule(from, until, power);
    }

    public static boolean addForcedBatteryDischargingSchedule(ThingActions actions, LocalTime from, LocalTime until,
            QuantityType<Power> power, @Nullable String weekdays) {
        return toFroniusSymoInverterActions(actions).addForcedBatteryDischargingSchedule(from, until, power, weekdays);
    }

    public static boolean addForcedBatteryDischargingSchedule(ThingActions actions, ZonedDateTime from,
            ZonedDateTime until, QuantityType<Power> power) {
        return addForcedBatteryDischargingSchedule(actions, from.toLocalTime(), until.toLocalTime(), power);
    }

    public static boolean addForcedBatteryDischargingSchedule(ThingActions actions, ZonedDateTime from,
            ZonedDateTime until, QuantityType<Power> power, @Nullable String weekdays) {
        return addForcedBatteryDischargingSchedule(actions, from.toLocalTime(), until.toLocalTime(), power, weekdays);
    }

    public static boolean setBackupReservedBatteryCapacity(ThingActions actions, int percent) {
        return toFroniusSymoInverterActions(actions).setBackupReservedBatteryCapacity(percent);
    }

    public static boolean setBackupReservedBatteryCapacity(ThingActions actions, PercentType percent) {
        return toFroniusSymoInverterActions(actions).setBackupReservedBatteryCapacity(percent);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof FroniusSymoInverterHandler h) {
            this.handler = h;
        } else if (handler != null) {
            throw new IllegalArgumentException(
                    "The 'handler' argument is not an instance of FroniusSymoInverterHandler");
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/actions.deprecated.reset-battery-control.label", description = "@text/actions.reset-battery-control.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean resetBatteryControl() {
        FroniusBatteryControl batteryControl = getBatteryControl();
        if (batteryControl != null) {
            return executeBatteryControlAction(() -> {
                batteryControl.reset();
                return true;
            });
        }
        return false;
    }

    public boolean addSchedule(LocalTime from, LocalTime until, ScheduleType scheduleType, QuantityType<Power> power) {
        return addSchedule(from, until, scheduleType, power, null);
    }

    public boolean addSchedule(LocalTime from, LocalTime until, ScheduleType scheduleType, QuantityType<Power> power,
            @Nullable String weekdays) {
        FroniusBatteryControl batteryControl = getBatteryControl();
        if (batteryControl != null) {
            return executeBatteryControlAction(() -> {
                batteryControl.addSchedule(from, until, scheduleType, power, parseWeekdays(weekdays));
                return true;
            });
        }
        return false;
    }

    @RuleAction(label = "@text/actions.deprecated.add-schedule.label", description = "@text/actions.add-schedule.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean addSchedule(
            @ActionInput(name = "from", label = "@text/actions.deprecated.from.label", description = "@text/actions.from.description", required = true) LocalTime from,
            @ActionInput(name = "until", label = "@text/actions.deprecated.until.label", description = "@text/actions.until.description", required = true) LocalTime until,
            @ActionInput(name = "scheduleType", label = "@text/actions.deprecated.schedule-type.label", description = "@text/actions.schedule-type.description", required = true) String scheduleType,
            @ActionInput(name = "power", label = "@text/actions.deprecated.power.label", description = "@text/actions.power.description", type = "QuantityType<Power>", required = true) QuantityType<Power> power,
            @ActionInput(name = "weekdays", label = "@text/actions.deprecated.weekdays.label", description = "@text/actions.weekdays.description", type = "java.lang.String", required = false) @Nullable String weekdays) {
        return addSchedule(from, until, ScheduleType.valueOf(scheduleType), power, weekdays);
    }

    public boolean addSchedule(LocalTime from, LocalTime until, String scheduleType, QuantityType<Power> power) {
        return addSchedule(from, until, ScheduleType.valueOf(scheduleType), power);
    }

    public boolean addSchedule(ZonedDateTime from, ZonedDateTime until, ScheduleType scheduleType,
            QuantityType<Power> power) {
        return addSchedule(from.toLocalTime(), until.toLocalTime(), scheduleType, power);
    }

    public boolean addSchedule(ZonedDateTime from, ZonedDateTime until, ScheduleType scheduleType,
            QuantityType<Power> power, @Nullable String weekdays) {
        return addSchedule(from.toLocalTime(), until.toLocalTime(), scheduleType, power, weekdays);
    }

    public boolean addSchedule(ZonedDateTime from, ZonedDateTime until, String scheduleType,
            QuantityType<Power> power) {
        return addSchedule(from.toLocalTime(), until.toLocalTime(), ScheduleType.valueOf(scheduleType), power);
    }

    public boolean addSchedule(ZonedDateTime from, ZonedDateTime until, String scheduleType, QuantityType<Power> power,
            @Nullable String weekdays) {
        return addSchedule(from.toLocalTime(), until.toLocalTime(), ScheduleType.valueOf(scheduleType), power,
                weekdays);
    }

    @RuleAction(label = "@text/actions.deprecated.hold-battery-charge.label", description = "@text/actions.hold-battery-charge.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean holdBatteryCharge() {
        FroniusBatteryControl batteryControl = getBatteryControl();
        if (batteryControl != null) {
            return executeBatteryControlAction(() -> {
                batteryControl.holdBatteryCharge();
                return true;
            });
        }
        return false;
    }

    @RuleAction(label = "@text/actions.deprecated.add-hold-battery-charge-schedule.label", description = "@text/actions.add-hold-battery-charge-schedule.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean addHoldBatteryChargeSchedule(
            @ActionInput(name = "from", label = "@text/actions.deprecated.from.label", description = "@text/actions.from.description", required = true) LocalTime from,
            @ActionInput(name = "until", label = "@text/actions.deprecated.until.label", description = "@text/actions.until.description", required = true) LocalTime until,
            @ActionInput(name = "weekdays", label = "@text/actions.deprecated.weekdays.label", description = "@text/actions.weekdays.description", type = "java.lang.String", required = false) @Nullable String weekdays) {
        return addSchedule(from, until, ScheduleType.DISCHARGE_MAX, new QuantityType<>(0, Units.WATT), weekdays);
    }

    public boolean addHoldBatteryChargeSchedule(LocalTime from, LocalTime until) {
        return addHoldBatteryChargeSchedule(from, until, null);
    }

    public boolean addHoldBatteryChargeSchedule(ZonedDateTime from, ZonedDateTime until) {
        return addHoldBatteryChargeSchedule(from.toLocalTime(), until.toLocalTime());
    }

    public boolean addHoldBatteryChargeSchedule(ZonedDateTime from, ZonedDateTime until, @Nullable String weekdays) {
        return addHoldBatteryChargeSchedule(from.toLocalTime(), until.toLocalTime(), weekdays);
    }

    @RuleAction(label = "@text/actions.deprecated.force-battery-charging.label", description = "@text/actions.force-battery-charging.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean forceBatteryCharging(
            @ActionInput(name = "power", label = "@text/actions.deprecated.power.label", description = "@text/actions.power.description", type = "QuantityType<Power>", required = true) QuantityType<Power> power) {
        FroniusBatteryControl batteryControl = getBatteryControl();
        if (batteryControl != null) {
            return executeBatteryControlAction(() -> {
                batteryControl.forceBatteryCharging(power);
                return true;
            });
        }
        return false;
    }

    @RuleAction(label = "@text/actions.deprecated.add-forced-battery-charging-schedule.label", description = "@text/actions.add-forced-battery-charging-schedule.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean addForcedBatteryChargingSchedule(
            @ActionInput(name = "from", label = "@text/actions.deprecated.from.label", description = "@text/actions.from.description", required = true) LocalTime from,
            @ActionInput(name = "until", label = "@text/actions.deprecated.until.label", description = "@text/actions.until.description", required = true) LocalTime until,
            @ActionInput(name = "power", label = "@text/actions.deprecated.power.label", description = "@text/actions.power.description", type = "QuantityType<Power>", required = true) QuantityType<Power> power,
            @ActionInput(name = "weekdays", label = "@text/actions.deprecated.weekdays.label", description = "@text/actions.weekdays.description", type = "java.lang.String", required = false) @Nullable String weekdays) {
        return addSchedule(from, until, ScheduleType.CHARGE_MIN, power, weekdays);
    }

    public boolean addForcedBatteryChargingSchedule(LocalTime from, LocalTime until, QuantityType<Power> power) {
        return addForcedBatteryChargingSchedule(from, until, power, null);
    }

    public boolean addForcedBatteryChargingSchedule(ZonedDateTime from, ZonedDateTime until,
            QuantityType<Power> power) {
        return addForcedBatteryChargingSchedule(from.toLocalTime(), until.toLocalTime(), power);
    }

    public boolean addForcedBatteryChargingSchedule(ZonedDateTime from, ZonedDateTime until, QuantityType<Power> power,
            @Nullable String weekdays) {
        return addForcedBatteryChargingSchedule(from.toLocalTime(), until.toLocalTime(), power, weekdays);
    }

    @RuleAction(label = "@text/actions.deprecated.prevent-battery-charging.label", description = "@text/actions.prevent-battery-charging.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean preventBatteryCharging() {
        FroniusBatteryControl batteryControl = getBatteryControl();
        if (batteryControl != null) {
            return executeBatteryControlAction(() -> {
                batteryControl.preventBatteryCharging();
                return true;
            });
        }
        return false;
    }

    @RuleAction(label = "@text/actions.deprecated.add-prevent-battery-charging-schedule.label", description = "@text/actions.add-prevent-battery-charging-schedule.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean addPreventBatteryChargingSchedule(
            @ActionInput(name = "from", label = "@text/actions.deprecated.from.label", description = "@text/actions.from.description", required = true) LocalTime from,
            @ActionInput(name = "until", label = "@text/actions.deprecated.until.label", description = "@text/actions.until.description", required = true) LocalTime until,
            @ActionInput(name = "weekdays", label = "@text/actions.deprecated.weekdays.label", description = "@text/actions.weekdays.description", type = "java.lang.String", required = false) @Nullable String weekdays) {
        return addSchedule(from, until, ScheduleType.CHARGE_MAX, new QuantityType<>(0, Units.WATT), weekdays);
    }

    public boolean addPreventBatteryChargingSchedule(LocalTime from, LocalTime until) {
        return addPreventBatteryChargingSchedule(from, until, null);
    }

    public boolean addPreventBatteryChargingSchedule(ZonedDateTime from, ZonedDateTime until) {
        return addPreventBatteryChargingSchedule(from.toLocalTime(), until.toLocalTime());
    }

    public boolean addPreventBatteryChargingSchedule(ZonedDateTime from, ZonedDateTime until,
            @Nullable String weekdays) {
        return addPreventBatteryChargingSchedule(from.toLocalTime(), until.toLocalTime(), weekdays);
    }

    @RuleAction(label = "@text/actions.deprecated.force-battery-discharging.label", description = "@text/actions.force-battery-discharging.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean forceBatteryDischarging(
            @ActionInput(name = "power", label = "@text/actions.deprecated.power.label", description = "@text/actions.power.description", type = "QuantityType<Power>", required = true) QuantityType<Power> power) {
        FroniusBatteryControl batteryControl = getBatteryControl();
        if (batteryControl != null) {
            return executeBatteryControlAction(() -> {
                batteryControl.forceBatteryDischarging(power);
                return true;
            });
        }
        return false;
    }

    @RuleAction(label = "@text/actions.deprecated.add-forced-battery-discharging-schedule.label", description = "@text/actions.add-forced-battery-discharging-schedule.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean addForcedBatteryDischargingSchedule(
            @ActionInput(name = "from", label = "@text/actions.deprecated.from.label", description = "@text/actions.from.description", required = true) LocalTime from,
            @ActionInput(name = "until", label = "@text/actions.deprecated.until.label", description = "@text/actions.until.description", required = true) LocalTime until,
            @ActionInput(name = "power", label = "@text/actions.deprecated.power.label", description = "@text/actions.power.description", type = "QuantityType<Power>", required = true) QuantityType<Power> power,
            @ActionInput(name = "weekdays", label = "@text/actions.deprecated.weekdays.label", description = "@text/actions.weekdays.description", type = "java.lang.String", required = false) @Nullable String weekdays) {
        return addSchedule(from, until, ScheduleType.DISCHARGE_MIN, power, weekdays);
    }

    public boolean addForcedBatteryDischargingSchedule(LocalTime from, LocalTime until, QuantityType<Power> power) {
        return addForcedBatteryDischargingSchedule(from, until, power, null);
    }

    public boolean addForcedBatteryDischargingSchedule(ZonedDateTime from, ZonedDateTime until,
            QuantityType<Power> power) {
        return addForcedBatteryDischargingSchedule(from.toLocalTime(), until.toLocalTime(), power);
    }

    public boolean addForcedBatteryDischargingSchedule(ZonedDateTime from, ZonedDateTime until,
            QuantityType<Power> power, @Nullable String weekdays) {
        return addForcedBatteryDischargingSchedule(from.toLocalTime(), until.toLocalTime(), power, weekdays);
    }

    @RuleAction(label = "@text/actions.deprecated.backup-reserved-battery-capacity.label", description = "@text/actions.backup-reserved-battery-capacity.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean setBackupReservedBatteryCapacity(
            @ActionInput(name = "percent", label = "@text/actions.deprecated.soc.label", description = "@text/actions.soc.description", required = true) int percent) {
        FroniusBatteryControl batteryControl = getBatteryControl();
        if (batteryControl != null) {
            return executeBatteryControlAction(() -> {
                try {
                    batteryControl.setBackupReservedCapacity(percent);
                } catch (IllegalArgumentException e) {
                    logger.warn("Failed to set backup reserved battery capacity: {}", e.getMessage());
                    return false;
                }
                return true;
            });
        }
        return false;
    }

    public boolean setBackupReservedBatteryCapacity(PercentType percent) {
        return setBackupReservedBatteryCapacity(percent.intValue());
    }

    private static Set<DayOfWeek> parseWeekdays(@Nullable String weekdays) {
        if (weekdays == null || weekdays.isBlank()) {
            return EnumSet.allOf(DayOfWeek.class);
        }
        Set<DayOfWeek> result = EnumSet.noneOf(DayOfWeek.class);
        for (String token : weekdays.split(",")) {
            String day = token.strip().toUpperCase(Locale.ROOT);
            switch (day) {
                case "MON" -> result.add(DayOfWeek.MONDAY);
                case "TUE" -> result.add(DayOfWeek.TUESDAY);
                case "WED" -> result.add(DayOfWeek.WEDNESDAY);
                case "THU" -> result.add(DayOfWeek.THURSDAY);
                case "FRI" -> result.add(DayOfWeek.FRIDAY);
                case "SAT" -> result.add(DayOfWeek.SATURDAY);
                case "SUN" -> result.add(DayOfWeek.SUNDAY);
                default -> {
                    try {
                        result.add(DayOfWeek.valueOf(day));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid weekday '" + token.strip()
                                + "', use three-letter abbreviations or full English weekday names, e.g. \"MON,TUE\" or \"MONDAY,TUESDAY\"");
                    }
                }
            }
        }
        return result;
    }

    private @Nullable FroniusBatteryControl getBatteryControl() {
        FroniusSymoInverterHandler handler = this.handler;
        if (handler != null) {
            if (!deprecationWarned) {
                deprecationWarned = true;
                logger.warn(
                        "Using the battery control actions through the powerinverter thing is deprecated, please retrieve them through the battery thing instead.");
            }
            return handler.getBatteryControl();
        }
        return null;
    }

    @FunctionalInterface
    public interface ExceptionalSupplier<T> {
        T get() throws FroniusCommunicationException, FroniusUnauthorizedException;
    }

    /**
     * Executes a battery control action provided through a {@link ExceptionalSupplier} and handles common exceptions.
     *
     * @param supplier the action to execute
     * @return the result of the action
     */
    private boolean executeBatteryControlAction(ExceptionalSupplier<Boolean> supplier) {
        try {
            return supplier.get();
        } catch (FroniusCommunicationException e) {
            logger.warn("Failed to execute battery control action", e);
        } catch (FroniusUnauthorizedException e) {
            logger.warn("Failed to execute battery control action: Invalid username or password");
        }
        return false;
    }
}
