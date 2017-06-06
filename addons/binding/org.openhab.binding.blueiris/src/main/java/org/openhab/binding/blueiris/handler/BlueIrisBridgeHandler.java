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
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
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
import org.openhab.binding.blueiris.internal.data.SysConfigReply.Data;
import org.openhab.binding.blueiris.internal.data.SysConfigRequest;
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
    private ScheduledFuture<?> pollingThread;
    private ScheduledFuture<?> sysInfoPollingThread;

    public BlueIrisBridgeHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(BlueIrisBindingConstants.CHANNEL_GLOBAL_SCHEDULE)) {
            SysConfigRequest request = new SysConfigRequest();
            if (command instanceof OnOffType) {
                OnOffType onOff = (OnOffType) command;
                request.setSchedule(onOff == OnOffType.ON);
                sendSysconfigRequest(request);
            }
        }
        if (channelUID.getId().equals(BlueIrisBindingConstants.CHANNEL_WEB_ARCHIVE)) {
            SysConfigRequest request = new SysConfigRequest();
            if (command instanceof OnOffType) {
                OnOffType onOff = (OnOffType) command;
                request.setArchive(onOff == OnOffType.ON);
                sendSysconfigRequest(request);
            }
        }
    }

    private void sendSysconfigRequest(final SysConfigRequest request) {
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (connection.sendCommand(request)) {
                    handleSysconfigData(request.getReply().getData());
                }
            }
        });
        myThread.start();
    }

    private void handleSysconfigData(Data data) {
        Channel chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_GLOBAL_SCHEDULE);
        updateState(chan.getUID(), data.isSchedule() ? OnOffType.ON : OnOffType.OFF);
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_WEB_ARCHIVE);
        updateState(chan.getUID(), data.isArchive() ? OnOffType.ON : OnOffType.OFF);
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
        startAutomaticRefresh(config);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        Config config = getConfigAs(Config.class);
        stopAutomaticRefresh();
        startAutomaticRefresh(config);
    }

    @Override
    public void dispose() {
        this.connection.removeListener(this);
        this.connection.dispose();
        stopAutomaticRefresh();
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

    private synchronized void startAutomaticRefresh(Config config) {
        if (pollingThread == null || pollingThread.isCancelled()) {
            pollingThread = scheduler.scheduleWithFixedDelay(new PollingThread(), 0, config.pollInterval,
                    TimeUnit.SECONDS);
        }
        if (sysInfoPollingThread == null || sysInfoPollingThread.isCancelled()) {
            sysInfoPollingThread = scheduler.scheduleWithFixedDelay(new SysInfoPollingThread(), 0,
                    config.configPollInterval, TimeUnit.MINUTES);

        }
    }

    private synchronized void stopAutomaticRefresh() {
        if (pollingThread != null) {
            pollingThread.cancel(true);
            pollingThread = null;
        }
        if (sysInfoPollingThread != null) {
            sysInfoPollingThread.cancel(true);
            sysInfoPollingThread = null;
        }
    }

    /**
     * Thread to handle the polling.
     */
    private class PollingThread implements Runnable {

        @Override
        public void run() {
            CamListRequest request = new CamListRequest();
            if (connection.sendCommand(request)) {
                onCamList(request.getReply());
            }
        }
    }

    /**
     * Thread to handle the polling.
     */
    private class SysInfoPollingThread implements Runnable {

        @Override
        public void run() {
            SysConfigRequest request = new SysConfigRequest();
            if (connection.sendCommand(request)) {
                handleSysconfigData(request.getReply().getData());
            }
        }
    }
}
