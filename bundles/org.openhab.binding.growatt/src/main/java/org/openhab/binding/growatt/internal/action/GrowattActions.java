/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link ThingActions} interface used for setting up battery charging and discharging programs.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@ThingActionsScope(name = "growatt")
@NonNullByDefault
public class GrowattActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(GrowattActions.class);
    private @Nullable GrowattInverterHandler handler;

    public static void setupChargingProgram(ThingActions actions, Number chargingPower, Number targetSOC,
            boolean allowAcCharging, String startTime, String stopTime, boolean programEnable) {
        if (actions instanceof GrowattActions growattActions) {
            growattActions.setupChargingProgram(chargingPower, targetSOC, allowAcCharging, startTime, stopTime,
                    programEnable);
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of GrowattActions");
        }
    }

    public static void setupDischargingProgram(ThingActions actions, Number dischargingPower, Number targetSOC,
            String startTime, String stopTime, boolean programEnable) {
        if (actions instanceof GrowattActions growattActions) {
            growattActions.setupDischargingProgram(dischargingPower, targetSOC, startTime, stopTime, programEnable);
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

    @RuleAction(label = "@text/actions.charging.label", description = "@text/actions.charging.description")
    public void setupChargingProgram(
            @ActionInput(name = "charging-power", label = "@text/actions.charging-power.label", description = "@text/actions.charging-power.description") Number chargingPower,
            @ActionInput(name = "target-soc", label = "@text/actions.target-soc.label", description = "@text/actions.target-soc.description") Number targetSOC,
            @ActionInput(name = "allow-ac-charging", label = "@text/actions.allow-ac-charging.label", description = "@text/actions.allow-ac-charging.description") boolean allowAcCharging,
            @ActionInput(name = "start-time", label = "@text/actions.start-time.label", description = "@text/actions.start-time.description") String startTime,
            @ActionInput(name = "stop-time", label = "@text/actions.stop-time.label", description = "@text/actions.stop-time.description") String stopTime,
            @ActionInput(name = "program-enable", label = "@text/actions.program-enable.label", description = "@text/actions.program-enable.description") boolean programEnable) {
        GrowattInverterHandler handler = this.handler;
        if (handler != null) {
            handler.setupChargingProgram(chargingPower, targetSOC, allowAcCharging, startTime, stopTime, programEnable);
        } else {
            logger.warn("ThingHandler is null.");
        }
    }

    @RuleAction(label = "@text/actions.discharging.label", description = "@text/actions.discharging.description")
    public void setupDischargingProgram(
            @ActionInput(name = "charging-power", label = "@text/actions.charging-power.label", description = "@text/actions.charging-power.description") Number dischargingPower,
            @ActionInput(name = "target-soc", label = "@text/actions.target-soc.label", description = "@text/actions.target-soc.description") Number targetSOC,
            @ActionInput(name = "start-time", label = "@text/actions.start-time.label", description = "@text/actions.start-time.description") String startTime,
            @ActionInput(name = "stop-time", label = "@text/actions.stop-time.label", description = "@text/actions.stop-time.description") String stopTime,
            @ActionInput(name = "program-enable", label = "@text/actions.program-enable.label", description = "@text/actions.program-enable.description") boolean programEnable) {
        GrowattInverterHandler handler = this.handler;
        if (handler != null) {
            handler.setupDischargingProgram(dischargingPower, targetSOC, startTime, stopTime, programEnable);
        } else {
            logger.warn("ThingHandler is null.");
        }
    }
}
