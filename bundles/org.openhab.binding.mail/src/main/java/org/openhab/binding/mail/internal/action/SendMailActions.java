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
package org.openhab.binding.mail.internal.action;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

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

    @RuleAction(label = "send a text mail", description = "Sends a text mail.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendMail(
            @ActionInput(name = "recipient") @Nullable String recipient,
            @ActionInput(name = "subject") @Nullable String subject,
            @ActionInput(name = "text") @Nullable String text) {
        return sendMail(recipient, subject, text, new ArrayList<>());
    }

    @RuleAction(label = "send a text mail", description = "Sends a text mail with URL attachment.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendMail(
            @ActionInput(name = "recipient") @Nullable String recipient,
            @ActionInput(name = "subject") @Nullable String subject, @ActionInput(name = "text") @Nullable String text,
            @ActionInput(name = "url") @Nullable String urlString) {
        List<String> urlList = new ArrayList<>();
        if (urlString != null) {
            urlList.add(urlString);
        }
        return sendMail(recipient, subject, text, urlList);
    }

    @RuleAction(label = "send a text mail", description = "Sends a text mail with several URL attachments.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendMail(
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
        return SendMailActions.sendMail(actions, recipient, subject, text, new ArrayList<>());
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
        return ((SendMailActions) actions).sendMail(recipient, subject, text, urlStringList);
    }

    @RuleAction(label = "send a HTML mail", description = "Sends a HTML mail.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendHtmlMail(
            @ActionInput(name = "recipient") @Nullable String recipient,
            @ActionInput(name = "subject") @Nullable String subject,
            @ActionInput(name = "html") @Nullable String html) {
        return sendHtmlMail(recipient, subject, html, new ArrayList<>());
    }

    @RuleAction(label = "send a HTML mail", description = "Sends a HTML mail with URL attachment.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendHtmlMail(
            @ActionInput(name = "recipient") @Nullable String recipient,
            @ActionInput(name = "subject") @Nullable String subject, @ActionInput(name = "html") @Nullable String html,
            @ActionInput(name = "url") @Nullable String urlString) {
        List<String> urlList = new ArrayList<>();
        if (urlString != null) {
            urlList.add(urlString);
        }
        return sendHtmlMail(recipient, subject, html, urlList);
    }

    @RuleAction(label = "send a HTML mail", description = "Sends a HTML mail with several URL attachments.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendHtmlMail(
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
        return SendMailActions.sendHtmlMail(actions, recipient, subject, html, new ArrayList<>());
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
        return ((SendMailActions) actions).sendHtmlMail(recipient, subject, html, urlStringList);
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
}
