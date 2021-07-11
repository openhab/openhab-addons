/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.threema.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * @author Kai K. - Initial contribution
 */
@ThingActionsScope(name = "threema")
@NonNullByDefault
public class ThreemaActions implements ThingActions {

    private ThreemaHandler handler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (ThreemaHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "send a message (basic mode)", description = "Send a message using the Threema.Gateway in basic mode.")
    public boolean sendTextMessageSimple(@ActionInput(name = "message") String message) {
        return handler.sendTextMessageSimple(message);
    }

    @RuleAction(label = "send a message (basic mode)", description = "Send a message using the Threema.Gateway in basic mode.")
    public boolean sendTextMessageSimple(@ActionInput(name = "threemaId") String threemaId,
            @ActionInput(name = "message") String message) {
        return handler.sendTextMessageSimple(threemaId, message);
    }
}
