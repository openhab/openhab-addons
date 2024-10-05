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
package org.openhab.binding.fronius.internal.action;

import java.time.LocalTime;
import java.time.ZonedDateTime;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fronius.internal.handler.FroniusSymoInverterHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

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
    private @Nullable FroniusSymoInverterHandler handler;

    public static void resetBatteryControl(ThingActions actions) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            froniusSymoInverterActions.resetBatteryControl();
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static void holdBatteryCharge(ThingActions actions) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            froniusSymoInverterActions.holdBatteryCharge();
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static void addHoldBatteryChargeSchedule(ThingActions actions, LocalTime from, LocalTime until) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            froniusSymoInverterActions.addHoldBatteryChargeSchedule(from, until);
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static void addHoldBatteryChargeSchedule(ThingActions actions, ZonedDateTime from, ZonedDateTime until) {
        addHoldBatteryChargeSchedule(actions, from.toLocalTime(), until.toLocalTime());
    }

    public static void forceBatteryCharging(ThingActions actions, QuantityType<Power> power) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            froniusSymoInverterActions.forceBatteryCharging(power);
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static void addForcedBatteryChargingSchedule(ThingActions actions, LocalTime from, LocalTime until,
            QuantityType<Power> power) {
        if (actions instanceof FroniusSymoInverterActions froniusSymoInverterActions) {
            froniusSymoInverterActions.addForcedBatteryChargingSchedule(from, until, power);
        } else {
            throw new IllegalArgumentException(
                    "The 'actions' argument is not an instance of FroniusSymoInverterActions");
        }
    }

    public static void addForcedBatteryChargingSchedule(ThingActions actions, ZonedDateTime from, ZonedDateTime until,
            QuantityType<Power> power) {
        addForcedBatteryChargingSchedule(actions, from.toLocalTime(), until.toLocalTime(), power);
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
    public void resetBatteryControl() {
        FroniusSymoInverterHandler handler = this.handler;
        if (handler != null) {
            handler.resetBatteryControl();
        }
    }

    @RuleAction(label = "@text/actions.hold-battery-charge.label", description = "@text/actions.hold-battery-charge.description")
    public void holdBatteryCharge() {
        FroniusSymoInverterHandler handler = this.handler;
        if (handler != null) {
            handler.holdBatteryCharge();
        }
    }

    @RuleAction(label = "@text/actions.add-hold-battery-charge-schedule.label", description = "@text/actions.add-hold-battery-charge-schedule.description")
    public void addHoldBatteryChargeSchedule(
            @ActionInput(name = "from", label = "@text/actions.from.label", description = "@text/actions.from.description") LocalTime from,
            @ActionInput(name = "until", label = "@text/actions.until.label", description = "@text/actions.until.description") LocalTime until) {
        FroniusSymoInverterHandler handler = this.handler;
        if (handler != null) {
            handler.addHoldBatteryChargeSchedule(from, until);
        }
    }

    public void addHoldBatteryChargeSchedule(ZonedDateTime from, ZonedDateTime until) {
        addHoldBatteryChargeSchedule(from.toLocalTime(), until.toLocalTime());
    }

    @RuleAction(label = "@text/actions.force-battery-charging.label", description = "@text/actions.force-battery-charging.description")
    public void forceBatteryCharging(
            @ActionInput(name = "power", label = "@text/actions.power.label", description = "@text/actions.power.label", type = "QuantityType<Power>") QuantityType<Power> power) {
        FroniusSymoInverterHandler handler = this.handler;
        if (handler != null) {
            handler.forceBatteryCharging(power);
        }
    }

    @RuleAction(label = "@text/actions.add-forced-battery-charging-schedule.label", description = "@text/actions.add-forced-battery-charging-schedule.description")
    public void addForcedBatteryChargingSchedule(
            @ActionInput(name = "from", label = "@text/actions.from.label", description = "@text/actions.from.description") LocalTime from,
            @ActionInput(name = "until", label = "@text/actions.until.label", description = "@text/actions.until.description") LocalTime until,
            @ActionInput(name = "power", label = "@text/actions.power.label", description = "@text/actions.power.label", type = "QuantityType<Power>") QuantityType<Power> power) {
        FroniusSymoInverterHandler handler = this.handler;
        if (handler != null) {
            handler.addForcedBatteryChargingSchedule(from, until, power);
        }
    }

    public void addForcedBatteryChargingSchedule(ZonedDateTime from, ZonedDateTime until, QuantityType<Power> power) {
        addForcedBatteryChargingSchedule(from.toLocalTime(), until.toLocalTime(), power);
    }
}
