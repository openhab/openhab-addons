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
package org.openhab.binding.doorbird.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.doorbird.internal.handler.DoorbellHandler;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DoorbirdActions} defines rule actions for the doorbird binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = DoorbirdActions.class)
@ThingActionsScope(name = "doorbird")
@NonNullByDefault
public class DoorbirdActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(DoorbirdActions.class);

    private @Nullable DoorbellHandler handler;

    public DoorbirdActions() {
        logger.debug("DoorbirdActions service created");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof DoorbellHandler doorbellHandler) {
            this.handler = doorbellHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "restart the Doorbird", description = "Restarts the Doorbird device.")
    public void restart() {
        logger.debug("Doorbird action 'restart' called");
        DoorbellHandler handler = this.handler;
        if (handler != null) {
            handler.actionRestart();
        } else {
            logger.info("Doorbird Action service ThingHandler is null!");
        }
    }

    public static void restart(ThingActions actions) {
        ((DoorbirdActions) actions).restart();
    }

    @RuleAction(label = "hangup a SIP call", description = "Hangup SIP call.")
    public void sipHangup() {
        logger.debug("Doorbird action 'sipHangup' called");
        DoorbellHandler handler = this.handler;
        if (handler != null) {
            handler.actionSIPHangup();
        } else {
            logger.info("Doorbird Action service ThingHandler is null!");
        }
    }

    public static void sipHangup(ThingActions actions) {
        ((DoorbirdActions) actions).sipHangup();
    }

    @RuleAction(label = "get the ring time limit", description = "Get the value of RING_TIME_LIMIT.")
    public @ActionOutput(name = "result", label = "Ring Time Limit", type = "java.lang.String") String getRingTimeLimit() {
        logger.debug("Doorbird action 'getRingTimeLimit' called");
        DoorbellHandler handler = this.handler;
        if (handler != null) {
            return handler.actionGetRingTimeLimit();
        } else {
            logger.info("Doorbird Action service ThingHandler is null!");
            return "";
        }
    }

    public static String getRingTimeLimit(ThingActions actions) {
        return ((DoorbirdActions) actions).getRingTimeLimit();
    }

    @RuleAction(label = "get the call time limit", description = "Get the value of CALL_TIME_LIMIT.")
    public @ActionOutput(name = "result", label = "Call Time Limit", type = "java.lang.String") String getCallTimeLimit() {
        logger.debug("Doorbird action 'getCallTimeLimit' called");
        DoorbellHandler handler = this.handler;
        if (handler != null) {
            return handler.actionGetCallTimeLimit();
        } else {
            logger.info("Doorbird Action service ThingHandler is null!");
            return "";
        }
    }

    public static String getCallTimeLimit(ThingActions actions) {
        return ((DoorbirdActions) actions).getCallTimeLimit();
    }

    @RuleAction(label = "get the last error code", description = "Get the value of LASTERRORCODE.")
    public @ActionOutput(name = "result", label = "Last Error Code", type = "java.lang.String") String getLastErrorCode() {
        logger.debug("Doorbird action 'getLastErrorCode' called");
        DoorbellHandler handler = this.handler;
        if (handler != null) {
            return handler.actionGetLastErrorCode();
        } else {
            logger.info("Doorbird Action service ThingHandler is null!");
            return "";
        }
    }

    public static String getLastErrorCode(ThingActions actions) {
        return ((DoorbirdActions) actions).getLastErrorCode();
    }

    @RuleAction(label = "get the last error text", description = "Get the value of LASTERRORTEXT.")
    public @ActionOutput(name = "result", label = "Last Error Text", type = "java.lang.String") String getLastErrorText() {
        logger.debug("Doorbird action 'getLastErrorText' called");
        DoorbellHandler handler = this.handler;
        if (handler != null) {
            return handler.actionGetLastErrorText();
        } else {
            logger.info("Doorbird Action service ThingHandler is null!");
            return "";
        }
    }

    public static String getLastErrorText(ThingActions actions) {
        return ((DoorbirdActions) actions).getLastErrorText();
    }
}
