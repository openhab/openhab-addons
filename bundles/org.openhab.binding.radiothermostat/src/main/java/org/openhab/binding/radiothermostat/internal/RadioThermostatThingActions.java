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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
public class RadioThermostatThingActions implements ThingActions, IRadioThermostatThingActions {
    private final Logger logger = LoggerFactory.getLogger(RadioThermostatThingActions.class);

    private @Nullable RadioThermostatHandler handler;

    @Override
    @RuleAction(label = "sendRawCommand", description = "Action that sends raw command to the thermostat")
    public void sendRawCommand(@ActionInput(name = "sendRawCommand") @Nullable String rawCommand) {
        RadioThermostatHandler localHandler = handler;
        if (rawCommand == null) {
            logger.warn("sendRawCommand called with null command, ignoring");
            return;
        }

        if (localHandler != null) {
            localHandler.handleRawCommand(rawCommand);
            logger.debug("sendRawCommand called with raw command: {}", rawCommand);
        }
    }

    /** Static alias to support the old DSL rules engine and make the action available there. */
    public static void sendRawCommand(@Nullable ThingActions actions, @Nullable String rawCommand)
            throws IllegalArgumentException {
        invokeMethodOf(actions).sendRawCommand(rawCommand);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (RadioThermostatHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    private static IRadioThermostatThingActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(RadioThermostatThingActions.class.getName())) {
            if (actions instanceof RadioThermostatThingActions) {
                return (IRadioThermostatThingActions) actions;
            } else {
                return (IRadioThermostatThingActions) Proxy.newProxyInstance(
                        IRadioThermostatThingActions.class.getClassLoader(),
                        new Class[] { IRadioThermostatThingActions.class },
                        (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of RadioThermostatThingActions");
    }
}
