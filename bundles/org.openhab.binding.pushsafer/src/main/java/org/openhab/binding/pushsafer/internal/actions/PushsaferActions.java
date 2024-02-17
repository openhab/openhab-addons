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
package org.openhab.binding.pushsafer.internal.actions;

import static org.openhab.binding.pushsafer.internal.PushsaferBindingConstants.DEFAULT_TITLE;
import static org.openhab.binding.pushsafer.internal.connection.PushsaferMessageBuilder.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pushsafer.internal.connection.PushsaferConfigurationException;
import org.openhab.binding.pushsafer.internal.connection.PushsaferMessageBuilder;
import org.openhab.binding.pushsafer.internal.handler.PushsaferAccountHandler;
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
 * Some automation actions to be used with a {@link PushsaferAccountHandler}.
 *
 * @author Kevin Siml - Initial contribution, forked from Christoph Weitkamp
 */
@Component(scope = ServiceScope.PROTOTYPE, service = PushsaferActions.class)
@ThingActionsScope(name = "pushsafer")
@NonNullByDefault
public class PushsaferActions implements ThingActions {

    private static final String DEFAULT_EMERGENCY_PRIORITY = "2";

    private final Logger logger = LoggerFactory.getLogger(PushsaferActions.class);

    private @NonNullByDefault({}) PushsaferAccountHandler accountHandler;

    @RuleAction(label = "@text/sendPushsaferMessageActionLabel", description = "@text/sendPushsaferMessageActionDescription")
    public @ActionOutput(name = "sent", label = "@text/sendPushsaferMessageActionOutputLabel", description = "@text/sendPushsaferMessageActionOutputDescription", type = "java.lang.Boolean") Boolean sendPushsaferMessage(
            @ActionInput(name = "message", label = "@text/sendPushsaferMessageActionInputMessageLabel", description = "@text/sendPushsaferMessageActionInputMessageDescription", type = "java.lang.String", required = true) String message,
            @ActionInput(name = "title", label = "@text/sendPushsaferMessageActionInputTitleLabel", description = "@text/sendPushsaferMessageActionInputTitleDescription", type = "java.lang.String", defaultValue = DEFAULT_TITLE) @Nullable String title) {
        logger.trace("ThingAction 'sendPushsaferMessage' called with value(s): message='{}', title='{}'", message,
                title);
        return send(getDefaultPushsaferMessageBuilder(message), title);
    }

    public static Boolean sendPushsaferMessage(ThingActions actions, String message, @Nullable String title) {
        return ((PushsaferActions) actions).sendPushsaferMessage(message, title);
    }

    @RuleAction(label = "@text/sendPushsaferURLMessageActionLabel", description = "@text/sendPushsaferURLMessageActionDescription")
    public @ActionOutput(name = "sent", label = "@text/sendPushsaferMessageActionOutputLabel", description = "@text/sendPushsaferMessageActionOutputDescription", type = "java.lang.Boolean") Boolean sendPushsaferURLMessage(
            @ActionInput(name = "message", label = "@text/sendPushsaferMessageActionInputMessageLabel", description = "@text/sendPushsaferMessageActionInputMessageDescription", type = "java.lang.String", required = true) String message,
            @ActionInput(name = "title", label = "@text/sendPushsaferMessageActionInputTitleLabel", description = "@text/sendPushsaferMessageActionInputTitleDescription", type = "java.lang.String", defaultValue = DEFAULT_TITLE) @Nullable String title,
            @ActionInput(name = "url", label = "@text/sendPushsaferMessageActionInputURLLabel", description = "@text/sendPushsaferMessageActionInputURLDescription", type = "java.lang.String", required = true) String url,
            @ActionInput(name = "urlTitle", label = "@text/sendPushsaferMessageActionInputURLTitleLabel", description = "@text/sendPushsaferMessageActionInputURLTitleDescription", type = "java.lang.String") @Nullable String urlTitle) {
        logger.trace(
                "ThingAction 'sendPushsaferURLMessage' called with value(s): message='{}', url='{}', title='{}', urlTitle='{}'",
                message, url, title, urlTitle);
        if (url == null) {
            throw new IllegalArgumentException("Skip sending message as 'url' is null.");
        }

        PushsaferMessageBuilder builder = getDefaultPushsaferMessageBuilder(message).withUrl(url);
        if (urlTitle != null) {
            builder.withUrl(urlTitle);
        }
        return send(builder, title);
    }

