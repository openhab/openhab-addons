/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.kaleidescape.internal.handler.KaleidescapeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some automation actions to be used with a {@link KaleidescapeThingActions}
 *
 * @author Michael Lobstein - initial contribution
 */
@ThingActionsScope(name = "kaleidescape")
@NonNullByDefault
public class KaleidescapeThingActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(KaleidescapeThingActions.class);

    private @Nullable KaleidescapeHandler handler;

    @RuleAction(label = "send a raw command", description = "Action that sends raw command to the kaleidescape zone.")
    public void sendKCommand(@ActionInput(name = "sendKCommand") String kCommand) {
        KaleidescapeHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.handleRawCommand(kCommand);
            logger.debug("sendKCommand called with command: {}", kCommand);
        } else {
            logger.warn("unable to send command, KaleidescapeHandler was null");
        }
    }

    /** Static alias to support the old DSL rules engine and make the action available there. */
    public static void sendKCommand(ThingActions actions, String kCommand) throws IllegalArgumentException {
        ((KaleidescapeThingActions) actions).sendKCommand(kCommand);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (KaleidescapeHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
