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
package org.openhab.binding.vwweconnect.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vwweconnect.internal.handler.VehicleHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@VehicleAction } class is responsible to call corresponding
 * action on Vehicle Handler
 *
 * @author Jan Gustafsson - Initial contribution
 */
@ThingActionsScope(name = "vwweconnect")
@NonNullByDefault
public class VWWeConnectActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(VWWeConnectActions.class);

    private @Nullable VehicleHandler handler;

    public VWWeConnectActions() {
        logger.debug("VW We Connect actions service instanciated");
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

    @RuleAction(label = "VW We Connect : Heater Start", description = "Starts vehicle heater")
    public void heaterStartCommand() {
        logger.debug("heaterStartCommand called");
        if (handler != null) {
            handler.actionHeater(true);
        } else {
            logger.warn("VWWeConnect Action service ThingHandler is null!");
        }
    }

    public static void heaterStartCommand(@Nullable ThingActions actions) {
        if (actions instanceof VWWeConnectActions) {
            ((VWWeConnectActions) actions).heaterStartCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VWWeConnectActionsService class.");
        }
    }

    @RuleAction(label = "VW We Connect : Lock", description = "Locks vehicle doors")
    public void lockCommand() {
        logger.debug("lockCommand called");
        if (handler != null) {
            handler.actionLock();
        } else {
            logger.warn("VWWeConnect Action service ThingHandler is null!");
        }
    }

    public static void lockCommand(@Nullable ThingActions actions) {
        if (actions instanceof VWWeConnectActions) {
            ((VWWeConnectActions) actions).lockCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VWWeConnectActionsService class.");
        }
    }

    @RuleAction(label = "VW We Connect : Ventilation Start", description = "Starts vehicle ventilation")
    public void ventilationStartCommand() {
        logger.debug("ventilationStartCommand called");
        if (handler != null) {
            handler.actionVentilation(true);
        } else {
            logger.warn("VWWeConnect Action service ThingHandler is null!");
        }
    }

    public static void ventilationStartCommand(@Nullable ThingActions actions) {
        if (actions instanceof VWWeConnectActions) {
            ((VWWeConnectActions) actions).ventilationStartCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VWWeConnectActionsService class.");
        }
    }

    @RuleAction(label = "VW We Connect : Charger Start", description = "Starts vehicle charger")
    public void chargerStartCommand() {
        logger.debug("chargerStartCommand called");
        if (handler != null) {
            handler.actionCharge(true);
        } else {
            logger.warn("VWWeConnect Action service ThingHandler is null!");
        }
    }

    public static void chargerStartCommand(@Nullable ThingActions actions) {
        if (actions instanceof VWWeConnectActions) {
            ((VWWeConnectActions) actions).chargerStartCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VWWeConnectActionsService class.");
        }
    }

    @RuleAction(label = "VW We Connect : Climatisation Start", description = "Starts vehicle climatisation")
    public void climateStartCommand() {
        logger.debug("climateStartCommand called");
        if (handler != null) {
            handler.actionClimate(true);
        } else {
            logger.warn("VWWeConnect Action service ThingHandler is null!");
        }
    }

    public static void climateStartCommand(@Nullable ThingActions actions) {
        if (actions instanceof VWWeConnectActions) {
            ((VWWeConnectActions) actions).climateStartCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VWWeConnectActionsService class.");
        }
    }

    @RuleAction(label = "VW We Connect : Window heating Start", description = "Starts vehicle window heating")
    public void windowHeatStartCommand() {
        logger.debug("windowHeatStartCommand called");
        if (handler != null) {
            handler.actionWindowHeat(true);
        } else {
            logger.warn("VWWeConnect Action service ThingHandler is null!");
        }
    }

    public static void windowHeatStartCommand(@Nullable ThingActions actions) {
        if (actions instanceof VWWeConnectActions) {
            ((VWWeConnectActions) actions).windowHeatStartCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VWWeConnectActionsService class.");
        }
    }

    @RuleAction(label = "VW We Connect : Heater Stop", description = "Stops vehicle heater")
    public void heaterStopCommand() {
        logger.debug("heaterStopCommand called");
        if (handler != null) {
            handler.actionHeater(false);
        } else {
            logger.warn("VWWeConnect Action service ThingHandler is null!");
        }
    }

    public static void heaterStopCommand(@Nullable ThingActions actions) {
        if (actions instanceof VWWeConnectActions) {
            ((VWWeConnectActions) actions).heaterStopCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VWWeConnectActionsService class.");
        }
    }

    @RuleAction(label = "VW We Connect : Unlock", description = "Unlocks vehicle doors")
    public void unlockCommand() {
        logger.debug("unlockCommand called");
        if (handler != null) {
            handler.actionUnlock();
        } else {
            logger.warn("VWWeConnect Action service ThingHandler is null!");
        }
    }

    public static void unlockCommand(@Nullable ThingActions actions) {
        if (actions instanceof VWWeConnectActions) {
            ((VWWeConnectActions) actions).unlockCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VWWeConnectActionsService class.");
        }
    }

    @RuleAction(label = "VW We Connect : Ventilation Stop", description = "Stops car ventilation")
    public void ventilationStopCommand() {
        logger.debug("ventilationStopCommand called");
        if (handler != null) {
            handler.actionVentilation(false);
        } else {
            logger.warn("VWWeConnect Action service ThingHandler is null!");
        }
    }

    public static void ventilationStopCommand(@Nullable ThingActions actions) {
        if (actions instanceof VWWeConnectActions) {
            ((VWWeConnectActions) actions).ventilationStopCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VWWeConnectActionsService class.");
        }
    }

    @RuleAction(label = "VW We Connect : Charger Stop", description = "Stops vehicle charger")
    public void chargerStopCommand() {
        logger.debug("chargerStopCommand called");
        if (handler != null) {
            handler.actionCharge(false);
        } else {
            logger.warn("VWWeConnect Action service ThingHandler is null!");
        }
    }

    public static void chargerStopCommand(@Nullable ThingActions actions) {
        if (actions instanceof VWWeConnectActions) {
            ((VWWeConnectActions) actions).chargerStopCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VWWeConnectActionsService class.");
        }
    }

    @RuleAction(label = "VW We Connect : Climatisation Stop", description = "Stops vehicle climatisation")
    public void climateStopCommand() {
        logger.debug("climateStopCommand called");
        if (handler != null) {
            handler.actionClimate(false);
        } else {
            logger.warn("VWWeConnect Action service ThingHandler is null!");
        }
    }

    public static void climateStopCommand(@Nullable ThingActions actions) {
        if (actions instanceof VWWeConnectActions) {
            ((VWWeConnectActions) actions).climateStopCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VWWeConnectActionsService class.");
        }
    }

    @RuleAction(label = "VW We Connect : Window heating Stop", description = "Stops vehicle window heating")
    public void windowHeatStopCommand() {
        logger.debug("windowHeatStopCommand called");
        if (handler != null) {
            handler.actionWindowHeat(false);
        } else {
            logger.warn("VWWeConnect Action service ThingHandler is null!");
        }
    }

    public static void windowHeatStopCommand(@Nullable ThingActions actions) {
        if (actions instanceof VWWeConnectActions) {
            ((VWWeConnectActions) actions).windowHeatStopCommand();
        } else {
            throw new IllegalArgumentException("Instance is not an VWWeConnectActionsService class.");
        }
    }

    @RuleAction(label = "VW We Connect : Honk-blink", description = "Activates the horn and or lights of the car")
    public void honkBlinkCommand(@ActionInput(name = "honk", label = "Honk") Boolean honk,
            @ActionInput(name = "blink", label = "Blink") Boolean blink) {
        logger.debug("honkBlinkCommand called");
        if (handler != null) {
            handler.actionHonkBlink(honk, blink);
        } else {
            logger.warn("VWCarNet Action service ThingHandler is null!");
        }
    }

    public static void honkBlinkCommand(@Nullable ThingActions actions, Boolean honk, Boolean blink) {
        if (actions instanceof VWWeConnectActions) {
            ((VWWeConnectActions) actions).honkBlinkCommand(honk, blink);
        } else {
            throw new IllegalArgumentException("Instance is not an VWWeConnectActionsService class.");
        }
    }
}
