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
package org.openhab.binding.doorbird.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.doorbird.internal.handler.DoorbellHandler;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DoorbirdActions} defines rule actions for the doorbird binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
@ThingActionsScope(name = "doorbird")
@NonNullByDefault
public class DoorbirdActions implements ThingActions {
    private static final Logger LOGGER = LoggerFactory.getLogger(DoorbirdActions.class);

    private @Nullable DoorbellHandler handler;

    public DoorbirdActions() {
        LOGGER.debug("DoorbirdActions service created");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof DoorbellHandler) {
            this.handler = (DoorbellHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    private static IDoorbirdActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(DoorbirdActions.class.getName())) {
            if (actions instanceof IDoorbirdActions) {
                return (IDoorbirdActions) actions;
            } else {
                return (IDoorbirdActions) Proxy.newProxyInstance(IDoorbirdActions.class.getClassLoader(),
                        new Class[] { IDoorbirdActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("actions is not an instance of DoorbirdActions");
    }

    @RuleAction(label = "Restart Doorbird", description = "Restarts the Doorbird device")
    public void restart() {
        LOGGER.debug("Doorbird action 'restart' called");
        if (handler != null) {
            handler.actionRestart();
        } else {
            LOGGER.info("Doorbird Action service ThingHandler is null!");
        }
    }

    public static void restart(@Nullable ThingActions actions) {
        invokeMethodOf(actions).restart();
    }

    @RuleAction(label = "SIP Hangup", description = "Hangup SIP call")
    public void sipHangup() {
        LOGGER.debug("Doorbird action 'sipHangup' called");
        if (handler != null) {
            handler.actionSIPHangup();
        } else {
            LOGGER.info("Doorbird Action service ThingHandler is null!");
        }
    }

    public static void sipHangup(@Nullable ThingActions actions) {
        invokeMethodOf(actions).sipHangup();
    }

    @RuleAction(label = "Get Ring Time Limit", description = "Get the value of RING_TIME_LIMIT")
    public @ActionOutput(name = "getRingTimeLimit", type = "java.lang.String") String getRingTimeLimit() {
        LOGGER.debug("Doorbird action 'getRingTimeLimit' called");
        if (handler != null) {
            return handler.actionGetRingTimeLimit();
        } else {
            LOGGER.info("Doorbird Action service ThingHandler is null!");
            return "";
        }
    }

    public static String getRingTimeLimit(@Nullable ThingActions actions) {
        return invokeMethodOf(actions).getRingTimeLimit();
    }

    @RuleAction(label = "Get Call Time Limit", description = "Get the value of CALL_TIME_LIMIT")
    public @ActionOutput(name = "getCallTimeLimit", type = "java.lang.String") String getCallTimeLimit() {
        LOGGER.debug("Doorbird action 'getCallTimeLimit' called");
        if (handler != null) {
            return handler.actionGetCallTimeLimit();
        } else {
            LOGGER.info("Doorbird Action service ThingHandler is null!");
            return "";
        }
    }

    public static String getCallTimeLimit(@Nullable ThingActions actions) {
        return invokeMethodOf(actions).getCallTimeLimit();
    }

    @RuleAction(label = "Get Last Error Code", description = "Get the value of LASTERRORCODE")
    public @ActionOutput(name = "getLastErrorCode", type = "java.lang.String") String getLastErrorCode() {
        LOGGER.debug("Doorbird action 'getLastErrorCode' called");
        if (handler != null) {
            return handler.actionGetLastErrorCode();
        } else {
            LOGGER.info("Doorbird Action service ThingHandler is null!");
            return "";
        }
    }

    public static String getLastErrorCode(@Nullable ThingActions actions) {
        return invokeMethodOf(actions).getLastErrorCode();
    }

    @RuleAction(label = "Get Last Error Text", description = "Get the value of LASTERRORTEXT")
    public @ActionOutput(name = "getLastErrorText", type = "java.lang.String") String getLastErrorText() {
        LOGGER.debug("Doorbird action 'getLastErrorText' called");
        if (handler != null) {
            return handler.actionGetLastErrorText();
        } else {
            LOGGER.info("Doorbird Action service ThingHandler is null!");
            return "";
        }
    }

    public static String getLastErrorText(@Nullable ThingActions actions) {
        return invokeMethodOf(actions).getLastErrorText();
    }
}
