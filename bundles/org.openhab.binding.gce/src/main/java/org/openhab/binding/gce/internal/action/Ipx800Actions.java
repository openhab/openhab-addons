/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gce.internal.handler.Ipx800v3Handler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines rule actions for the GCE binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@ThingActionsScope(name = "gce")
@NonNullByDefault
public class Ipx800Actions implements ThingActions {
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
        return handler;
    }

    @RuleAction(label = "reset a counter", description = "Resets to 0 value of a given counter.")
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

    @RuleAction(label = "reset the PLC", description = "Restarts the IPX800.")
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

    public static void resetCounter(ThingActions actions, Integer counter) {
        ((Ipx800Actions) actions).resetCounter(counter);
    }

    public static void reset(ThingActions actions, @Nullable Integer placeholder) {
        ((Ipx800Actions) actions).reset(placeholder);
    }
}
