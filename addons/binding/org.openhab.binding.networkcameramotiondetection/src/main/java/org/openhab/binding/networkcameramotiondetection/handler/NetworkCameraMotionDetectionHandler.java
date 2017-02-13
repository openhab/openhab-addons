/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.networkcameramotiondetection.handler;

import static org.openhab.binding.networkcameramotiondetection.NetworkCameraMotionDetectionBindingConstants.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.networkcameramotiondetection.internal.config.NetworkCameraMotionDetectionConfig;
import org.openhab.binding.networkcameramotiondetection.internal.ftp.FtpServer;
import org.openhab.binding.networkcameramotiondetection.internal.ftp.FtpServerEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetworkCameraMotionDetectionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class NetworkCameraMotionDetectionHandler extends BaseThingHandler implements FtpServerEventListener {

    private Logger logger = LoggerFactory.getLogger(NetworkCameraMotionDetectionHandler.class);

    private NetworkCameraMotionDetectionConfig configuration;
    private FtpServer ftpServer;

    public NetworkCameraMotionDetectionHandler(Thing thing, FtpServer ftpServer) {
        super(thing);
        this.ftpServer = ftpServer;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand for channel {}: {}", channelUID.getId(), command.toString());
        logger.debug("Command sending not supported by this binding");

        switch (channelUID.getId()) {
            case IMAGE:
                if (command.equals(RefreshType.REFRESH)) {
                    ftpServer.printStats();
                }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for Network Camera Motion Detection binding");
        configuration = getConfigAs(NetworkCameraMotionDetectionConfig.class);
        logger.info("Using configuration: {}", configuration.toString());

        ftpServer.addEventListener(this);
        ftpServer.addAuthenticationCredentials(configuration.userName, configuration.password);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        ftpServer.removeAuthenticationCredentials(configuration.userName);
        ftpServer.removeEventListener(this);
    }

    @Override
    public void fileReceived(String userName, byte[] data) {
        if (configuration.userName.equals(userName)) {
            updateStatus(ThingStatus.ONLINE);
            updateState(IMAGE, new RawType(data));
            updateState(MOTION, OnOffType.ON);
            triggerChannel(MOTION_TRIGGER, "MOTION_DETECTED");
        }
    }
}
