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
package org.openhab.binding.jeelink.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openhab.binding.jeelink.internal.config.JeeLinkConfig;
import org.openhab.binding.jeelink.internal.connection.ConnectionListener;
import org.openhab.binding.jeelink.internal.connection.JeeLinkConnection;
import org.openhab.binding.jeelink.internal.connection.JeeLinkSerialConnection;
import org.openhab.binding.jeelink.internal.connection.JeeLinkTcpConnection;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a JeeLink USB Receiver thing.
 *
 * @author Volker Bier - Initial contribution
 */
public class JeeLinkHandler extends BaseBridgeHandler implements BridgeHandler, ConnectionListener {
    private final Logger logger = LoggerFactory.getLogger(JeeLinkHandler.class);

    private final List<JeeLinkReadingConverter<?>> converters = new ArrayList<>();
    private final Map<String, JeeLinkReadingConverter<?>> sensorTypeConvertersMap = new HashMap<>();
    private final Map<Class<?>, Set<ReadingHandler<? extends Reading>>> readingClassHandlerMap = new HashMap<>();
    private final SerialPortManager serialPortManager;

    private JeeLinkConnection connection;
    private AtomicBoolean connectionInitialized = new AtomicBoolean(false);
    private ScheduledFuture<?> connectJob;
    private ScheduledFuture<?> initJob;

    private long lastReadingTime;
    private ScheduledFuture<?> monitorJob;

    public JeeLinkHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        JeeLinkConfig cfg = getConfig().as(JeeLinkConfig.class);

        if (cfg.serialPort != null && cfg.baudRate != null) {
            SerialPortIdentifier serialPortIdentifier = serialPortManager.getIdentifier(cfg.serialPort);
            if (serialPortIdentifier == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Port not found: " + cfg.serialPort);
                return;
            }
            connection = new JeeLinkSerialConnection(serialPortIdentifier, cfg.baudRate, this);
            connection.openConnection();
        } else if (cfg.ipAddress != null && cfg.port != null) {
            connection = new JeeLinkTcpConnection(cfg.ipAddress + ":" + cfg.port, scheduler, this);
            connection.openConnection();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Connection configuration incomplete");
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

        if (cfg.reconnectInterval > 0) {
            monitorJob = scheduler.scheduleWithFixedDelay(new Runnable() {
                private long lastMonitorTime;

                @Override
                public void run() {
                    if (getThing().getStatus() == ThingStatus.ONLINE && lastReadingTime < lastMonitorTime) {
                        logger.debug("Monitoring job for port {} detected missing readings. Triggering reconnect...",
                                connection.getPort());

                        connection.closeConnection();
                        updateStatus(ThingStatus.OFFLINE);

                        connection.openConnection();
                    }
                    lastMonitorTime = System.currentTimeMillis();
                }
            }, cfg.reconnectInterval, cfg.reconnectInterval, TimeUnit.SECONDS);
            logger.debug("Monitoring job started.");
        }
    }

    @Override
    public void connectionClosed() {
        logger.debug("Connection to port {} closed.", connection.getPort());

        updateStatus(ThingStatus.OFFLINE);
        connectionInitialized.set(false);

        if (initJob != null) {
            initJob.cancel(true);
        }
        if (monitorJob != null) {
            monitorJob.cancel(true);
        }
    }

    @Override
    public void connectionAborted(String cause) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, cause);

        if (monitorJob != null) {
            monitorJob.cancel(true);
        }
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
            Set<ReadingHandler<? extends Reading>> handlers = readingClassHandlerMap.get(h.getReadingClass());
            if (handlers == null) {
                handlers = new HashSet<>();

                // this is the first handler for this reading class => also setup converter
                readingClassHandlerMap.put(h.getReadingClass(), handlers);

                if (SensorDefinition.ALL_TYPE.equals(h.getSensorType())) {
                    converters.addAll(SensorDefinition.getDiscoveryConverters());
                } else {
                    JeeLinkReadingConverter<?> c = SensorDefinition.getConverter(h.getSensorType());
                    if (c != null) {
                        converters.add(c);
                        sensorTypeConvertersMap.put(h.getSensorType(), c);
                    }
                }
            }

            if (!handlers.contains(h)) {
                logger.debug("Adding reading handler for class {}: {}", h.getReadingClass(), h);

                handlers.add(h);
            }
        }
    }

    public void removeReadingHandler(ReadingHandler<? extends Reading> h) {
        synchronized (readingClassHandlerMap) {
            Set<ReadingHandler<? extends Reading>> handlers = readingClassHandlerMap.get(h.getReadingClass());
            if (handlers != null) {
                logger.debug("Removing reading handler for class {}: {}", h.getReadingClass(), h);
                handlers.remove(h);

                if (handlers.isEmpty()) {
                    // this was the last handler for this reading class => also remove converter
                    readingClassHandlerMap.remove(h.getReadingClass());

                    if (SensorDefinition.ALL_TYPE.equals(h.getSensorType())) {
                        converters.removeAll(SensorDefinition.getDiscoveryConverters());
                    } else {
                        JeeLinkReadingConverter<?> c = SensorDefinition.getConverter(h.getSensorType());
                        if (c != null) {
                            converters.remove(c);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUid, Command command) {
    }

    @Override
    public void handleInput(String input) {
        lastReadingTime = System.currentTimeMillis();

        // try all associated converters to find the correct one
        for (JeeLinkReadingConverter<?> c : converters) {
            Reading r = c.createReading(input);

            if (r != null) {
                // this converter is responsible
                intializeConnection();

                // propagate to the appropriate sensor handler
                synchronized (readingClassHandlerMap) {
                    Set<ReadingHandler<? extends Reading>> handlers = getAllHandlers(r.getClass());

                    for (ReadingHandler h : handlers) {
                        h.handleReading(r);
                    }
                }

                break;
            }
        }
    }

    private Set<ReadingHandler<? extends Reading>> getAllHandlers(Class<? extends Reading> readingClass) {
        Set<ReadingHandler<? extends Reading>> handlers = new HashSet<>();

        Set<ReadingHandler<? extends Reading>> typeHandlers = readingClassHandlerMap.get(readingClass);
        if (typeHandlers != null) {
            handlers.addAll(typeHandlers);
        }
        Set<ReadingHandler<? extends Reading>> discoveryHandlers = readingClassHandlerMap.get(Reading.class);
        if (discoveryHandlers != null) {
            handlers.addAll(discoveryHandlers);
        }

        return handlers;
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

        super.dispose();
    }

    public JeeLinkConnection getConnection() {
        return connection;
    }
}
