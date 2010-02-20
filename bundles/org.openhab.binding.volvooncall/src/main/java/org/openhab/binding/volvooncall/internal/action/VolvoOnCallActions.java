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
package org.openhab.binding.volvooncall.internal.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.volvooncall.internal.handler.VehicleHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@VehicleAction } class is responsible to call corresponding
 * action on Vehicle Handler
 *
 * @author Gaël L'hopital - Initial contribution
 */
@ThingActionsScope(name = "volvooncall")
@NonNullByDefault
public class VolvoOnCallActions implements ThingActions, IVolvoOnCallActions {

    private final Logger logger = LoggerFactory.getLogger(VolvoOnCallActions.class);

    private @Nullable VehicleHandler handler;

    public VolvoOnCallActions() {
        logger.info("Volvo On Call actions service instanciated");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof VehicleHandler) {
            this.handler = (VehicleHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @Override
    @RuleAction(label = "Volvo On Call : Close", description = "Closes the car")
    public void closeCarCommand() {
        logger.debug("closeCarCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionClose();
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    public static void closeCarCommand(@Nullable ThingActions actions) {
        invokeMethodOf(actions).closeCarCommand();
    }

    @Override
    @RuleAction(label = "Volvo On Call : Open", description = "Opens the car")
    public void openCarCommand() {
        logger.debug("openCarCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionOpen();
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    public static void openCarCommand(@Nullable ThingActions actions) {
        invokeMethodOf(actions).openCarCommand();
    }

    @Override
    @RuleAction(label = "Volvo On Call : Start Engine", description = "Starts the engine")
    public void engineStartCommand(@ActionInput(name = "runtime", label = "Runtime") @Nullable Integer runtime) {
        logger.debug("engineStartCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionStart(runtime != null ? runtime : 5);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    public static void engineStartCommand(@Nullable ThingActions actions, @Nullable Integer runtime) {
        invokeMethodOf(actions).engineStartCommand(runtime);
    }

    @Override
    @RuleAction(label = "Volvo On Call : Heater Start", description = "Starts car heater")
    public void heaterStartCommand() {
        logger.debug("heaterStartCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionHeater(true);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    public static void heaterStartCommand(@Nullable ThingActions actions) {
        invokeMethodOf(actions).heaterStartCommand();
    }

    @Override
    @RuleAction(label = "Volvo On Call : Preclimatization Start", description = "Starts car heater")
    public void preclimatizationStartCommand() {
        logger.debug("preclimatizationStartCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionPreclimatization(true);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    public static void preclimatizationStartCommand(@Nullable ThingActions actions) {
        invokeMethodOf(actions).preclimatizationStartCommand();
    }

    @Override
    @RuleAction(label = "Volvo On Call : Heater Stop", description = "Stops car heater")
    public void heaterStopCommand() {
        logger.debug("heaterStopCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionHeater(false);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    public static void heaterStopCommand(@Nullable ThingActions actions) {
        invokeMethodOf(actions).heaterStopCommand();
    }

    @Override
    @RuleAction(label = "Volvo On Call : Preclimatization Stop", description = "Stops car heater")
    public void preclimatizationStopCommand() {
        logger.debug("preclimatizationStopCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionPreclimatization(false);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    public static void preclimatizationStopCommand(@Nullable ThingActions actions) {
        invokeMethodOf(actions).preclimatizationStopCommand();
    }

    @Override
    @RuleAction(label = "Volvo On Call : Honk-blink", description = "Activates the horn and or lights of the car")
    public void honkBlinkCommand(@ActionInput(name = "honk", label = "Honk") Boolean honk,
            @ActionInput(name = "blink", label = "Blink") Boolean blink) {
        logger.debug("honkBlinkCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionHonkBlink(honk, blink);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    public static void honkBlinkCommand(@Nullable ThingActions actions, Boolean honk, Boolean blink) {
        invokeMethodOf(actions).honkBlinkCommand(honk, blink);
    }

    private static IVolvoOnCallActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(VolvoOnCallActions.class.getName())) {
            if (actions instanceof IVolvoOnCallActions) {
                return (IVolvoOnCallActions) actions;
            } else {
                return (IVolvoOnCallActions) Proxy.newProxyInstance(IVolvoOnCallActions.class.getClassLoader(),
                        new Class[] { IVolvoOnCallActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of VolvoOnCallActions");
    }
}
