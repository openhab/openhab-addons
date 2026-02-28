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
package org.openhab.binding.bluelink.internal.handler;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluelink.internal.api.BluelinkApiException;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Actions for the Bluelink binding.
 *
 * @author Marcus Better - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = VehicleActions.class)
@ThingActionsScope(name = "bluelink")
@NonNullByDefault
public class VehicleActions implements ThingActions {
    private @Nullable BluelinkVehicleHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (BluelinkVehicleHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.force-refresh.label", description = "@text/action.force-refresh.desc")
    public void forceRefresh() {
        final BluelinkVehicleHandler hnd = handler;
        if (hnd != null) {
            hnd.refreshVehicleStatus(true);
        }
    }

    @RuleAction(label = "@text/action.lock.label")
    @ActionOutput(type = "boolean")
    public boolean lock() {
        final BluelinkVehicleHandler hnd = handler;
        try {
            return hnd != null && hnd.lock();
        } catch (final BluelinkApiException e) {
            return false;
        }
    }

    @RuleAction(label = "@text/action.unlock.label")
    @ActionOutput(type = "boolean")
    public boolean unlock() {
        final BluelinkVehicleHandler hnd = handler;
        try {
            return hnd != null && hnd.unlock();
        } catch (final BluelinkApiException e) {
            return false;
        }
    }

    @RuleAction(label = "@text/action.start-charging.label")
    @ActionOutput(type = "boolean")
    public boolean startCharging() {
        final BluelinkVehicleHandler hnd = handler;
        try {
            return hnd != null && hnd.startCharging();
        } catch (final BluelinkApiException e) {
            return false;
        }
    }

    @RuleAction(label = "@text/action.stop-charging.label")
    @ActionOutput(type = "boolean")
    public boolean stopCharging() {
        final BluelinkVehicleHandler hnd = handler;
        try {
            return hnd != null && hnd.stopCharging();
        } catch (final BluelinkApiException e) {
            return false;
        }
    }

    @RuleAction(label = "@text/action.climate-start.label")
    @ActionOutput(type = "boolean")
    public boolean climateStart(
            final @ActionInput(name = "temperature", type = "QuantityType<Temperature>", label = "@text/action.climate-start.input.temperature.label") QuantityType<Temperature> temperature,
            final @ActionInput(name = "heating", label = "@text/action.climate-start.input.heating.label", description = "@text/action.climate-start.input.heating.desc") boolean heating,
            final @ActionInput(name = "defrost", label = "@text/action.climate-start.input.defrost.label") boolean defrost) {
        final BluelinkVehicleHandler hnd = handler;
        try {
            return hnd != null && hnd.climateStart(temperature, heating, defrost);
        } catch (final BluelinkApiException e) {
            return false;
        }
    }

    @RuleAction(label = "@text/action.climate-stop.label")
    @ActionOutput(type = "boolean")
    public boolean climateStop() {
        final BluelinkVehicleHandler hnd = handler;
        try {
            return hnd != null && hnd.climateStop();
        } catch (final BluelinkApiException e) {
            return false;
        }
    }

    public static void forceRefresh(final @Nullable ThingActions actions) {
        if (actions instanceof VehicleActions va) {
            va.forceRefresh();
        } else {
            throw new IllegalArgumentException("expected VehicleActions");
        }
    }

    public static void climateStart(final @Nullable ThingActions actions, final QuantityType<Temperature> temperature,
            final boolean heating, final boolean defrost) {
        if (actions instanceof VehicleActions va) {
            va.climateStart(temperature, heating, defrost);
        } else {
            throw new IllegalArgumentException("expected VehicleActions");
        }
    }

    public static void climateStop(final @Nullable ThingActions actions) {
        if (actions instanceof VehicleActions va) {
            va.climateStop();
        } else {
            throw new IllegalArgumentException("expected VehicleActions");
        }
    }

    public static void lock(final @Nullable ThingActions actions) {
        if (actions instanceof VehicleActions va) {
            va.lock();
        } else {
            throw new IllegalArgumentException("expected VehicleActions");
        }
    }

    public static void unlock(final @Nullable ThingActions actions) {
        if (actions instanceof VehicleActions va) {
            va.unlock();
        } else {
            throw new IllegalArgumentException("expected VehicleActions");
        }
    }

    public static void startCharging(final @Nullable ThingActions actions) {
        if (actions instanceof VehicleActions va) {
            va.startCharging();
        } else {
            throw new IllegalArgumentException("expected VehicleActions");
        }
    }

    public static void stopCharging(final @Nullable ThingActions actions) {
        if (actions instanceof VehicleActions va) {
            va.stopCharging();
        } else {
            throw new IllegalArgumentException("expected VehicleActions");
        }
    }
}
