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
package org.openhab.binding.mail.internal;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.PatchedMailcapCommandMap;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mail.internal.action.SendMailActions;
import org.openhab.binding.mail.internal.config.SMTPConfig;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
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
 */
@NonNullByDefault
public class SMTPHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SMTPHandler.class);
    private final PatchedMailcapCommandMap commandMap = new PatchedMailcapCommandMap();

    private @NonNullByDefault({}) SMTPConfig config;

    public SMTPHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(SMTPConfig.class);

        if (config.port == 0) {
            if (config.security == ServerSecurity.SSL) {
                config.port = 465;
            } else {
                config.port = 25;
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * use this server to send a mail
     *
     * @param mail the Email that needs to be sent
     * @return true if successful, false if failed
     */
    public boolean sendMail(Email mail) {
        try {
            if (mail.getFromAddress() == null) {
                mail.setFrom(config.sender);
            }
            mail.setHostName(config.hostname);
            switch (config.security) {
                case SSL:
                    mail.setSSLOnConnect(true);
                    mail.setSslSmtpPort(config.port.toString());
                    break;
                case STARTTLS:
                    mail.setStartTLSEnabled(true);
                    mail.setStartTLSRequired(true);
                    mail.setSmtpPort(config.port);
                    break;
                case PLAIN:
                    mail.setSmtpPort(config.port);
            }
            if (!config.username.isEmpty() && !config.password.isEmpty()) {
                mail.setAuthenticator(new DefaultAuthenticator(config.username, config.password));
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
                if (data instanceof MimeMultipart) {
                    MimeMultipart mimeMultipart = (MimeMultipart) data;
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
            if (cause != null) {
                logger.warn("{}", cause.toString());
            } else {
                logger.warn("{}", e.getMessage());
            }
            return false;
        }
        return true;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SendMailActions.class);
    }
}
