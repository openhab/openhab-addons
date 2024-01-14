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
package org.openhab.binding.volvooncall.internal.action;

import static org.openhab.binding.volvooncall.internal.VolvoOnCallBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.volvooncall.internal.handler.VehicleHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VolvoOnCallActions} class is responsible to call corresponding
 * action on Vehicle Handler
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = VolvoOnCallActions.class)
@ThingActionsScope(name = "volvooncall")
@NonNullByDefault
public class VolvoOnCallActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(VolvoOnCallActions.class);

    private @Nullable VehicleHandler handler;

    public VolvoOnCallActions() {
        logger.debug("Volvo On Call actions service instantiated");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof VehicleHandler vehicleHandler) {
            this.handler = vehicleHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "close the car", description = "Closes the car")
    public void closeCarCommand() {
        logger.debug("closeCarCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionOpenClose(LOCK, OnOffType.ON);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    @RuleAction(label = "open the car", description = "Opens the car")
    public void openCarCommand() {
        logger.debug("openCarCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionOpenClose(UNLOCK, OnOffType.OFF);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    @RuleAction(label = "start the engine", description = "Starts the engine")
    public void engineStartCommand(@ActionInput(name = "runtime", label = "Runtime") @Nullable Integer runtime) {
        logger.debug("engineStartCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionStart(runtime != null ? runtime : 5);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    @RuleAction(label = "start the heater", description = "Starts car heater")
    public void heaterStartCommand() {
        logger.debug("heaterStartCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionHeater(REMOTE_HEATER, true);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    @RuleAction(label = "start preclimatization", description = "Starts the car heater")
    public void preclimatizationStartCommand() {
        logger.debug("preclimatizationStartCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionHeater(PRECLIMATIZATION, true);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    @RuleAction(label = "stop the heater", description = "Stops car heater")
    public void heaterStopCommand() {
        logger.debug("heaterStopCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionHeater(REMOTE_HEATER, false);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    @RuleAction(label = "stop preclimatization", description = "Stops the car heater")
    public void preclimatizationStopCommand() {
        logger.debug("preclimatizationStopCommand called");
        VehicleHandler handler = this.handler;
        if (handler != null) {
            handler.actionHeater(PRECLIMATIZATION, false);
        } else {
            logger.warn("VolvoOnCall Action service ThingHandler is null!");
        }
    }

    @RuleAction(label = "honk-blink", description = "Activates the horn and or lights of the car")
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
}
