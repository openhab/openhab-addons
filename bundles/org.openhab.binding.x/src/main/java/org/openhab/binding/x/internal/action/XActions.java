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
package org.openhab.binding.x.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.x.internal.XHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TweetActions} class defines rule actions for sending tweet
 *
 * @author Scott Hanson - Initial contribution
 */
@ThingActionsScope(name = "x")
@NonNullByDefault
public class XActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(XActions.class);

    private @Nullable XHandler handler;

    @RuleAction(label = "@text/sendTweetActionLabel", description = "@text/sendTweetActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendTweet(
            @ActionInput(name = "text") @Nullable String text) {
        if (text == null) {
            logger.warn("Cannot send Tweet as text is missing.");
            return false;
        }

        final XHandler handler = this.handler;
        if (handler == null) {
            logger.debug("Handler is null, cannot tweet.");
            return false;
        } else {
            return handler.sendTweet(text);
        }
    }

    @RuleAction(label = "@text/sendAttachmentTweetActionLabel", description = "@text/sendAttachmentTweetActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendTweetWithAttachment(
            @ActionInput(name = "text") @Nullable String text, @ActionInput(name = "url") @Nullable String urlString) {
        if (text == null) {
            logger.warn("Cannot send Tweet as text is missing.");
            return false;
        }
        if (urlString == null) {
            logger.warn("Cannot send Tweet as urlString is missing.");
            return false;
        }

        final XHandler handler = this.handler;
        if (handler == null) {
            logger.debug("Handler is null, cannot tweet.");
            return false;
        } else {
            return handler.sendTweet(text, urlString);
        }
    }

    @RuleAction(label = "@text/sendDirectMessageActionLabel", description = "@text/sendDirectMessageActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendDirectMessage(
            @ActionInput(name = "recipient") @Nullable String recipient,
            @ActionInput(name = "text") @Nullable String text) {
        if (recipient == null) {
            logger.warn("Cannot send Direct Message as recipient is missing.");
            return false;
        }
        if (text == null) {
            logger.warn("Cannot send Direct Message as text is missing.");
            return false;
        }

        final XHandler handler = this.handler;
        if (handler == null) {
            logger.debug("Handler is null, cannot tweet.");
            return false;
        } else {
            return handler.sendDirectMessage(recipient, text);
        }
    }

    public static boolean sendTweet(ThingActions actions, @Nullable String text) {
        return ((XActions) actions).sendTweet(text);
    }

    public static boolean sendTweetWithAttachment(ThingActions actions, @Nullable String text,
            @Nullable String urlString) {
        return ((XActions) actions).sendTweetWithAttachment(text, urlString);
    }

    public static boolean sendDirectMessage(ThingActions actions, @Nullable String recipient, @Nullable String text) {
        return ((XActions) actions).sendDirectMessage(recipient, text);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof XHandler) {
            this.handler = (XHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
