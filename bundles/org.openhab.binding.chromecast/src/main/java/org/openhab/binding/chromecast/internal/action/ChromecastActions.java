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
package org.openhab.binding.chromecast.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.chromecast.internal.handler.ChromecastHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChromecastActions} class defines rule actions for playing URLs
 *
 * @author Scott Hanson - Initial contribution
 */
@ThingActionsScope(name = "chromecast")
@NonNullByDefault
public class ChromecastActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(ChromecastActions.class);

    private @Nullable ChromecastHandler handler;

    @RuleAction(label = "@text/playURLActionLabel", description = "@text/playURLActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean playURL(
            @ActionInput(name = "url") @Nullable String url) {
        if (url == null) {
            logger.warn("Cannot Play as URL is missing.");
            return false;
        }

        final ChromecastHandler handler = this.handler;
        if (handler == null) {
            logger.warn("Handler is null, cannot play.");
            return false;
        } else {
            return handler.playURL(null, url, null);
        }
    }

    @RuleAction(label = "@text/playURLTypeActionLabel", description = "@text/playURLTypeActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean playURL(
            @ActionInput(name = "url") @Nullable String url,
            @ActionInput(name = "mediaType") @Nullable String mediaType) {
        if (url == null) {
            logger.warn("Cannot Play as URL is missing.");
            return false;
        }

        final ChromecastHandler handler = this.handler;
        if (handler == null) {
            logger.warn("Handler is null, cannot tweet.");
            return false;
        } else {
            return handler.playURL(null, url, mediaType);
        }
    }

    public static boolean playURL(ThingActions actions, @Nullable String url) {
        return ((ChromecastActions) actions).playURL(url);
    }

    public static boolean playURL(ThingActions actions, @Nullable String url, @Nullable String mediaType) {
        return ((ChromecastActions) actions).playURL(url, mediaType);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ChromecastHandler chromecastHandler) {
            this.handler = chromecastHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
