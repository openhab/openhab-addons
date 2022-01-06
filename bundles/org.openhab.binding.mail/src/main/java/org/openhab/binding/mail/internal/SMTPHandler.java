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
package org.openhab.binding.mail.internal;

import java.util.Collection;
import java.util.Collections;

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
 */
@NonNullByDefault
public class SMTPHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SMTPHandler.class);

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
            mail.send();
        } catch (EmailException e) {
            logger.warn("{}", e.getMessage());
            if (e.getCause() != null) {
                logger.warn("{}", e.getCause().toString());
            }
            return false;
        }
        return true;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(SendMailActions.class);
    }
}
