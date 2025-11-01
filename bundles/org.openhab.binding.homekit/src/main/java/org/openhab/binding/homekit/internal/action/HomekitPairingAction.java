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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.handler.HomekitBaseAccessoryHandler;
import org.openhab.core.automation.annotation.ActionInput;
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
@Component(scope = ServiceScope.PROTOTYPE, service = HomekitPairingAction.class)
@ThingActionsScope(name = "homekit-pairing")
@NonNullByDefault
public class HomekitPairingAction implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(HomekitPairingAction.class);
    private @Nullable HomekitBaseAccessoryHandler handler;

    public static void pair(ThingActions actions, String code) {
        if (actions instanceof HomekitPairingAction accessoryActions) {
            accessoryActions.pair(code);
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of HomekitAccessoryActions");
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

    @RuleAction(label = "@text/actions.pairing-code.label", description = "@text/actions.pairing-code.description")
    public void pair(
            @ActionInput(name = "code", label = "@text/actions.pairing-code.label", description = "@text/actions.pairing-code.description") String code) {
        HomekitBaseAccessoryHandler handler = this.handler;
        if (handler != null) {
            handler.pair(code);
        } else {
            logger.warn("ThingHandler is null.");
        }
    }
}
