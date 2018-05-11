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
 * @author David Graeff - Initial contribution
 */
public abstract class AbstractMilightBridgeHandler extends BaseBridgeHandler {
    protected Logger logger = LoggerFactory.getLogger(AbstractMilightBridgeHandler.class);
    protected QueuedSend com;
    protected String bridgeid;
    protected ThingDiscoveryService thingDiscoveryService;
    private ScheduledFuture<?> keepAliveTimer;
    protected int refreshIntervalSec = 5;

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
        Object hostConfigObj = thing.getConfiguration().get(MilightBindingConstants.CONFIG_HOST_NAME);
        String hostConfig = ((hostConfigObj instanceof String) ? (String) hostConfigObj
                : (hostConfigObj instanceof InetAddress) ? ((InetAddress) hostConfigObj).getHostAddress() : null);

        InetAddress addr = null;

        try {
            addr = InetAddress.getByName(hostConfig);
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
            BigDecimal repeatCommand = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_REPEAT);
            if (repeatCommand != null && repeatCommand.intValue() > 1 && repeatCommand.intValue() <= 5) {
                com.setRepeatCommands(repeatCommand.intValue());
            }

            BigDecimal waitBetweenCommands = (BigDecimal) thing.getConfiguration()
                    .get(MilightBindingConstants.CONFIG_WAIT_BETWEEN_COMMANDS);
            if (waitBetweenCommands != null && waitBetweenCommands.intValue() > 1
                    && waitBetweenCommands.intValue() <= 200) {
                com.setDelayBetweenCommands(waitBetweenCommands.intValue());
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

        BigDecimal refreshTime = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_REFRESH_SEC);
        if (refreshTime != null && refreshTime.intValue() != refreshIntervalSec) {
            setupRefreshTimer(refreshTime.intValue());
        }

        // Create a new communication object if the user changed the IP configuration.
        String hostConfig = (String) thing.getConfiguration().get(MilightBindingConstants.CONFIG_HOST_NAME);
        if (hostConfig != null && !hostConfig.equals(com.getAddr().getHostAddress())) {
            try {
                com.setAddress(InetAddress.getByName(hostConfig));
            } catch (UnknownHostException e) {
                logger.warn("Unknown host {}", hostConfig, e);
            }
        }

        // Create a new communication object if the user changed the port configuration.
        BigDecimal portConfig = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_CUSTOM_PORT);
        if (portConfig != null && portConfig.intValue() > 0 && portConfig.intValue() <= 65000
                && portConfig.intValue() != com.getPort()) {
            com.setPort(portConfig.intValue());
        }

        // Create a new communication object if the user changed the bridge ID configuration.
        String idConfig = (String) thing.getConfiguration().get(MilightBindingConstants.CONFIG_ID);
        if (idConfig != null && !idConfig.equals(bridgeid)) {
            connectAndKeepAlive();
        }

        BigDecimal repeatCommand = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_REPEAT);
        if (repeatCommand != null && repeatCommand.intValue() >= 1 && repeatCommand.intValue() <= 5) {
            com.setRepeatCommands(repeatCommand.intValue());
        }

        BigDecimal waitBetweenCommands = (BigDecimal) thing.getConfiguration()
                .get(MilightBindingConstants.CONFIG_WAIT_BETWEEN_COMMANDS);
        if (waitBetweenCommands != null && waitBetweenCommands.intValue() > 1
                && waitBetweenCommands.intValue() <= 200) {
            com.setDelayBetweenCommands(waitBetweenCommands.intValue());
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

    protected void setupRefreshTimer(int refreshIntervalSec) {
        this.refreshIntervalSec = refreshIntervalSec;
        keepAliveTimer = scheduler.scheduleWithFixedDelay(getKeepAliveRunnable(), refreshIntervalSec,
                refreshIntervalSec, TimeUnit.SECONDS);
    }

    /**
     * @return Return the protocol communication object. This may be null
     *         if the bridge is offline.
     */
    public QueuedSend getCommunication() {
        return com;
    }

    protected int getPort(int defaultPort) {
        BigDecimal portConfig = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_CUSTOM_PORT);
        if (portConfig != null && (portConfig.intValue() < 0 || portConfig.intValue() > 65000)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No valid port set!");
            return 0;
        }
        return (portConfig != null) ? portConfig.intValue() : defaultPort;
    }
}