    public static Boolean sendPushsaferURLMessage(ThingActions actions, String message, @Nullable String title,
            String url, @Nullable String urlTitle) {
        return ((PushsaferActions) actions).sendPushsaferURLMessage(message, title, url, urlTitle);
    }

    @RuleAction(label = "@text/sendHTMLMessageActionLabel", description = "@text/sendHTMLMessageActionDescription")
    public @ActionOutput(name = "sent", label = "@text/sendPushsaferMessageActionOutputLabel", description = "@text/sendPushsaferMessageActionOutputDescription", type = "java.lang.Boolean") Boolean sendPushsaferHtmlMessage(
            @ActionInput(name = "message", label = "@text/sendPushsaferMessageActionInputMessageLabel", description = "@text/sendPushsaferMessageActionInputMessageDescription", type = "java.lang.String", required = true) String message,
            @ActionInput(name = "title", label = "@text/sendPushsaferMessageActionInputTitleLabel", description = "@text/sendPushsaferMessageActionInputTitleDescription", type = "java.lang.String", defaultValue = DEFAULT_TITLE) @Nullable String title) {
        logger.trace("ThingAction 'sendPushsaferHtmlMessage' called with value(s): message='{}', title='{}'", message,
                title);
        return send(getDefaultPushsaferMessageBuilder(message).withHtmlFormatting(), title);
    }

    public static Boolean sendPushsaferHtmlMessage(ThingActions actions, String message, @Nullable String title) {
        return ((PushsaferActions) actions).sendPushsaferHtmlMessage(message, title);
    }

    @RuleAction(label = "@text/sendPushsaferMonospaceMessageActionLabel", description = "@text/sendPushsaferMonospaceMessageActionDescription")
    public @ActionOutput(name = "sent", label = "@text/sendPushsaferMessageActionOutputLabel", description = "@text/sendPushsaferMessageActionOutputDescription", type = "java.lang.Boolean") Boolean sendPushsaferMonospaceMessage(
            @ActionInput(name = "message", label = "@text/sendPushsaferMessageActionInputMessageLabel", description = "@text/sendPushsaferMessageActionInputMessageDescription", type = "java.lang.String", required = true) String message,
            @ActionInput(name = "title", label = "@text/sendPushsaferMessageActionInputTitleLabel", description = "@text/sendPushsaferMessageActionInputTitleDescription", type = "java.lang.String", defaultValue = DEFAULT_TITLE) @Nullable String title) {
        logger.trace("ThingAction 'sendPushsaferMonospaceMessage' called with value(s): message='{}', title='{}'",
                message, title);
        return send(getDefaultPushsaferMessageBuilder(message).withMonospaceFormatting(), title);
    }

    public static Boolean sendPushsaferMonospaceMessage(ThingActions actions, String message, @Nullable String title) {
        return ((PushsaferActions) actions).sendPushsaferMonospaceMessage(message, title);
    }

