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
package org.openhab.binding.pushbullet.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pushbullet.internal.handler.PushbulletHandler;
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
 * The {@link PushbulletActions} class defines rule actions for sending notifications
 *
 * @author Hakan Tandogan - Initial contribution
 * @author Jeremy Setton - Add link and file push type support
 */
@Component(scope = ServiceScope.PROTOTYPE, service = PushbulletActions.class)
@ThingActionsScope(name = "pushbullet")
@NonNullByDefault
public class PushbulletActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(PushbulletActions.class);

    private @Nullable PushbulletHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (PushbulletHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/actionSendPushbulletNoteLabel", description = "@text/actionSendPushbulletNoteDesc")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendPushbulletNote(
            @ActionInput(name = "recipient", label = "@text/actionSendPushbulletNoteInputRecipientLabel", description = "@text/actionSendPushbulletNoteInputRecipientDesc", type = "java.lang.String") @Nullable String recipient,
            @ActionInput(name = "title", label = "@text/actionSendPushbulletNoteInputTitleLabel", description = "@text/actionSendPushbulletNoteInputTitleDesc", type = "java.lang.String") @Nullable String title,
            @ActionInput(name = "message", label = "@text/actionSendPushbulletNoteInputMessageLabel", description = "@text/actionSendPushbulletNoteInputMessageDesc", type = "java.lang.String", required = true) String message) {
        logger.trace("sendPushbulletNote '{}', '{}', '{}'", recipient, title, message);

        PushbulletHandler localHandler = handler;
        if (localHandler == null) {
            logger.warn("Pushbullet service Handler is null!");
            return false;
        }

        return localHandler.sendPushNote(recipient, title, message);
    }

    public static boolean sendPushbulletNote(ThingActions actions, @Nullable String recipient, @Nullable String title,
            String message) {
        return ((PushbulletActions) actions).sendPushbulletNote(recipient, title, message);
    }

    @RuleAction(label = "@text/actionSendPushbulletNoteLabel", description = "@text/actionSendPushbulletNoteDesc")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendPushbulletNote(
            @ActionInput(name = "recipient", label = "@text/actionSendPushbulletNoteInputRecipientLabel", description = "@text/actionSendPushbulletNoteInputRecipientDesc", type = "java.lang.String") @Nullable String recipient,
            @ActionInput(name = "message", label = "@text/actionSendPushbulletNoteInputMessageLabel", description = "@text/actionSendPushbulletNoteInputMessageDesc", type = "java.lang.String", required = true) String message) {
        logger.trace("sendPushbulletNote '{}', '{}'", recipient, message);

        PushbulletHandler localHandler = handler;
        if (localHandler == null) {
            logger.warn("Pushbullet service Handler is null!");
            return false;
        }

        return localHandler.sendPushNote(recipient, null, message);
    }

    public static boolean sendPushbulletNote(ThingActions actions, @Nullable String recipient, String message) {
        return ((PushbulletActions) actions).sendPushbulletNote(recipient, message);
    }

    @RuleAction(label = "@text/actionSendPushbulletLinkLabel", description = "@text/actionSendPushbulletLinkDesc")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendPushbulletLink(
            @ActionInput(name = "recipient", label = "@text/actionSendPushbulletNoteInputRecipientLabel", description = "@text/actionSendPushbulletNoteInputRecipientDesc", type = "java.lang.String") @Nullable String recipient,
            @ActionInput(name = "title", label = "@text/actionSendPushbulletNoteInputTitleLabel", description = "@text/actionSendPushbulletNoteInputTitleDesc", type = "java.lang.String") @Nullable String title,
            @ActionInput(name = "message", label = "@text/actionSendPushbulletNoteInputMessageLabel", description = "@text/actionSendPushbulletNoteInputMessageDesc", type = "java.lang.String") @Nullable String message,
            @ActionInput(name = "url", label = "@text/actionSendPushbulletLinkInputUrlLabel", description = "@text/actionSendPushbulletLinkInputUrlDesc", type = "java.lang.String", required = true) String url) {
        logger.trace("sendPushbulletLink '{}', '{}', '{}', '{}'", recipient, title, message, url);

        PushbulletHandler localHandler = handler;
        if (localHandler == null) {
            logger.warn("Pushbullet service Handler is null!");
            return false;
        }

        return localHandler.sendPushLink(recipient, title, message, url);
    }

    public static boolean sendPushbulletLink(ThingActions actions, @Nullable String recipient, @Nullable String title,
            @Nullable String message, String url) {
        return ((PushbulletActions) actions).sendPushbulletLink(recipient, title, message, url);
    }

    @RuleAction(label = "@text/actionSendPushbulletLinkLabel", description = "@text/actionSendPushbulletLinkDesc")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendPushbulletLink(
            @ActionInput(name = "recipient", label = "@text/actionSendPushbulletNoteInputRecipientLabel", description = "@text/actionSendPushbulletNoteInputRecipientDesc", type = "java.lang.String") @Nullable String recipient,
            @ActionInput(name = "url", label = "@text/actionSendPushbulletLinkInputUrlLabel", description = "@text/actionSendPushbulletLinkInputUrlDesc", type = "java.lang.String", required = true) String url) {
        logger.trace("sendPushbulletLink '{}', '{}'", recipient, url);

        PushbulletHandler localHandler = handler;
        if (localHandler == null) {
            logger.warn("Pushbullet service Handler is null!");
            return false;
        }

        return localHandler.sendPushLink(recipient, null, null, url);
    }

    public static boolean sendPushbulletLink(ThingActions actions, @Nullable String recipient, String url) {
        return ((PushbulletActions) actions).sendPushbulletLink(recipient, url);
    }

    @RuleAction(label = "@text/actionSendPushbulletFileLabel", description = "@text/actionSendPushbulletFileDesc")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendPushbulletFile(
            @ActionInput(name = "recipient", label = "@text/actionSendPushbulletNoteInputRecipientLabel", description = "@text/actionSendPushbulletNoteInputRecipientDesc", type = "java.lang.String") @Nullable String recipient,
            @ActionInput(name = "title", label = "@text/actionSendPushbulletNoteInputTitleLabel", description = "@text/actionSendPushbulletNoteInputTitleDesc", type = "java.lang.String") @Nullable String title,
            @ActionInput(name = "message", label = "@text/actionSendPushbulletNoteInputMessageLabel", description = "@text/actionSendPushbulletNoteInputMessageDesc", type = "java.lang.String") @Nullable String message,
            @ActionInput(name = "content", label = "@text/actionSendPushbulletFileInputContent", description = "@text/actionSendPushbulletFileInputContentDesc", type = "java.lang.String", required = true) String content,
            @ActionInput(name = "filename", label = "@text/actionSendPushbulletFileInputName", description = "@text/actionSendPushbulletFileInputNameDesc", type = "java.lang.String") @Nullable String fileName) {
        logger.trace("sendPushbulletFile '{}', '{}', '{}', '{}', '{}'", recipient, title, message, content, fileName);

        PushbulletHandler localHandler = handler;
        if (localHandler == null) {
            logger.warn("Pushbullet service Handler is null!");
            return false;
        }

        return localHandler.sendPushFile(recipient, title, message, content, fileName);
    }

    public static boolean sendPushbulletFile(ThingActions actions, @Nullable String recipient, @Nullable String title,
            @Nullable String message, String content, @Nullable String filename) {
        return ((PushbulletActions) actions).sendPushbulletFile(recipient, title, message, content, filename);
    }

    @RuleAction(label = "@text/actionSendPushbulletFileLabel", description = "@text/actionSendPushbulletFileDesc")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendPushbulletFile(
            @ActionInput(name = "recipient", label = "@text/actionSendPushbulletNoteInputRecipientLabel", description = "@text/actionSendPushbulletNoteInputRecipientDesc", type = "java.lang.String") @Nullable String recipient,
            @ActionInput(name = "title", label = "@text/actionSendPushbulletNoteInputTitleLabel", description = "@text/actionSendPushbulletNoteInputTitleDesc", type = "java.lang.String") @Nullable String title,
            @ActionInput(name = "message", label = "@text/actionSendPushbulletNoteInputMessageLabel", description = "@text/actionSendPushbulletNoteInputMessageDesc", type = "java.lang.String") @Nullable String message,
            @ActionInput(name = "content", label = "@text/actionSendPushbulletFileInputContent", description = "@text/actionSendPushbulletFileInputContentDesc", type = "java.lang.String", required = true) String content) {
        logger.trace("sendPushbulletFile '{}', '{}', '{}', '{}'", recipient, title, message, content);

        PushbulletHandler localHandler = handler;
        if (localHandler == null) {
            logger.warn("Pushbullet service Handler is null!");
            return false;
        }

        return localHandler.sendPushFile(recipient, title, message, content, null);
    }

    public static boolean sendPushbulletFile(ThingActions actions, @Nullable String recipient, @Nullable String title,
            @Nullable String message, String content) {
        return ((PushbulletActions) actions).sendPushbulletFile(recipient, title, message, content);
    }

    @RuleAction(label = "@text/actionSendPushbulletFileLabel", description = "@text/actionSendPushbulletFileDesc")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendPushbulletFile(
            @ActionInput(name = "recipient", label = "@text/actionSendPushbulletNoteInputRecipientLabel", description = "@text/actionSendPushbulletNoteInputRecipientDesc", type = "java.lang.String") @Nullable String recipient,
            @ActionInput(name = "content", label = "@text/actionSendPushbulletFileInputContent", description = "@text/actionSendPushbulletFileInputContentDesc", type = "java.lang.String", required = true) String content) {
        logger.trace("sendPushbulletFile '{}', '{}'", recipient, content);

        PushbulletHandler localHandler = handler;
        if (localHandler == null) {
            logger.warn("Pushbullet service Handler is null!");
            return false;
        }

        return localHandler.sendPushFile(recipient, null, null, content, null);
    }

    public static boolean sendPushbulletFile(ThingActions actions, @Nullable String recipient, String content) {
        return ((PushbulletActions) actions).sendPushbulletFile(recipient, content);
    }
}
