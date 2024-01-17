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
package org.openhab.binding.openwebnet.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.handler.OpenWebNetScenarioHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.communication.Response;
import org.openwebnet4j.message.CEN;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetCENActions} defines CEN/CEN+ actions for the openwebnet binding.
 *
 * @author Massimo Valla - Initial contribution
 */

@Component(scope = ServiceScope.PROTOTYPE, service = OpenWebNetCENActions.class)
@ThingActionsScope(name = "openwebnet")
@NonNullByDefault
public class OpenWebNetCENActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetCENActions.class);
    private @Nullable OpenWebNetScenarioHandler scenarioHandler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.scenarioHandler = (OpenWebNetScenarioHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return scenarioHandler;
    }

    @RuleAction(label = "virtualPress", description = "Virtual press of the push button")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean virtualPress(
            @ActionInput(name = "press", label = "press", description = "Type of press") @Nullable String press,
            @ActionInput(name = "button", label = "button", description = "Button number") int button) {
        OpenWebNetScenarioHandler handler = scenarioHandler;
        if (handler == null) {
            logger.warn("openwebnet OpenWebNetCENActions: scenarioHandler is null!");
            return false;
        }
        if (press == null) {
            logger.warn("openwebnet OpenWebNetCENActions: press parameter is null!");
            return false;
        }
        CEN msg = null;
        try {
            msg = handler.pressStrToMessage(press, button);
            Response res = handler.send(msg);
            if (res != null) {
                logger.debug("Sent virtualPress '{}' to gateway. Response: {}", msg, res.getResponseMessages());
                return res.isSuccess();
            } else {
                logger.debug("virtual press action returned null response");
            }
        } catch (IllegalArgumentException e) {
            logger.warn("cannot execute virtual press action for thing {}: {}", handler.getThing().getUID(),
                    e.getMessage());
        } catch (OWNException e) {
            logger.warn("exception while sending virtual press message '{}' to gateway: {}", msg, e.getMessage());
        }
        return false;
    }

    // legacy delegate methods
    public static void virtualPress(@Nullable ThingActions actions, @Nullable String press, int button) {
        if (actions instanceof OpenWebNetCENActions openWebNetCENActions) {
            openWebNetCENActions.virtualPress(press, button);
        } else {
            throw new IllegalArgumentException("Instance is not an OpenWebNetCENActions class.");
        }
    }
}