    @RuleAction(label = "@text/sendPushsaferAttachmentMessageActionLabel", description = "@text/sendPushsaferAttachmentMessageActionDescription")
    public @ActionOutput(name = "sent", label = "@text/sendPushsaferMessageActionOutputLabel", description = "@text/sendPushsaferMessageActionOutputDescription", type = "java.lang.Boolean") Boolean sendPushsaferAttachmentMessage(
            @ActionInput(name = "message", label = "@text/sendPushsaferMessageActionInputMessageLabel", description = "@text/sendPushsaferMessageActionInputMessageDescription", type = "java.lang.String", required = true) String message,
            @ActionInput(name = "title", label = "@text/sendPushsaferMessageActionInputTitleLabel", description = "@text/sendPushsaferMessageActionInputTitleDescription", type = "java.lang.String", defaultValue = DEFAULT_TITLE) @Nullable String title,
            @ActionInput(name = "attachment", label = "@text/sendPushsaferMessageActionInputAttachmentLabel", description = "@text/sendPushsaferMessageActionInputAttachmentDescription", type = "java.lang.String", required = true) String attachment,
            @ActionInput(name = "contentType", label = "@text/sendPushsaferMessageActionInputContentTypeLabel", description = "@text/sendPushsaferMessageActionInputContentTypeDescription", type = "java.lang.String", defaultValue = DEFAULT_CONTENT_TYPE) @Nullable String contentType,
            @ActionInput(name = "authentication", label = "@text/sendPushsaferMessageActionInputAuthenticationLabel", description = "@text/sendPushsaferMessageActionInputAuthenticationDescription", type = "java.lang.String", defaultValue = DEFAULT_AUTH) @Nullable String authentication) {
        logger.trace(
                "ThingAction 'sendPushsaferAttachmentMessage' called with value(s): message='{}', title='{}', attachment='{}', contentType='{}', authentication='{}'",
                message, title, attachment, contentType, authentication);
        if (attachment == null) {
            throw new IllegalArgumentException("Skip sending message as 'attachment' is null.");
        }

        PushsaferMessageBuilder builder = getDefaultPushsaferMessageBuilder(message).withAttachment(attachment);
        if (contentType != null) {
            builder.withContentType(contentType);
        }
        if (authentication != null) {
            builder.withAuthentication(authentication);
        }
        return send(builder, title);
    }

    public static Boolean sendPushsaferAttachmentMessage(ThingActions actions, String message, @Nullable String title,
            String attachment, @Nullable String contentType, @Nullable String authentication) {
        return ((PushsaferActions) actions).sendPushsaferAttachmentMessage(message, title, attachment, contentType,
                authentication);
    }

    @RuleAction(label = "@text/sendPushsaferPriorityMessageActionLabel", description = "@text/sendPushsaferPriorityMessageActionDescription")
    public @ActionOutput(name = "receipt", label = "@text/sendPushsaferPriorityMessageActionOutputLabel", description = "@text/sendPushsaferPriorityMessageActionOutputDescription", type = "java.lang.String") String sendPushsaferPriorityMessage(
            @ActionInput(name = "message", label = "@text/sendPushsaferMessageActionInputMessageLabel", description = "@text/sendPushsaferMessageActionInputMessageDescription", type = "java.lang.String", required = true) String message,
            @ActionInput(name = "title", label = "@text/sendPushsaferMessageActionInputTitleLabel", description = "@text/sendPushsaferMessageActionInputTitleDescription", type = "java.lang.String", defaultValue = DEFAULT_TITLE) @Nullable String title,
            @ActionInput(name = "priority", label = "@text/sendPushsaferMessageActionInputPriorityLabel", description = "@text/sendPushsaferMessageActionInputPriorityDescription", type = "java.lang.Integer", defaultValue = DEFAULT_EMERGENCY_PRIORITY) @Nullable Integer priority) {
        logger.trace(
                "ThingAction 'sendPushsaferPriorityMessage' called with value(s): message='{}', title='{}', priority='{}'",
                message, title, priority);
        PushsaferMessageBuilder builder = getDefaultPushsaferMessageBuilder(message)
                .withPriority(priority == null ? EMERGENCY_PRIORITY : priority.intValue());

        if (title != null) {
            builder.withTitle(title);
        }
        return accountHandler.sendPushsaferPriorityMessage(builder);
    }

