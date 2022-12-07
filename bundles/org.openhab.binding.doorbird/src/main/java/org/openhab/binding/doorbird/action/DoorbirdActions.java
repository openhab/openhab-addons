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
package org.openhab.binding.doorbird.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.doorbird.internal.DoorbirdHandler;
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
    private final static Logger logger = LoggerFactory.getLogger(DoorbirdActions.class);

    private @Nullable DoorbirdHandler handler;

    public DoorbirdActions() {
        logger.debug("Doorbird actions service created");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof DoorbirdHandler) {
            this.handler = (DoorbirdHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "Restart Doorbird", description = "restarts the Doorbird device")
    public void restart() {
        logger.debug("Doorbird action 'restart' called");
        if (handler != null) {
            handler.actionRestart();
        } else {
            logger.info("Doorbird Action service ThingHandler is null!");
        }
    }

    public static void restart(@Nullable ThingActions actions) {
        if (actions instanceof DoorbirdActions) {
            ((DoorbirdActions) actions).restart();
        } else {
            throw new IllegalArgumentException("Instance is not a DoorbirdActionsService class");
        }
    }

    @RuleAction(label = "SIP Hangup", description = "hangup SIP call")
    public void sipHangup() {
        logger.debug("Doorbird action 'sipHangup' called");
        if (handler != null) {
            handler.actionSIPHangup();
        } else {
            logger.info("Doorbird Action service ThingHandler is null!");
        }
    }

    public static void sipHangup(@Nullable ThingActions actions) {
        if (actions instanceof DoorbirdActions) {
            ((DoorbirdActions) actions).sipHangup();
        } else {
            throw new IllegalArgumentException("Instance is not a DoorbirdActionsService class");
        }
    }
}
