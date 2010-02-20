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
package org.openhab.binding.alarmdecoder.internal.actions;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.alarmdecoder.internal.handler.ADBridgeHandler;
import org.openhab.binding.alarmdecoder.internal.protocol.ADCommand;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BridgeActions} class defines thing actions for alarmdecoder bridges.
 *
 * @author Bob Adair - Initial contribution
 */
@ThingActionsScope(name = "alarmdecoder")
@NonNullByDefault
public class BridgeActions implements ThingActions, IBridgeActions {

    private final Logger logger = LoggerFactory.getLogger(BridgeActions.class);

    private @Nullable ADBridgeHandler bridge;

    public BridgeActions() {
        logger.trace("Alarm Decoder bridge actions service created");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ADBridgeHandler) {
            this.bridge = (ADBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridge;
    }

    /**
     * Reboot thing action
     */
    @Override
    @RuleAction(label = "Reboot", description = "Reboot the Alarm Decoder device")
    public void reboot() {
        ADBridgeHandler bridge = this.bridge;
        if (bridge != null) {
            bridge.sendADCommand(ADCommand.reboot());
            logger.debug("Sending reboot command.");
        } else {
            logger.debug("Request for reboot action, but bridge is undefined.");
        }
    }

    // Static method for Rules DSL backward compatibility
    public static void reboot(@Nullable ThingActions actions) {
        // if (actions instanceof BridgeActions) {
        // ((BridgeActions) actions).reboot();
        // } else {
        // throw new IllegalArgumentException("Instance is not a BridgeActions class.");
        // }
        invokeMethodOf(actions).reboot(); // Remove and uncomment above when core issue #1536 is fixed
    }

    /**
     * This is only necessary to work around a bug in openhab-core (issue #1536). It should be removed once that is
     * resolved.
     */
    private static IBridgeActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(BridgeActions.class.getName())) {
            if (actions instanceof IBridgeActions) {
                return (IBridgeActions) actions;
            } else {
                return (IBridgeActions) Proxy.newProxyInstance(IBridgeActions.class.getClassLoader(),
                        new Class[] { IBridgeActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of BridgeActions");
    }
}
