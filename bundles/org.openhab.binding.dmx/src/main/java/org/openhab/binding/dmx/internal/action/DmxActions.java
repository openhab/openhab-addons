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
package org.openhab.binding.dmx.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dmx.internal.DmxBridgeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DmxActions} provides actions for DMX Bridges
 *
 * @author Jan N. Klug - Initial contribution
 */
@ThingActionsScope(name = "dmx")
@NonNullByDefault
public class DmxActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(DmxActions.class);

    private @Nullable DmxBridgeHandler handler;

    @RuleAction(label = "immediately fade channels", description = "Immediately performs fade on selected DMX channels.")
    public void sendFade(@ActionInput(name = "channels") @Nullable String channels,
            @ActionInput(name = "fade") @Nullable String fade,
            @ActionInput(name = "resumeAfter") @Nullable Boolean resumeAfter) {
        logger.debug("thingHandlerAction called with inputs: {} {} {}", channels, fade, resumeAfter);
        DmxBridgeHandler handler = this.handler;

        if (handler == null) {
            logger.warn("DMX Action service ThingHandler is null!");
            return;
        }

        if (channels == null) {
            logger.debug("skipping immediate DMX action {} due to missing channel(s)", fade);
            return;
        }

        if (fade == null) {
            logger.debug("skipping immediate DMX action channel(s) {} due to missing fade", channels);
            return;
        }

        if (resumeAfter == null) {
            logger.debug("DMX action {} to channel(s) {} with default resumeAfter=false", fade, channels);
            handler.immediateFade(channels, fade, false);
        } else {
            handler.immediateFade(channels, fade, resumeAfter);
        }
    }

    public static void sendFade(ThingActions actions, @Nullable String channels, @Nullable String fade,
            @Nullable Boolean resumeAfter) {
        ((DmxActions) actions).sendFade(channels, fade, resumeAfter);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof DmxBridgeHandler) {
            this.handler = (DmxBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
