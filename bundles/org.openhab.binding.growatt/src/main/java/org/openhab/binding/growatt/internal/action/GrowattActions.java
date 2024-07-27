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
package org.openhab.binding.growatt.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.growatt.internal.handler.GrowattInverterHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link ThingActions} interface used for setting up battery charging and discharging programs.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = GrowattActions.class)
@ThingActionsScope(name = "growatt")
@NonNullByDefault
public class GrowattActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(GrowattActions.class);
    private @Nullable GrowattInverterHandler handler;

    public static void setupBatteryProgram(ThingActions actions, Integer programMode, @Nullable Integer powerLevel,
            @Nullable Integer stopSOC, @Nullable Boolean enableAcCharging, @Nullable String startTime,
            @Nullable String stopTime, @Nullable Boolean enableProgram) {
        if (actions instanceof GrowattActions growattActions) {
            growattActions.setupBatteryProgram(programMode, powerLevel, stopSOC, enableAcCharging, startTime, stopTime,
                    enableProgram);
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of GrowattActions");
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (handler instanceof GrowattInverterHandler growattHandler) ? growattHandler : null;
    }

    @RuleAction(label = "@text/actions.battery-program.label", description = "@text/actions.battery-program.description")
    public void setupBatteryProgram(
            @ActionInput(name = "program-mode", label = "@text/actions.program-mode.label", description = "@text/actions.program-mode.description") Integer programMode,
            @ActionInput(name = "power-level", label = "@text/actions.power-level.label", description = "@text/actions.power-level.description") @Nullable Integer powerLevel,
            @ActionInput(name = "stop-soc", label = "@text/actions.stop-soc.label", description = "@text/actions.stop-soc.description") @Nullable Integer stopSOC,
            @ActionInput(name = "enable-ac-charging", label = "@text/actions.enable-ac-charging.label", description = "@text/actions.enable-ac-charging.description") @Nullable Boolean enableAcCharging,
            @ActionInput(name = "start-time", label = "@text/actions.start-time.label", description = "@text/actions.start-time.description") @Nullable String startTime,
            @ActionInput(name = "stop-time", label = "@text/actions.stop-time.label", description = "@text/actions.stop-time.description") @Nullable String stopTime,
            @ActionInput(name = "enable-program", label = "@text/actions.enable-program.label", description = "@text/actions.enable-program.description") @Nullable Boolean enableProgram) {
        GrowattInverterHandler handler = this.handler;
        if (handler != null) {
            handler.setupBatteryProgram(programMode, powerLevel, stopSOC, enableAcCharging, startTime, stopTime,
                    enableProgram);
        } else {
            logger.warn("ThingHandler is null.");
        }
    }
}
