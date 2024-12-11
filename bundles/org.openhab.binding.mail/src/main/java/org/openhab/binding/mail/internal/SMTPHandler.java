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
package org.openhab.binding.mail.internal;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.PatchedMailcapCommandMap;
import javax.mail.AuthenticationFailedException;
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

        scheduler.execute(this::checkConnection);
    }

    private @Nullable Session checkConnection() {
        Session localSession = this.session;
        if (localSession != null) {
            return localSession;
        }

        SMTPConfig config = getConfigAs(SMTPConfig.class);
        if (config.sender instanceof String confSender) {
            this.sender = confSender;
        }

        Email mail = new SimpleEmail();
        mail.setHostName(config.hostname);
        if (!(config.username.isEmpty() || config.password.isEmpty())) {
            mail.setAuthentication(config.username, config.password);
        }

        int port = config.port != 0 ? config.port : config.security == ServerSecurity.SSL ? 465 : 25;
        switch (config.security) {
            case SSL -> {
                mail.setSSLOnConnect(true);
                mail.setSslSmtpPort(Integer.toString(port));
            }
            case STARTTLS -> {
                mail.setStartTLSEnabled(true);
                mail.setStartTLSRequired(true);
                mail.setSmtpPort(port);
            }
            case PLAIN -> mail.setSmtpPort(port);
        }

        try {
            localSession = mail.getMailSession();
            localSession.getTransport().connect();
        } catch (AuthenticationFailedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return null;
        } catch (EmailException | MessagingException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return null;
        }

        this.session = localSession;
        updateStatus(ThingStatus.ONLINE);
        return localSession;
    }

    @Override
    public void dispose() {
        if (session instanceof Session localSession) {
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
        if (checkConnection() instanceof Session localSession) {
            mail.setMailSession(localSession);

            try {
                if (mail.getFromAddress() == null) {
                    mail.setFrom(sender);
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
                return true;
            } catch (MessagingException | EmailException e) {
                logger.warn("{}", e.getCause() instanceof Throwable cause ? cause.toString() : e.getMessage());
            }
        } else {
            logger.warn("Thing {} is not ONLINE, can't send mail", thing.getUID());
        }
        return false;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SendMailActions.class);
    }
}