    public static String sendPushsaferPriorityMessage(ThingActions actions, String message, @Nullable String title,
            @Nullable Integer priority) {
        return ((PushsaferActions) actions).sendPushsaferPriorityMessage(message, title, priority);
    }

    @RuleAction(label = "@text/cancelPushsaferPriorityMessageActionLabel", description = "@text/cancelPushsaferPriorityMessageActionDescription")
    public @ActionOutput(name = "canceled", label = "@text/cancelPushsaferPriorityMessageActionOutputLabel", description = "@text/cancelPushsaferPriorityMessageActionOutputDescription", type = "java.lang.Boolean") Boolean cancelPushsaferPriorityMessage(
            @ActionInput(name = "receipt", label = "@text/cancelPushsaferPriorityMessageActionInputReceiptLabel", description = "@text/cancelPushsaferPriorityMessageActionInputReceiptDescription", type = "java.lang.String", required = true) String receipt) {
        logger.trace("ThingAction 'cancelPushsaferPriorityMessage' called with value(s): '{}'", receipt);
        if (accountHandler == null) {
            throw new RuntimeException("PushsaferAccountHandler is null!");
        }

        if (receipt == null) {
            throw new IllegalArgumentException("Skip sending message as 'receipt' is null.");
        }

        return accountHandler.cancelPushsaferPriorityMessage(receipt);
    }

    public static Boolean cancelPushsaferPriorityMessage(ThingActions actions, String receipt) {
        return ((PushsaferActions) actions).cancelPushsaferPriorityMessage(receipt);
    }

    @RuleAction(label = "@text/sendPushsaferMessageToDeviceActionLabel", description = "@text/sendPushsaferMessageToDeviceActionDescription")
    public @ActionOutput(name = "sent", label = "@text/sendPushsaferMessageActionOutputLabel", description = "@text/sendPushsaferMessageActionOutputDescription", type = "java.lang.Boolean") Boolean sendPushsaferMessageToDevice(
            @ActionInput(name = "device", label = "@text/sendPushsaferMessageActionInputDeviceLabel", description = "@text/sendPushsaferMessageActionInputDeviceDescription", type = "java.lang.String", required = true) String device,
            @ActionInput(name = "message", label = "@text/sendPushsaferMessageActionInputMessageLabel", description = "@text/sendPushsaferMessageActionInputMessageDescription", type = "java.lang.String", required = true) String message,
            @ActionInput(name = "title", label = "@text/sendPushsaferMessageActionInputTitleLabel", description = "@text/sendPushsaferMessageActionInputTitleDescription", type = "java.lang.String", defaultValue = DEFAULT_TITLE) @Nullable String title) {
        logger.trace(
                "ThingAction 'sendPushsaferMessageToDevice' called with value(s): device='{}', message='{}', title='{}'",
                device, message, title);
        if (device == null) {
            throw new IllegalArgumentException("Skip sending message as 'device' is null.");
        }

        return send(getDefaultPushsaferMessageBuilder(message).withDevice(device), title);
    }

    public static Boolean sendPushsaferMessageToDevice(ThingActions actions, String device, String message,
            @Nullable String title) {
        return ((PushsaferActions) actions).sendPushsaferMessageToDevice(device, message, title);
    }

    private PushsaferMessageBuilder getDefaultPushsaferMessageBuilder(String message) {
        if (accountHandler == null) {
            throw new RuntimeException("PushsaferAccountHandler is null!");
        }

        if (message == null) {
            throw new IllegalArgumentException("Skip sending message as 'message' is null.");
        }

        try {
            return accountHandler.getDefaultPushsaferMessageBuilder(message);
        } catch (PushsaferConfigurationException e) {
            throw new IllegalArgumentException(e.getCause());
        }
    }

    private Boolean send(PushsaferMessageBuilder builder, @Nullable String title) {
        if (title != null) {
            builder.withTitle(title);
        }
        return accountHandler.sendPushsaferMessage(builder);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.accountHandler = (PushsaferAccountHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return accountHandler;
    }
}
