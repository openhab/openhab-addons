/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.time.LocalTime;
import java.time.ZonedDateTime;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fronius.internal.api.FroniusBatteryControl;
import org.openhab.binding.fronius.internal.api.FroniusCommunicationException;
import org.openhab.binding.fronius.internal.api.FroniusUnauthorizedException;
import org.openhab.binding.fronius.internal.handler.FroniusSymoInverterHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link ThingActions} interface used for controlling battery charging and discharging for
 * Fronius hybrid inverters.
 *
 * @author Florian Hotze - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = FroniusSymoInverterActions.class)
@ThingActionsScope(name = "fronius")
@NonNullByDefault
public class FroniusSymoInverterActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(FroniusSymoInverterActions.class);
    private @Nullable FroniusSymoInverterHandler handler;

    public static boolean resetBatteryControl(ThingActions actions) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            return froniusSymoInverterActions.resetBatteryControl();
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static boolean holdBatteryCharge(ThingActions actions) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            return froniusSymoInverterActions.holdBatteryCharge();
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static boolean addHoldBatteryChargeSchedule(ThingActions actions, LocalTime from, LocalTime until) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            return froniusSymoInverterActions.addHoldBatteryChargeSchedule(from, until);
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static boolean addHoldBatteryChargeSchedule(ThingActions actions, ZonedDateTime from, ZonedDateTime until) {
        return addHoldBatteryChargeSchedule(actions, from.toLocalTime(), until.toLocalTime());
    }

    public static boolean forceBatteryCharging(ThingActions actions, QuantityType<Power> power) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            return froniusSymoInverterActions.forceBatteryCharging(power);
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static boolean addForcedBatteryChargingSchedule(ThingActions actions, LocalTime from, LocalTime until,
            QuantityType<Power> power) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            return froniusSymoInverterActions.addForcedBatteryChargingSchedule(from, until, power);
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static boolean addForcedBatteryChargingSchedule(ThingActions actions, ZonedDateTime from,
            ZonedDateTime until, QuantityType<Power> power) {
        return addForcedBatteryChargingSchedule(actions, from.toLocalTime(), until.toLocalTime(), power);
    }

    public static boolean preventBatteryCharging(ThingActions actions) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            return froniusSymoInverterActions.preventBatteryCharging();
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static boolean addPreventBatteryChargingSchedule(ThingActions actions, LocalTime from, LocalTime until) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            return froniusSymoInverterActions.addPreventBatteryChargingSchedule(from, until);
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static boolean addPreventBatteryChargingSchedule(ThingActions actions, ZonedDateTime from,
            ZonedDateTime until) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            return froniusSymoInverterActions.addPreventBatteryChargingSchedule(from, until);
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static boolean forceBatteryDischarging(ThingActions actions, QuantityType<Power> power) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            return froniusSymoInverterActions.forceBatteryDischarging(power);
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static boolean addForcedBatteryDischargingSchedule(ThingActions actions, LocalTime from, LocalTime until,
            QuantityType<Power> power) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            return froniusSymoInverterActions.addForcedBatteryDischargingSchedule(from, until, power);
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static boolean addForcedBatteryDischargingSchedule(ThingActions actions, ZonedDateTime from,
            ZonedDateTime until, QuantityType<Power> power) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            return froniusSymoInverterActions.addForcedBatteryDischargingSchedule(from, until, power);
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static boolean setBackupReservedBatteryCapacity(ThingActions actions, int percent) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            return froniusSymoInverterActions.setBackupReservedBatteryCapacity(percent);
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static boolean setBackupReservedBatteryCapacity(ThingActions actions, PercentType percent) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            return froniusSymoInverterActions.setBackupReservedBatteryCapacity(percent);
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (FroniusSymoInverterHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/actions.reset-battery-control.label", description = "@text/actions.reset-battery-control.description")
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

    @RuleAction(label = "@text/actions.hold-battery-charge.label", description = "@text/actions.hold-battery-charge.description")
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

    @RuleAction(label = "@text/actions.add-hold-battery-charge-schedule.label", description = "@text/actions.add-hold-battery-charge-schedule.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean addHoldBatteryChargeSchedule(
            @ActionInput(name = "from", label = "@text/actions.from.label", description = "@text/actions.from.description", required = true) LocalTime from,
            @ActionInput(name = "until", label = "@text/actions.until.label", description = "@text/actions.until.description", required = true) LocalTime until) {
        FroniusBatteryControl batteryControl = getBatteryControl();
        if (batteryControl != null) {
            return executeBatteryControlAction(() -> {
                batteryControl.addHoldBatteryChargeSchedule(from, until);
                return true;
            });
        }
        return false;
    }

    public boolean addHoldBatteryChargeSchedule(ZonedDateTime from, ZonedDateTime until) {
        return addHoldBatteryChargeSchedule(from.toLocalTime(), until.toLocalTime());
    }

    @RuleAction(label = "@text/actions.force-battery-charging.label", description = "@text/actions.force-battery-charging.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean forceBatteryCharging(
            @ActionInput(name = "power", label = "@text/actions.power.label", description = "@text/actions.power.label", type = "QuantityType<Power>", required = true) QuantityType<Power> power) {
        FroniusBatteryControl batteryControl = getBatteryControl();
        if (batteryControl != null) {
            return executeBatteryControlAction(() -> {
                batteryControl.forceBatteryCharging(power);
                return true;
            });
        }
        return false;
    }

    @RuleAction(label = "@text/actions.add-forced-battery-charging-schedule.label", description = "@text/actions.add-forced-battery-charging-schedule.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean addForcedBatteryChargingSchedule(
            @ActionInput(name = "from", label = "@text/actions.from.label", description = "@text/actions.from.description", required = true) LocalTime from,
            @ActionInput(name = "until", label = "@text/actions.until.label", description = "@text/actions.until.description", required = true) LocalTime until,
            @ActionInput(name = "power", label = "@text/actions.power.label", description = "@text/actions.power.label", type = "QuantityType<Power>", required = true) QuantityType<Power> power) {
        FroniusBatteryControl batteryControl = getBatteryControl();
        if (batteryControl != null) {
            return executeBatteryControlAction(() -> {
                batteryControl.addForcedBatteryChargingSchedule(from, until, power);
                return true;
            });
        }
        return false;
    }

    public boolean addForcedBatteryChargingSchedule(ZonedDateTime from, ZonedDateTime until,
            QuantityType<Power> power) {
        return addForcedBatteryChargingSchedule(from.toLocalTime(), until.toLocalTime(), power);
    }

    @RuleAction(label = "@text/actions.prevent-battery-charging.label", description = "@text/actions.prevent-battery-charging.description")
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

    @RuleAction(label = "@text/actions.add-prevent-battery-charging-schedule.label", description = "@text/actions.add-prevent-battery-charging-schedule.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean addPreventBatteryChargingSchedule(
            @ActionInput(name = "from", label = "@text/actions.from.label", description = "@text/actions.from.description", required = true) LocalTime from,
            @ActionInput(name = "until", label = "@text/actions.until.label", description = "@text/actions.until.description", required = true) LocalTime until) {
        FroniusBatteryControl batteryControl = getBatteryControl();
        if (batteryControl != null) {
            return executeBatteryControlAction(() -> {
                batteryControl.addPreventBatteryChargingSchedule(from, until);
                return true;
            });
        }
        return false;
    }

    public boolean addPreventBatteryChargingSchedule(ZonedDateTime from, ZonedDateTime until) {
        return addPreventBatteryChargingSchedule(from.toLocalTime(), until.toLocalTime());
    }

    @RuleAction(label = "@text/actions.force-battery-discharging.label", description = "@text/actions.force-battery-discharging.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean forceBatteryDischarging(
            @ActionInput(name = "power", label = "@text/actions.power.label", description = "@text/actions.power.label", type = "QuantityType<Power>", required = true) QuantityType<Power> power) {
        FroniusBatteryControl batteryControl = getBatteryControl();
        if (batteryControl != null) {
            return executeBatteryControlAction(() -> {
                batteryControl.forceBatteryDischarging(power);
                return true;
            });
        }
        return false;
    }

    @RuleAction(label = "@text/actions.add-forced-battery-discharging-schedule.label", description = "@text/actions.add-forced-battery-discharging-schedule.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean addForcedBatteryDischargingSchedule(
            @ActionInput(name = "from", label = "@text/actions.from.label", description = "@text/actions.from.description", required = true) LocalTime from,
            @ActionInput(name = "until", label = "@text/actions.until.label", description = "@text/actions.until.description", required = true) LocalTime until,
            @ActionInput(name = "power", label = "@text/actions.power.label", description = "@text/actions.power.label", type = "QuantityType<Power>", required = true) QuantityType<Power> power) {
        FroniusBatteryControl batteryControl = getBatteryControl();
        if (batteryControl != null) {
            return executeBatteryControlAction(() -> {
                batteryControl.addForcedBatteryDischargingSchedule(from, until, power);
                return true;
            });
        }
        return false;
    }

    public boolean addForcedBatteryDischargingSchedule(ZonedDateTime from, ZonedDateTime until,
            QuantityType<Power> power) {
        return addForcedBatteryDischargingSchedule(from.toLocalTime(), until.toLocalTime(), power);
    }

    @RuleAction(label = "@text/actions.backup-reserved-battery-capacity.label", description = "@text/actions.backup-reserved-battery-capacity.description")
    public @ActionOutput(type = "boolean", label = "Success") boolean setBackupReservedBatteryCapacity(
            @ActionInput(name = "percent", label = "@text/actions.soc.label", description = "@text/actions.soc.description", required = true) int percent) {
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

    private @Nullable FroniusBatteryControl getBatteryControl() {
        FroniusSymoInverterHandler handler = this.handler;
        if (handler != null) {
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
