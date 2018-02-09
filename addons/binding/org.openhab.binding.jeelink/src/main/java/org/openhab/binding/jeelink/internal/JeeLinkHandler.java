/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.jeelink.internal.config.JeeLinkConfig;
import org.openhab.binding.jeelink.internal.connection.AbstractJeeLinkConnection;
import org.openhab.binding.jeelink.internal.connection.ConnectionListener;
import org.openhab.binding.jeelink.internal.connection.JeeLinkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a JeeLink USB Receiver thing.
 *
 * @author Volker Bier - Initial contribution
 */
public class JeeLinkHandler extends BaseBridgeHandler implements BridgeHandler, ConnectionListener {
    private final Logger logger = LoggerFactory.getLogger(JeeLinkHandler.class);

    private JeeLinkConnection connection;
    private Map<String, JeeLinkReadingConverter> converters = new HashMap<>();
    private Map<JeeLinkReadingConverter, List<ReadingHandler>> convSensorMap = new HashMap<>();

    private AtomicBoolean connectionInitialized = new AtomicBoolean(false);
    private ScheduledFuture<?> connectJob;

    public JeeLinkHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        JeeLinkConfig cfg = getConfig().as(JeeLinkConfig.class);

        try {
            for (JeeLinkReadingConverter c : SensorDefinition.createConverters(this)) {
                converters.put(c.getSketchName(), c);
            }

            connection = AbstractJeeLinkConnection.createFor(cfg, scheduler, this);
            connection.openConnection();
        } catch (java.net.ConnectException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void connectionOpened() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void connectionAborted(String cause) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, cause);

        connectJob = scheduler.schedule(() -> {
            connection.openConnection();
        }, 10, TimeUnit.SECONDS);
        logger.debug("Connection to port {} aborted ({}). Reconnect scheduled.", connection.getPort(), cause);

        connectionInitialized.set(false);
    }

    public void addReadingHandler(ReadingHandler h) {
        synchronized (convSensorMap) {
            JeeLinkReadingConverter c = converters.get(h.getSketchName());
            List<ReadingHandler> handlers = convSensorMap.get(c);
            if (handlers == null) {
                handlers = new ArrayList<>();
                convSensorMap.put(c, handlers);
            }

            if (!handlers.contains(h)) {
                handlers.add(h);
            }
        }
    }

    public void removeReadingHandler(ReadingHandler h) {
        synchronized (convSensorMap) {
            JeeLinkReadingConverter c = converters.get(h.getSketchName());
            List<ReadingHandler> handlers = convSensorMap.get(c);
            if (handlers != null) {
                handlers.remove(h);

                if (handlers.isEmpty()) {
                    convSensorMap.remove(c);
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUid, Command command) {
    }

    @Override
    public void handleInput(String input) {
        synchronized (convSensorMap) {
            for (JeeLinkReadingConverter c : convSensorMap.keySet()) {
                Reading r = c.createReading(input);
                if (r != null) {
                    if (!connectionInitialized.getAndSet(true)) {
                        JeeLinkConfig cfg = getConfig().as(JeeLinkConfig.class);

                        String initCommands = cfg.initCommands;
                        if (initCommands != null && !initCommands.trim().isEmpty()) {
                            logger.debug("Sending init commands for port {}: {}", connection.getPort(), initCommands);
                            connection.sendInitCommands(initCommands);
                        }
                    }

                    for (ReadingHandler h : convSensorMap.get(c)) {
                        h.handleReading(r);
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        if (connectJob != null) {
            connectJob.cancel(true);
            connectJob = null;
        }

        if (connection != null) {
            connection.closeConnection();
        }

        connectionInitialized.set(false);

        SensorDefinition.disposeConverters(this);
        super.dispose();
    }
}
