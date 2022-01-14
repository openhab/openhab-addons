/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mail.internal.action;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.AddressException;

import org.apache.commons.mail.EmailException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mail.internal.MailBuilder;
import org.openhab.binding.mail.internal.SMTPHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SendMailActions} class defines rule actions for sending mail
 *
 * @author Jan N. Klug - Initial contribution
 */
@ThingActionsScope(name = "mail")
@NonNullByDefault
public class SendMailActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(SendMailActions.class);

    private @Nullable SMTPHandler handler;
    private Map<String, String> headers = new HashMap<>();

    @RuleAction(label = "@text/sendMessageActionLabel", description = "@text/sendMessageActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendMail(
            @ActionInput(name = "recipient") @Nullable String recipient,
            @ActionInput(name = "subject") @Nullable String subject,
            @ActionInput(name = "text") @Nullable String text) {
        return sendMailWithAttachments(recipient, subject, text, List.of());
    }

    @RuleAction(label = "@text/sendAttachmentMessageActionLabel", description = "@text/sendAttachmentMessageActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendMailWithAttachment(
            @ActionInput(name = "recipient") @Nullable String recipient,
            @ActionInput(name = "subject") @Nullable String subject, @ActionInput(name = "text") @Nullable String text,
            @ActionInput(name = "url") @Nullable String urlString) {
        List<String> urlList = new ArrayList<>();
        if (urlString != null) {
            urlList.add(urlString);
        }
        return sendMailWithAttachments(recipient, subject, text, urlList);
    }

    @RuleAction(label = "@text/sendAttachmentsMessageActionLabel", description = "@text/sendAttachmentsMessageActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendMailWithAttachments(
            @ActionInput(name = "recipient") @Nullable String recipient,
            @ActionInput(name = "subject") @Nullable String subject, @ActionInput(name = "text") @Nullable String text,
            @ActionInput(name = "urlList") @Nullable List<String> urlStringList) {
        if (recipient == null) {
            logger.warn("Cannot send mail as recipient is missing.");
            return false;
        }

        try {
            MailBuilder builder = new MailBuilder(recipient);

            if (subject != null && !subject.isEmpty()) {
                builder.withSubject(subject);
            }
            if (text != null && !text.isEmpty()) {
                builder.withText(text);
            }
            if (urlStringList != null) {
                for (String urlString : urlStringList) {
                    builder.withURLAttachment(urlString);
                }
            }

            headers.forEach((name, value) -> builder.withHeader(name, value));

            final SMTPHandler handler = this.handler;
            if (handler == null) {
                logger.info("Handler is null, cannot send mail.");
                return false;
            } else {
                return handler.sendMail(builder.build());
            }
        } catch (AddressException | MalformedURLException | EmailException e) {
            logger.warn("Could not send mail: {}", e.getMessage());
            return false;
        }
    }

    public static boolean sendMail(ThingActions actions, @Nullable String recipient, @Nullable String subject,
            @Nullable String text) {
        return SendMailActions.sendMail(actions, recipient, subject, text, List.of());
    }

    public static boolean sendMail(ThingActions actions, @Nullable String recipient, @Nullable String subject,
            @Nullable String text, @Nullable String urlString) {
        List<String> urlList = new ArrayList<>();
        if (urlString != null) {
            urlList.add(urlString);
        }
        return SendMailActions.sendMail(actions, recipient, subject, text, urlList);
    }

    public static boolean sendMail(ThingActions actions, @Nullable String recipient, @Nullable String subject,
            @Nullable String text, @Nullable List<String> urlStringList) {
        return ((SendMailActions) actions).sendMailWithAttachments(recipient, subject, text, urlStringList);
    }

    @RuleAction(label = "@text/sendHTMLMessageActionLabel", description = "@text/sendHTMLMessageActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendHtmlMail(
            @ActionInput(name = "recipient") @Nullable String recipient,
            @ActionInput(name = "subject") @Nullable String subject,
            @ActionInput(name = "html") @Nullable String html) {
        return sendHtmlMailWithAttachments(recipient, subject, html, List.of());
    }

    @RuleAction(label = "@text/sendHTMLAttachmentMessageActionLabel", description = "@text/sendHTMLAttachmentMessageActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendHtmlMailWithAttachment(
            @ActionInput(name = "recipient") @Nullable String recipient,
            @ActionInput(name = "subject") @Nullable String subject, @ActionInput(name = "html") @Nullable String html,
            @ActionInput(name = "url") @Nullable String urlString) {
        List<String> urlList = new ArrayList<>();
        if (urlString != null) {
            urlList.add(urlString);
        }
        return sendHtmlMailWithAttachments(recipient, subject, html, urlList);
    }

    @RuleAction(label = "@text/sendHTMLAttachmentsMessageActionLabel", description = "@text/sendHTMLAttachmentsMessageActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendHtmlMailWithAttachments(
            @ActionInput(name = "recipient") @Nullable String recipient,
            @ActionInput(name = "subject") @Nullable String subject, @ActionInput(name = "html") @Nullable String html,
            @ActionInput(name = "urlList") @Nullable List<String> urlStringList) {
        if (recipient == null) {
            logger.warn("Cannot send mail as recipient is missing.");
            return false;
        }

        try {
            MailBuilder builder = new MailBuilder(recipient);

            if (subject != null && !subject.isEmpty()) {
                builder.withSubject(subject);
            }
            if (html != null && !html.isEmpty()) {
                builder.withHtml(html);
            }
            if (urlStringList != null) {
                for (String urlString : urlStringList) {
                    builder.withURLAttachment(urlString);
                }
            }

            headers.forEach((name, value) -> builder.withHeader(name, value));

            final SMTPHandler handler = this.handler;
            if (handler == null) {
                logger.warn("Handler is null, cannot send mail.");
                return false;
            } else {
                return handler.sendMail(builder.build());
            }
        } catch (AddressException | MalformedURLException | EmailException e) {
            logger.warn("Could not send mail: {}", e.getMessage());
            return false;
        }
    }

    public static boolean sendHtmlMail(ThingActions actions, @Nullable String recipient, @Nullable String subject,
            @Nullable String html) {
        return SendMailActions.sendHtmlMail(actions, recipient, subject, html, List.of());
    }

    public static boolean sendHtmlMail(ThingActions actions, @Nullable String recipient, @Nullable String subject,
            @Nullable String html, @Nullable String urlString) {
        List<String> urlList = new ArrayList<>();
        if (urlString != null) {
            urlList.add(urlString);
        }
        return SendMailActions.sendHtmlMail(actions, recipient, subject, html, urlList);
    }

    public static boolean sendHtmlMail(ThingActions actions, @Nullable String recipient, @Nullable String subject,
            @Nullable String html, @Nullable List<String> urlStringList) {
        return ((SendMailActions) actions).sendHtmlMailWithAttachments(recipient, subject, html, urlStringList);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof SMTPHandler) {
            this.handler = (SMTPHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/addHeaderActionLabel", description = "@text/addHeaderActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean addHeader(
            @ActionInput(name = "name") @Nullable String name, @ActionInput(name = "value") @Nullable String value) {
        if (name != null && !name.isEmpty()) {
            if (value != null && !value.isEmpty()) {
                headers.put(name, value);
            } else {
                headers.remove(name);
            }
            return true;
        }
        return false;
    }

    public static boolean addHeader(ThingActions actions, @Nullable String name, @Nullable String value) {
        return ((SendMailActions) actions).addHeader(name, value);
    }
}
