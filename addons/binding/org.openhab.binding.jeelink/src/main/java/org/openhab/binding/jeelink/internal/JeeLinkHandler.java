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
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern READING_P = Pattern.compile("^OK\\s+([0-9]+)(?:\\s+([0-9]+))+$");

    private final Logger logger = LoggerFactory.getLogger(JeeLinkHandler.class);

    private JeeLinkConnection connection;
    private Map<String, JeeLinkReadingConverter<?>> sensorTypeConvertersMap = new HashMap<>();
    private Map<Class<?>, List<ReadingHandler<? extends Reading>>> readingClassHandlerMap = new HashMap<>();

    private final AtomicReference<ReadingHandler<Reading>> discoveryHandler = new AtomicReference<>();

    private AtomicBoolean connectionInitialized = new AtomicBoolean(false);
    private ScheduledFuture<?> connectJob;
    private ScheduledFuture<?> initJob;

    public JeeLinkHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        JeeLinkConfig cfg = getConfig().as(JeeLinkConfig.class);

        try {
            connection = AbstractJeeLinkConnection.createFor(cfg, scheduler, this);
            connection.openConnection();
        } catch (java.net.ConnectException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void connectionOpened() {
        logger.debug("Connection to port {} opened.", connection.getPort());

        updateStatus(ThingStatus.ONLINE);

        if (connectJob != null) {
            logger.debug("Connection to port {} established. Reconnect cancelled.", connection.getPort());
            connectJob.cancel(true);
            connectJob = null;
        }

        JeeLinkConfig cfg = getConfig().as(JeeLinkConfig.class);
        initJob = scheduler.schedule(() -> {
            intializeConnection();
        }, cfg.initDelay, TimeUnit.SECONDS);

        logger.debug("Init commands scheduled in {} seconds.", cfg.initDelay);
    }

    @Override
    public void connectionClosed() {
        logger.debug("Connection to port {} closed.", connection.getPort());

        updateStatus(ThingStatus.OFFLINE);
        connectionInitialized.set(false);

        if (initJob != null) {
            initJob.cancel(true);
        }
    }

    @Override
    public void connectionAborted(String cause) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, cause);

        if (initJob != null) {
            initJob.cancel(true);
        }
        connectionInitialized.set(false);

        connectJob = scheduler.schedule(() -> {
            connection.openConnection();
        }, 10, TimeUnit.SECONDS);
        logger.debug("Connection to port {} aborted ({}). Reconnect scheduled.", connection.getPort(), cause);
    }

    public void addReadingHandler(ReadingHandler<? extends Reading> h) {
        synchronized (readingClassHandlerMap) {
            List<ReadingHandler<? extends Reading>> handlers = readingClassHandlerMap.get(h.getReadingClass());
            if (handlers == null) {
                handlers = new ArrayList<>();
                readingClassHandlerMap.put(h.getReadingClass(), handlers);
            }

            if (!handlers.contains(h)) {
                logger.debug("Adding reading handler for class {}: {}", h.getReadingClass(), h);

                handlers.add(h);
            }
        }
    }

    public void removeReadingHandler(ReadingHandler<? extends Reading> h) {
        synchronized (readingClassHandlerMap) {
            List<ReadingHandler<? extends Reading>> handlers = readingClassHandlerMap.get(h.getReadingClass());
            if (handlers != null) {
                logger.debug("Removing reading handler for class {}: {}", h.getReadingClass(), h);
                handlers.remove(h);

                if (handlers.isEmpty()) {
                    readingClassHandlerMap.remove(h.getReadingClass());
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUid, Command command) {
    }

    @Override
    public void handleInput(String input) {
        Matcher matcher = READING_P.matcher(input);
        if (matcher.matches()) {
            intializeConnection();

            String sensorType = matcher.group(1);
            JeeLinkReadingConverter<?> converter;

            synchronized (sensorTypeConvertersMap) {
                converter = sensorTypeConvertersMap.get(sensorType);
                if (converter == null) {
                    converter = SensorDefinition.getConverter(sensorType);

                    if (converter == null) {
                        logger.debug("Missing converter for sensor type {}. Ignoring readings.", sensorType);
                        converter = new IgnoringConverter();
                    } else {
                        logger.debug("Registering converter for sensor type {}: {}", sensorType, converter);
                    }

                    sensorTypeConvertersMap.put(sensorType, converter);
                }
            }

            Reading r = converter.createReading(input);
            if (r != null) {
                ReadingHandler<Reading> d = discoveryHandler.get();
                if (d != null) {
                    d.handleReading(r);
                }

                // propagate to the appropriate sensor handler
                synchronized (readingClassHandlerMap) {
                    List<ReadingHandler<? extends Reading>> handlers = readingClassHandlerMap.get(r.getClass());
                    if (handlers != null) {
                        for (ReadingHandler h : handlers) {
                            h.handleReading(r);
                        }
                    }
                }
            }
        }
    }

    private void intializeConnection() {
        if (!connectionInitialized.getAndSet(true)) {
            JeeLinkConfig cfg = getConfig().as(JeeLinkConfig.class);

            String initCommands = cfg.initCommands;
            if (initCommands != null && !initCommands.trim().isEmpty()) {
                logger.debug("Sending init commands for port {}: {}", connection.getPort(), initCommands);
                connection.sendCommands(initCommands);
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

        synchronized (sensorTypeConvertersMap) {
            sensorTypeConvertersMap.clear();
        }

        super.dispose();
    }

    public JeeLinkConnection getConnection() {
        return connection;
    }

    public void startDiscovery(ReadingHandler<Reading> handler) {
        discoveryHandler.set(handler);
    }

    public void stopDiscovery() {
        discoveryHandler.set(null);
    }
}
