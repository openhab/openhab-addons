/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.mail.internal;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.PatchedMailcapCommandMap;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mail.internal.action.SendMailActions;
import org.openhab.binding.mail.internal.config.SMTPConfig;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SMTPHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan N. Klug - Initial contribution
 * @author Hans-Jörg Merk - Fixed UnsupportedDataTypeException - Originally by Jan N. Klug
 *         - Fix sending HTML/Multipart mail - Originally by Jan N. Klug
 * @author Gaël L'hopital - Added session initialization for thing status check
 */
@NonNullByDefault
public class SMTPHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SMTPHandler.class);
    private final PatchedMailcapCommandMap commandMap = new PatchedMailcapCommandMap();

    private String sender = "";
    private @Nullable Session session;

    public SMTPHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        SMTPConfig config = getConfigAs(SMTPConfig.class);
        if (config.sender instanceof String confSender) {
            sender = confSender;
        }

        Email mail = new SimpleEmail();
        mail.setHostName(config.hostname);

        int port = config.port != 0 ? config.port : config.security == ServerSecurity.SSL ? 465 : 25;
        switch (config.security) {
            case SSL:
                mail.setSSLOnConnect(true);
                mail.setSslSmtpPort(Integer.toString(port));
                break;
            case STARTTLS:
                mail.setStartTLSEnabled(true);
                mail.setStartTLSRequired(true);
                mail.setSmtpPort(port);
                break;
            case PLAIN:
                mail.setSmtpPort(port);
        }

        if (!(config.username.isEmpty() || config.password.isEmpty())) {
            mail.setAuthentication(config.username, config.password);
        }

        Session localSession;
        try {
            localSession = mail.getMailSession();
            localSession.getTransport().connect();
        } catch (EmailException | MessagingException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        this.session = localSession;
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        Session localSession = session;
        if (localSession != null) {
            try {
                localSession.getTransport().close();
            } catch (MessagingException ignore) {
            }
            session = null;
        }

    }

    /**
     * use this server to send a mail
     *
     * @param mail the Email that needs to be sent
     * @return true if successful, false if failed
     */
    public boolean sendMail(Email mail) {
        Session localSession = session;
        if (localSession == null) {
            logger.warn("Thing {} is not ONLINE, can't send mail", thing.getUID());
            return false;
        }

        mail.setMailSession(localSession);

        try {
            if (mail.getFromAddress() == null && !sender.isEmpty()) {
                mail.setFrom(sender);
            } else {
                logger.warn("No sender defined, can't send mail");
                return false;
            }
            mail.buildMimeMessage();

            // fix command map not available
            DataHandler dataHandler = mail.getMimeMessage().getDataHandler();
            dataHandler.setCommandMap(commandMap);
            try {
                DataSource dataSource = dataHandler.getDataSource();
                Field dataField = dataSource.getClass().getDeclaredField("data");
                dataField.setAccessible(true);
                Object data = dataField.get(dataSource);
                if (data instanceof MimeMultipart mimeMultipart) {
                    for (int i = 0; i < mimeMultipart.getCount(); i++) {
                        Part mimePart = mimeMultipart.getBodyPart(i);
                        mimePart.getDataHandler().setCommandMap(commandMap);
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }

            mail.sendMimeMessage();
        } catch (MessagingException | EmailException e) {
            Throwable cause = e.getCause();
            logger.warn("{}", cause != null ? cause.toString() : e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SendMailActions.class);
    }
}
