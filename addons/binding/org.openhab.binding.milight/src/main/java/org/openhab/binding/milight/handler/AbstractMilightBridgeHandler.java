/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.handler;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.milight.MilightBindingConstants;
import org.openhab.binding.milight.internal.discovery.ThingDiscoveryService;
import org.openhab.binding.milight.internal.protocol.QueuedSend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractMilightBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Graeff <david.graeff@web.de>
 */
public abstract class AbstractMilightBridgeHandler extends BaseBridgeHandler {
    protected Logger logger = LoggerFactory.getLogger(AbstractMilightBridgeHandler.class);
    protected QueuedSend com;
    protected String bridgeid;
    protected ThingDiscoveryService thingDiscoveryService;
    private ScheduledFuture<?> keepAliveTimer;
    protected int refrehIntervalSec = 5;

    public AbstractMilightBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // There is nothing to handle in the bridge handler
    }

    /**
     * Creates a connection and other supportive objects.
     *
     * @param addr
     */
    protected abstract void startConnectAndKeepAlive(InetAddress addr);

    protected abstract Runnable getKeepAliveRunnable();

    private void connectAndKeepAlive() {
        Object host_config_obj = thing.getConfiguration().get(MilightBindingConstants.CONFIG_HOST_NAME);
        String host_config = ((host_config_obj instanceof String) ? (String) host_config_obj
                : (host_config_obj instanceof InetAddress) ? ((InetAddress) host_config_obj).getHostAddress() : null);

        InetAddress addr = null;

        try {
            addr = InetAddress.getByName(host_config);
        } catch (UnknownHostException ignored) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No address known!");
            return;
        }

        bridgeid = (String) thing.getConfiguration().get(MilightBindingConstants.CONFIG_ID);

        // Version 1/2 do not support response messages / detection. We therefore directly call bridgeDetected(addr).
        if (bridgeid == null || bridgeid.length() != 12) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridgeID of length 12!");
            return;
        }

        startConnectAndKeepAlive(addr);

        thingDiscoveryService.start(bundleContext);

        if (com != null) {
            BigDecimal repeat_command = (BigDecimal) thing.getConfiguration()
                    .get(MilightBindingConstants.CONFIG_REPEAT);
            if (repeat_command != null && repeat_command.intValue() > 1 && repeat_command.intValue() <= 5) {
                com.setRepeatCommands(repeat_command.intValue());
            }

            BigDecimal wait_between_commands = (BigDecimal) thing.getConfiguration()
                    .get(MilightBindingConstants.CONFIG_WAIT_BETWEEN_COMMANDS);
            if (wait_between_commands != null && wait_between_commands.intValue() > 1
                    && wait_between_commands.intValue() <= 200) {
                com.setDelayBetweenCommands(wait_between_commands.intValue());
            }
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        if (!isInitialized()) {
            super.handleConfigurationUpdate(configurationParameters);
            return;
        }

        validateConfigurationParameters(configurationParameters);

        // can be overridden by subclasses
        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }

        updateConfiguration(configuration);

        if (com == null) {
            return;
        }

        BigDecimal refresh_time = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_REFRESH_SEC);
        if (refresh_time != null && refresh_time.intValue() != refrehIntervalSec) {
            setupRefreshTimer(refresh_time.intValue());
        }

        // Create a new communication object if the user changed the IP configuration.
        String host_config = (String) thing.getConfiguration().get(MilightBindingConstants.CONFIG_HOST_NAME);
        if (host_config != null && !host_config.equals(com.getAddr().getHostAddress())) {
            try {
                com.setAddress(InetAddress.getByName(host_config));
            } catch (UnknownHostException e) {
                logger.warn("Unknown host {}", host_config, e);
            }
        }

        // Create a new communication object if the user changed the port configuration.
        BigDecimal port_config = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_CUSTOM_PORT);
        if (port_config != null && port_config.intValue() > 0 && port_config.intValue() <= 65000
                && port_config.intValue() != com.getPort()) {
            com.setPort(port_config.intValue());
        }

        // Create a new communication object if the user changed the bridge ID configuration.
        String id_config = (String) thing.getConfiguration().get(MilightBindingConstants.CONFIG_ID);
        if (id_config != null && !id_config.equals(bridgeid)) {
            connectAndKeepAlive();
        }

        BigDecimal repeat_command = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_REPEAT);
        if (repeat_command != null && repeat_command.intValue() >= 1 && repeat_command.intValue() <= 5) {
            com.setRepeatCommands(repeat_command.intValue());
        }

        BigDecimal wait_between_commands = (BigDecimal) thing.getConfiguration()
                .get(MilightBindingConstants.CONFIG_WAIT_BETWEEN_COMMANDS);
        if (wait_between_commands != null && wait_between_commands.intValue() > 1
                && wait_between_commands.intValue() <= 200) {
            com.setDelayBetweenCommands(wait_between_commands.intValue());
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        this.thing = thing;
    }

    /**
     * You need a CONFIG_HOST_NAME and CONFIG_ID for a milight bridge handler to initialize correctly.
     * The ID is a unique 12 character long ASCII based on the bridge MAC address (for example ACCF23A20164)
     * and is send as response for a discovery message.
     */
    @Override
    public void initialize() {
        if (com == null) {
            try {
                com = new QueuedSend();
            } catch (SocketException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
                return;
            }
        }

        thingDiscoveryService = new ThingDiscoveryService(thing.getUID());
        connectAndKeepAlive();
    }

    @Override
    public void dispose() {
        if (keepAliveTimer != null) {
            keepAliveTimer.cancel(true);
            keepAliveTimer = null;
        }

        if (thingDiscoveryService != null) {
            thingDiscoveryService.stop();
        }

        if (com != null) {
            com.dispose();
        }

    }

    protected void setupRefreshTimer(int refrehIntervalSec) {
        this.refrehIntervalSec = refrehIntervalSec;
        keepAliveTimer = scheduler.scheduleWithFixedDelay(getKeepAliveRunnable(), refrehIntervalSec, refrehIntervalSec,
                TimeUnit.SECONDS);
    }

    /**
     * @return Return the protocol communication object. This may be null
     *         if the bridge is offline.
     */
    public QueuedSend getCommunication() {
        return com;
    }

    protected int getPort(int default_port) {
        BigDecimal port_config = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_CUSTOM_PORT);
        if (port_config != null && (port_config.intValue() < 0 || port_config.intValue() > 65000)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No valid port set!");
            return 0;
        }
        return (port_config != null) ? port_config.intValue() : default_port;
    }
}
