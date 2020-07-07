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
package org.openhab.binding.kaleidescape.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.kaleidescape.internal.handler.KaleidescapeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some automation actions to be used with a {@link KaleidescapeThingActions}
 *
 * @author Michael Lobstein - initial contribution
 *
 */
@ThingActionsScope(name = "kaleidescape")
@NonNullByDefault
public class KaleidescapeThingActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(KaleidescapeThingActions.class);

    private @Nullable KaleidescapeHandler handler;

    @RuleAction(label = "sendKCommand", description = "Action that sends raw command to the kaleidescape zone")
    public void sendKCommand(@ActionInput(name = "sendKCommand") String kCommand) {
        KaleidescapeHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.handleRawCommand(kCommand);
            logger.debug("sendKCommand called with command: {}", kCommand);
        } else {
            logger.debug("sendKCommand called with null command, ignoring");
        }
    }

    public static void sendKCommand(@Nullable ThingActions actions, String kCommand) throws IllegalArgumentException {
        if (actions instanceof KaleidescapeThingActions) {
            ((KaleidescapeThingActions) actions).sendKCommand(kCommand);
        } else {
            throw new IllegalArgumentException("Instance is not an KaleidescapeThingActions class.");
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (KaleidescapeHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }
}
