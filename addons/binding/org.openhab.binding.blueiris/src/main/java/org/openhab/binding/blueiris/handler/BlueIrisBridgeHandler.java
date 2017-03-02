/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blueiris.handler;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.blueiris.BlueIrisBindingConstants;
import org.openhab.binding.blueiris.internal.config.Config;
import org.openhab.binding.blueiris.internal.control.Connection;
import org.openhab.binding.blueiris.internal.control.ConnectionListener;
import org.openhab.binding.blueiris.internal.data.CamListReply;
import org.openhab.binding.blueiris.internal.data.CamListRequest;
import org.openhab.binding.blueiris.internal.data.LoginReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * The {@link BlueIrisBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Bennett - Initial contribution
 */
public class BlueIrisBridgeHandler extends BaseBridgeHandler implements ConnectionListener {
    private Logger logger = LoggerFactory.getLogger(BlueIrisBridgeHandler.class);
    private Connection connection;
    private List<BridgeListener> listeners = Lists.newArrayList();
    private Thread pollingThread;
    private boolean running;

    public BlueIrisBridgeHandler(Bridge thing) {
        super(thing);
        logger.error("Init bridge is {}", thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Reading login stuff");
        Config config = getConfigAs(Config.class);
        try {
            this.connection = new Connection(config);
            this.connection.addListener(this);

            logger.info("Initialized the blue iris bridge handler");
            if (this.connection.initialize()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "Sending login request to blue iris");
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to setup the md5 pieces", e);
        }
        this.running = true;
        this.pollingThread = new Thread(new PollingThread());
        this.pollingThread.start();
    }

    @Override
    public void dispose() {
        this.connection.removeListener(this);
        this.connection.dispose();
        this.running = false;
        this.pollingThread.interrupt();
        super.dispose();
    }

    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public void onLogin(LoginReply loginReply) {
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Login details");
    }

    @Override
    public void onCamList(CamListReply camListReply) {
        for (BridgeListener listener : listeners) {
            listener.onCamList(camListReply);
        }
        logger.error("Bridge is {}", getThing());
        List<Thing> things = getThing().getThings();
        for (CamListReply.Data data : camListReply.getCameras()) {
            for (Thing thing : things) {
                if (thing.getProperties().get(BlueIrisBindingConstants.PROPERTY_SHORT_NAME)
                        .equals(data.getOptionValue())) {
                    BlueIrisCameraHandler cameraHandler = (BlueIrisCameraHandler) thing.getHandler();
                    cameraHandler.onCamUpdated(data);
                }
            }
        }
    }

    public void addListener(BridgeListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(BridgeListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void onFailedToLogin() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Failed to login");
    }

    /**
     * Thread to handle the polling.
     */
    private class PollingThread implements Runnable {

        @Override
        public void run() {
            while (running) {
                long pollInterval = getConfigAs(Config.class).pollInterval * 1000L;
                if (pollInterval <= 0) {
                    pollInterval = 5000L;
                }
                try {
                    Thread.sleep(pollInterval);
                } catch (InterruptedException e) {
                    if (!running) {
                        return;
                    }
                }
                CamListRequest request = new CamListRequest();
                if (connection.sendCommand(request)) {
                    onCamList(request.getCamListReply());
                }
            }
        }

    }
}
