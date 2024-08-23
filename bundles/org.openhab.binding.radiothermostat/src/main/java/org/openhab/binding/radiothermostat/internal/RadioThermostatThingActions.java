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
package org.openhab.binding.radiothermostat.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.radiothermostat.internal.handler.RadioThermostatHandler;
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
 * Some automation actions to be used with a {@link RadioThermostatThingActions}
 *
 * @author Michael Lobstein - initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = RadioThermostatThingActions.class)
@ThingActionsScope(name = "radiothermostat")
@NonNullByDefault
public class RadioThermostatThingActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(RadioThermostatThingActions.class);

    private @Nullable RadioThermostatHandler handler;

    @RuleAction(label = "send a raw command", description = "Send a raw command to the thermostat's 'tstat' endpoint.")
    public void sendRawCommand(@ActionInput(name = "sendRawCommand") @Nullable String rawCommand) {
        if (rawCommand == null) {
            logger.warn("sendRawCommand called with null command, ignoring");
            return;
        }

        RadioThermostatHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.handleRawCommand(rawCommand);
            logger.debug("sendRawCommand called with raw command: {}", rawCommand);
        }
    }

    @RuleAction(label = "send a raw command", description = "Send a raw command to a specific endpoint on the thermostat.")
    public void sendRawCommand(@ActionInput(name = "sendRawCommand") @Nullable String rawCommand,
            @Nullable String resource) {
        if (rawCommand == null || resource == null) {
            logger.warn("sendRawCommand called with null command, ignoring");
            return;
        }

        RadioThermostatHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.handleRawCommand(rawCommand, resource);
            logger.debug("sendRawCommand called with raw command: {}, resource: {}", rawCommand, resource);
        }
    }

    /** Static aliases to support the old DSL rules engine and make the action available there. */
    public static void sendRawCommand(ThingActions actions, @Nullable String rawCommand) {
        ((RadioThermostatThingActions) actions).sendRawCommand(rawCommand);
    }

    public static void sendRawCommand(ThingActions actions, @Nullable String rawCommand, @Nullable String resource) {
        ((RadioThermostatThingActions) actions).sendRawCommand(rawCommand, resource);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (RadioThermostatHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
