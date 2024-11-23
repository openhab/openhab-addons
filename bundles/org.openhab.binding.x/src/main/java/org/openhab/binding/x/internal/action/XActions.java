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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link XActions} class defines rule actions for sending post
 *
 * @author Scott Hanson - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = XActions.class)
@ThingActionsScope(name = "x")
@NonNullByDefault
public class XActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(XActions.class);

    private @Nullable XHandler handler;

    @RuleAction(label = "@text/sendPostActionLabel", description = "@text/sendPostActionDescription")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendPost(
            @ActionInput(name = "text") @Nullable String text) {
        if (text == null) {
            logger.warn("Cannot send Post as text is missing.");
            return false;
        }

        final XHandler handler = this.handler;
        if (handler == null) {
            logger.debug("Handler is null, cannot post.");
            return false;
        } else {
            return handler.sendPost(text);
        }
    }

    @RuleAction(label = "@text/sendAttachmentPostActionLabel", description = "@text/sendAttachmentPostActionDescription")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendPostWithAttachment(
            @ActionInput(name = "text") @Nullable String text, @ActionInput(name = "url") @Nullable String urlString) {
        if (text == null) {
            logger.warn("Cannot send Post as text is missing.");
            return false;
        }
        if (urlString == null) {
            logger.warn("Cannot send Post as urlString is missing.");
            return false;
        }

        final XHandler handler = this.handler;
        if (handler == null) {
            logger.debug("Handler is null, cannot post.");
            return false;
        } else {
            return handler.sendPost(text, urlString);
        }
    }

    @RuleAction(label = "@text/sendDirectMessageActionLabel", description = "@text/sendDirectMessageActionDescription")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendDirectMessage(
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
            logger.debug("Handler is null, cannot post.");
            return false;
        } else {
            return handler.sendDirectMessage(recipient, text);
        }
    }

    public static boolean sendPost(ThingActions actions, @Nullable String text) {
        return ((XActions) actions).sendPost(text);
    }

    public static boolean sendPostWithAttachment(ThingActions actions, @Nullable String text,
            @Nullable String urlString) {
        return ((XActions) actions).sendPostWithAttachment(text, urlString);
    }

    public static boolean sendDirectMessage(ThingActions actions, @Nullable String recipient, @Nullable String text) {
        return ((XActions) actions).sendDirectMessage(recipient, text);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof XHandler xHandler) {
            this.handler = xHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
