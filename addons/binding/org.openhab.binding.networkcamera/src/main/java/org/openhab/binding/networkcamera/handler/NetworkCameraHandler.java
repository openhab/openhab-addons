/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.networkcamera.handler;

import static org.openhab.binding.networkcamera.NetworkCameraBindingConstants.*;

import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.networkcamera.internal.config.NetworkCameraConfig;
import org.openhab.binding.networkcamera.internal.ftp.FtpServer;
import org.openhab.binding.networkcamera.internal.ftp.FtpServerEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetworkCameraHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class NetworkCameraHandler extends BaseThingHandler implements FtpServerEventListener {

    private Logger logger = LoggerFactory.getLogger(NetworkCameraHandler.class);

    private NetworkCameraConfig configuration;
    private FtpServer ftpServer;

    public NetworkCameraHandler(Thing thing, FtpServer ftpServer) {
        super(thing);
        this.ftpServer = ftpServer;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand for channel {}: {}", channelUID.getId(), command.toString());
        logger.debug("Command sending not supported by this binding");

        if (command.equals(RefreshType.REFRESH)) {
            ftpServer.printStats();
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for Network Camera Binding");
        configuration = getConfigAs(NetworkCameraConfig.class);
        logger.debug("Using configuration: {}", configuration.toString());

        ftpServer.addEventListener(this);
        try {
            ftpServer.addAuthenticationCredentials(configuration.userName, configuration.password);
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }

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
            updateState(IMAGE, new RawType(data, guessMimeTypeFromData(data)));
            triggerChannel(MOTION_TRIGGER, "MOTION_DETECTED");
        }
    }

    private String guessMimeTypeFromData(byte[] data) {
        String mimeType = HttpUtil.guessContentTypeFromData(data);
        logger.debug("Mime type guess from content: {}", mimeType);
        if (mimeType == null) {
            mimeType = RawType.DEFAULT_MIME_TYPE;
        }
        logger.debug("Mime type: {}", mimeType);
        return mimeType;
    }
}
