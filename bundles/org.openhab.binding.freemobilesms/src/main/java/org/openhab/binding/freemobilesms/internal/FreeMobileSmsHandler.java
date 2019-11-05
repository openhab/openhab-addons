/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.freemobilesms.internal;

import static org.openhab.binding.freemobilesms.internal.FreeMobileSmsBindingConstants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freemobilesms.internal.FreeMobileSmsConfiguration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.free.smsapi.Account;
import fr.free.smsapi.Sender;

/**
 * The {@link FreeMobileSmsHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Guilhem Bonnefille - Initial contribution
 */
@NonNullByDefault
public class FreeMobileSmsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FreeMobileSmsHandler.class);

    @Nullable
    private FreeMobileSmsConfiguration config;

    private final Sender sender = new Sender();

    public FreeMobileSmsHandler(Thing thing) {
        super(thing);
    }

    protected void sendMessage(String message) {
      logger.debug("Message to send: {}", message);
      try {
        // Local variable to disable Null annotation locally
        Account account = this.config;
        if (account != null) {
          sender.send(account, message);
        } else {
          logger.error("Failed to send message: configuration not set");
        }
      } catch (IOException e) {
        logger.error("Failed to send message: {}", e.getMessage());
      }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!CHANNEL_MESSAGE.equals(channelUID.getId())) {
            logger.warn("Received command {} for unknown channel: {}", command, channelUID);
            return;
        }
        logger.debug("Message command");
        if (command instanceof StringType) {
            scheduler.execute(() -> {
                sendMessage(command.toString());
            });
        } else {
            logger.warn("Unsupported Command type");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");

        // Check configuration
        config = getConfigAs(FreeMobileSmsConfiguration.class);
        if (config != null && config.user != null && config.password != null) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "Failed to retrieve configuration");
        }

        logger.debug("Finished initializing!");
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(FreeMobileSmsActions.class);
    }
}
