/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.orvibo.handler;

import static org.openhab.binding.orvibo.OrviboBindingConstants.*;

import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tavalin.orvibo.OrviboClient;
import com.github.tavalin.orvibo.devices.AllOne;

/**
 * The {@link AllOneHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Walters/Janis Steder - Initial contribution
 */
public class AllOneHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(AllOneHandler.class);
    private AllOne allone;
    private OrviboClient client;
    private ScheduledFuture<?> subscribeHandler;
    private long refreshInterval = 15;
    private Runnable subscribeTask = new Runnable() {
        @Override
        public void run() {
            if (allone != null) {
                allone.subscribe();
            }
        }
    };

    public AllOneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        configure();
    }

    @Override
    public void dispose() {
        subscribeHandler.cancel(true);
    }

    private void configure() {
        try {
            client = OrviboClient.getInstance();
            String deviceId = thing.getUID().getId();
            allone = client.allOneWithDeviceId(deviceId);
            allone.find();
            subscribeHandler = scheduler.scheduleWithFixedDelay(subscribeTask, 0, refreshInterval, TimeUnit.SECONDS);
            updateStatus(ThingStatus.ONLINE);
        } catch (SocketException ex) {
            logger.error("Error occured while initializing S20 handler: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_ALLONE_LEARN:
                handleLearn(channelUID, command);
                break;
            case CHANNEL_ALLONE_EMIT:
                handleEmit(channelUID, command);
                break;
        }
    }

    private void handleEmit(ChannelUID channelUID, Command command) {
        if (command instanceof StringType) {
            try {
                String filename = command.toString();
                Configuration config = thing.getConfiguration();
                String rootFolder = (String) config.get(CONFIG_PROPERTY_ROOT);
                Path file = Paths.get(rootFolder, filename);
                allone.emit(file);
            } catch (IOException e) {
                logger.error(e.toString());
            }
        } else {
            logger.warn("Received invalid commandType for channel {}", channelUID.getAsString());
        }
    }

    private void handleLearn(ChannelUID channelUID, Command command) {
        if (command instanceof StringType) {
            String filename = command.toString();
            Configuration config = thing.getConfiguration();
            String rootFolder = (String) config.get(CONFIG_PROPERTY_ROOT);
            Path file = Paths.get(rootFolder, filename);
            allone.learn(file);
        } else {
            logger.warn("Received invalid commandType for channel {}", channelUID.getAsString());
        }
    }

}
