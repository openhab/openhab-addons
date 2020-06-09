/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.radiothermostat.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.radiothermostat.internal.handler.RadioThermostatHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some automation actions to be used with a {@link RadioThermostatThingActions}
 *
 * @author Michael Lobstein - initial contribution
 *
 */
@ThingActionsScope(name = "radiothermostat")
@NonNullByDefault
public class RadioThermostatThingActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(RadioThermostatThingActions.class);

    private @Nullable RadioThermostatHandler handler;

    @SuppressWarnings("null")
    @RuleAction(label = "sendRawCommand", description = "Action that sends raw command to the thermostat")
    public void sendRawCommand(@ActionInput(name = "sendRawCommand") @Nullable String rawCommand) {
        if (handler != null && rawCommand != null) {
            handler.handleRawCommand(rawCommand);
            logger.debug("sendRawCommand called with raw command: {}", rawCommand);
        } else {
            logger.debug("sendRawCommand called with null command, ignoring");
        }
    }

    public static void sendRawCommand(@Nullable ThingActions actions, @Nullable String rawCommand)
            throws IllegalArgumentException {
        if (actions instanceof RadioThermostatThingActions) {
            ((RadioThermostatThingActions) actions).sendRawCommand(rawCommand);
        } else {
            throw new IllegalArgumentException("Instance is not an RadioThermostatThingActions class.");
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (RadioThermostatHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }
}
