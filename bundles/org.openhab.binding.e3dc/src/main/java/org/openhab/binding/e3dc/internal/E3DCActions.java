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
package org.openhab.binding.e3dc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The rule actions of the {@code power-plant} thing.
 *
 * <p>
 * They read and replace the idle periods of the second version, the named time windows during which the
 * battery is locked against charging or discharging. A period is written as one line
 * {@code name:days:mode:active:hh:mm-hh:mm}, for example {@code weekend:saturday,sunday:charge:true:08:00-18:00}.
 *
 * <p>
 * {@code fetchHistory} reads the yearly history of all earlier years once and publishes it on the
 * {@code history#year-*} channels as time series, so the requests beyond the running year are made only
 * when the user asks for them.
 *
 * <p>
 * The device replaces its whole list on every write, so {@code setIdlePeriods} takes the complete list. The
 * switches {@code battery-control#block-charging} and {@code battery-control#block-discharging} manage one period of
 * their own
 * and keep all others.
 *
 * @author Bernd Weymann - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = E3DCActions.class)
@ThingActionsScope(name = "e3dc")
@NonNullByDefault
public class E3DCActions implements ThingActions {

    private @Nullable E3DCPowerPlantHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = handler instanceof E3DCPowerPlantHandler powerPlantHandler ? powerPlantHandler : null;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.getIdlePeriods.label", description = "@text/action.getIdlePeriods.description")
    public @ActionOutput(name = "periods", type = "java.lang.String") @Nullable String getIdlePeriods() {
        E3DCPowerPlantHandler current = handler;
        return current != null ? current.getIdlePeriods() : null;
    }

    @RuleAction(label = "@text/action.setIdlePeriods.label", description = "@text/action.setIdlePeriods.description")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") boolean setIdlePeriods(
            @ActionInput(name = "periods", label = "@text/action.setIdlePeriods.periods.label", //
                    description = "@text/action.setIdlePeriods.periods.description") String periods) {
        E3DCPowerPlantHandler current = handler;
        return current != null && current.setIdlePeriods(periods);
    }

    @RuleAction(label = "@text/action.fetchHistory.label", description = "@text/action.fetchHistory.description")
    public @ActionOutput(name = "years", type = "java.lang.Integer") int fetchHistory() {
        E3DCPowerPlantHandler current = handler;
        return current != null ? current.fetchHistory() : -1;
    }

    /** Fetches the yearly history of all earlier years once and returns the number of years that had data. */
    public static int fetchHistory(ThingActions actions) {
        return ((E3DCActions) actions).fetchHistory();
    }

    /** Reads the idle periods, one per line, or {@code null} when the device could not be asked. */
    public static @Nullable String getIdlePeriods(ThingActions actions) {
        return ((E3DCActions) actions).getIdlePeriods();
    }

    /** Replaces the whole list of idle periods and reports whether the device accepted it. */
    public static boolean setIdlePeriods(ThingActions actions, String periods) {
        return ((E3DCActions) actions).setIdlePeriods(periods);
    }
}
