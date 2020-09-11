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
package org.openhab.binding.gce.internal.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.gce.internal.handler.Ipx800v3Handler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {Ipx800Actions } defines rule actions for the GCE binding.
 * <p>
 * <b>Note:</b>The static method <b>invokeMethodOf</b> handles the case where
 * the test <i>actions instanceof Ipx800Actions</i> fails. This test can fail
 * due to an issue in openHAB core v2.5.0 where the {@link Ipx800Actions} class
 * can be loaded by a different classloader than the <i>actions</i> instance.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@ThingActionsScope(name = "gce")
@NonNullByDefault
public class Ipx800Actions implements ThingActions, IIpx800Actions {
    private final Logger logger = LoggerFactory.getLogger(Ipx800Actions.class);

    protected @Nullable Ipx800v3Handler handler;

    public Ipx800Actions() {
        logger.debug("IPX800 actions service instanciated");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof Ipx800v3Handler) {
            this.handler = (Ipx800v3Handler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @Override
    @RuleAction(label = "GCE : Reset counter", description = "Resets to 0 value of a given counter")
    public void resetCounter(
            @ActionInput(name = "counter", label = "Counter", required = true, description = "Id of the counter", type = "java.lang.Integer") Integer counter) {
        logger.debug("IPX800 action 'resetCounter' called");
        Ipx800v3Handler theHandler = this.handler;
        if (theHandler != null) {
            theHandler.resetCounter(counter);
        } else {
            logger.warn("Method call resetCounter failed because IPX800 action service ThingHandler is null!");
        }
    }

    @Override
    @RuleAction(label = "GCE : Reset PLC", description = "Restarts the IPX800")
    public void reset(
            @ActionInput(name = "placeholder", label = "Placeholder", required = false, description = "This parameter is not used", type = "java.lang.Integer") @Nullable Integer placeholder) {
        logger.debug("IPX800 action 'reset' called");
        Ipx800v3Handler theHandler = this.handler;
        if (theHandler != null) {
            theHandler.reset();
        } else {
            logger.warn("Method call reset failed because IPX800 action service ThingHandler is null!");
        }
    }

    public static void resetCounter(@Nullable ThingActions actions, Integer counter) {
        invokeMethodOf(actions).resetCounter(counter);
    }

    public static void reset(@Nullable ThingActions actions, @Nullable Integer placeholder) {
        invokeMethodOf(actions).reset(placeholder);
    }

    private static IIpx800Actions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(Ipx800Actions.class.getName())) {
            if (actions instanceof IIpx800Actions) {
                return (IIpx800Actions) actions;
            } else {
                return (IIpx800Actions) Proxy.newProxyInstance(IIpx800Actions.class.getClassLoader(),
                        new Class[] { IIpx800Actions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of Ipx800Actions");
    }
}
