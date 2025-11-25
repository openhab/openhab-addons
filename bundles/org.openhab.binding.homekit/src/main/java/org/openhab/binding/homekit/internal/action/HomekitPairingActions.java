/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.action;

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.ACTION_RESULT_ERROR_FORMAT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.handler.HomekitBaseAccessoryHandler;
import org.openhab.core.automation.annotation.ActionInput;
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
 * Implementation of the {@link ThingActions} interface for pairing.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = HomekitPairingActions.class)
@ThingActionsScope(name = "homekit-pairing")
@NonNullByDefault
public class HomekitPairingActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(HomekitPairingActions.class);
    private @Nullable HomekitBaseAccessoryHandler handler;

    public static String pair(ThingActions actions, String code, boolean auth) {
        if (actions instanceof HomekitPairingActions accessoryActions) {
            return accessoryActions.pair(code, auth);
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of HomekitPairingActions");
        }
    }

    public static String unpair(ThingActions actions) {
        if (actions instanceof HomekitPairingActions accessoryActions) {
            return accessoryActions.unpair();
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of HomekitPairingActions");
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = handler instanceof HomekitBaseAccessoryHandler accessoryHandler ? accessoryHandler : null;
    }

    @RuleAction(label = "@text/actions.pairing-action.label", description = "@text/actions.pairing-action.description")
    public @ActionOutput(type = "java.lang.String", label = "@text/actions.pairing-result.label", description = "@text/actions.pairing-result.description") String pair(
            @ActionInput(name = "code", label = "@text/actions.pairing-code.label", description = "@text/actions.pairing-code.description") String code,
            @ActionInput(name = "auth", label = "@text/actions.pairing-auth.label", description = "@text/actions.pairing-auth.description", defaultValue = "false") boolean auth) {
        HomekitBaseAccessoryHandler handler = this.handler;
        if (handler != null) {
            return handler.pair(code, auth);
        } else {
            logger.warn("ThingHandler is null.");
            return ACTION_RESULT_ERROR_FORMAT.formatted("handler");
        }
    }

    @RuleAction(label = "@text/actions.unpairing-action.label", description = "@text/actions.unpairing-action.description")
    public @ActionOutput(type = "java.lang.String", label = "@text/actions.unpairing-result.label", description = "@text/actions.unpairing-result.description") String unpair() {
        HomekitBaseAccessoryHandler handler = this.handler;
        if (handler != null) {
            return handler.unpair();
        } else {
            logger.warn("ThingHandler is null.");
            return ACTION_RESULT_ERROR_FORMAT.formatted("handler");
        }
    }
}
