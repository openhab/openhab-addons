/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
public class VolvoOnCallActions implements ThingActions {

    private final static Logger logger = LoggerFactory.getLogger(VolvoOnCallActions.class);

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

    @RuleAction(label = "Volvo On Call : Close", description = "Closes the car")
    public void closeCarCommand() {
        logger.debug("closeCarCommand called");
        if (handler != null) {
            handler.actionClose();
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    public static void closeCarCommand(@Nullable ThingActions actions) {
        if (actions instanceof VolvoOnCallActions) {
            ((VolvoOnCallActions) actions).closeCarCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VolvoThingActionsService class.");
        }
    }

    @RuleAction(label = "Volvo On Call : Open", description = "Opens the car")
    public void openCarCommand() {
        logger.debug("openCarCommand called");
        if (handler != null) {
            handler.actionOpen();
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    public static void openCarCommand(@Nullable ThingActions actions) {
        if (actions instanceof VolvoOnCallActions) {
            ((VolvoOnCallActions) actions).openCarCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VolvoThingActionsService class.");
        }
    }

    @RuleAction(label = "Volvo On Call : Start Engine", description = "Starts the engine")
    public void engineStartCommand(@ActionInput(name = "runtime", label = "Runtime") @Nullable Integer runtime) {
        logger.debug("engineStartCommand called");
        if (handler != null) {
            handler.actionStart(runtime != null ? runtime : 5);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    public static void engineStartCommand(@Nullable ThingActions actions, @Nullable Integer runtime) {
        if (actions instanceof VolvoOnCallActions) {
            ((VolvoOnCallActions) actions).engineStartCommand(runtime);
        } else {
            throw new IllegalArgumentException("Instance is not an VolvoThingActionsService class.");
        }
    }

    @RuleAction(label = "Volvo On Call : Heater Start", description = "Starts car heater")
    public void heaterStartCommand() {
        logger.debug("heaterStartCommand called");
        if (handler != null) {
            handler.actionHeater(true);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    public static void heaterStartCommand(@Nullable ThingActions actions) {
        if (actions instanceof VolvoOnCallActions) {
            ((VolvoOnCallActions) actions).heaterStartCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VolvoThingActionsService class.");
        }
    }

    @RuleAction(label = "Volvo On Call : Heater Stop", description = "Stops car heater")
    public void heaterStopCommand() {
        logger.debug("heaterStopCommand called");
        if (handler != null) {
            handler.actionHeater(false);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    public static void heaterStopCommand(@Nullable ThingActions actions) {
        if (actions instanceof VolvoOnCallActions) {
            ((VolvoOnCallActions) actions).heaterStopCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VolvoThingActionsService class.");
        }
    }

    @RuleAction(label = "Volvo On Call : Honk-blink", description = "Activates the horn and or lights of the car")
    public void honkBlinkCommand(@ActionInput(name = "honk", label = "Honk") Boolean honk,
            @ActionInput(name = "blink", label = "Blink") Boolean blink) {
        logger.debug("honkBlinkCommand called");
        if (handler != null) {
            handler.actionHonkBlink(honk, blink);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    public static void honkBlinkCommand(@Nullable ThingActions actions, Boolean honk, Boolean blink) {
        if (actions instanceof VolvoOnCallActions) {
            ((VolvoOnCallActions) actions).honkBlinkCommand(honk, blink);
        } else {
            throw new IllegalArgumentException("Instance is not an VolvoThingActionsService class.");
        }
    }

}
