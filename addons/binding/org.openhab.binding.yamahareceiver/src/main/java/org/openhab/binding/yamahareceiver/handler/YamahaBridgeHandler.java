/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.internal.discovery.ZoneDiscoveryService;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.ConnectionStateListener;
import org.openhab.binding.yamahareceiver.internal.protocol.DeviceInformation;
import org.openhab.binding.yamahareceiver.internal.protocol.ProtocolFactory;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.protocol.SystemControl;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlState;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YamahaBridgeHandler} is responsible for fetching basic information about the
 * found AVR and start the zone detection.
 *
 * @author David Graeff - Initial contribution
 */
public class YamahaBridgeHandler extends BaseBridgeHandler
        implements ConnectionStateListener, SystemControlStateListener {
    private Logger logger = LoggerFactory.getLogger(YamahaBridgeHandler.class);
    private int refrehInterval = 60; // Default: Every 1min
    private float relativeVolumeChangeFactor = 0.5f; // Default: 0.5 percent
    private ScheduledFuture<?> refreshTimer;
    private ZoneDiscoveryService zoneDiscoveryService;

    private AbstractConnection connection;
    private SystemControlState systemControlState = new SystemControlState();
    private DeviceInformationState deviceInformationState = new DeviceInformationState();
    private final CountDownLatch loadingDone = new CountDownLatch(1);

    public YamahaBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * Return the relative volume change factor
     */
    public float getRelativeVolumeChangeFactor() {
        return relativeVolumeChangeFactor;
    }

    /**
     * Wait until the loading is complete or the timeout occurred.
     *
     * @param timeoutInMs timeout in milliseconds
     * @return Return true if the initial loading is done. This can either be after all requests have been answered by
     *         the AVR or after an error occurred.
     */
    public boolean waitForLoadingDone(long timeoutInMs) throws InterruptedException {
        return loadingDone.await(timeoutInMs, TimeUnit.MILLISECONDS);
    }

    /**
     * @return Return the protocol communication object. This may be null
     *         if the bridge is offline.
     */
    public AbstractConnection getCommunication() {
        return connection;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (connection == null || deviceInformationState.host == null) {
            return;
        }

        if (command instanceof RefreshType) {
            refreshFromState(channelUID);
            return;
        }

        try {
            // Might be extended in the future, therefore a switch statement
            String id = channelUID.getId();
            switch (id) {
                case YamahaReceiverBindingConstants.CHANNEL_POWER:
                    SystemControl systemControl = ProtocolFactory.SystemControl(connection, this);
                    systemControl.setPower(((OnOffType) command) == OnOffType.ON);
                    break;
                default:
                    logger.warn(
                            "Channel {} not supported on the yamaha device directly! Try with the zone things instead.",
                            id);
            }
        } catch (IOException | ReceivedMessageParseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void refreshFromState(ChannelUID channelUID) {
        // Might be extended in the future, therefore a switch statement
        switch (channelUID.getId()) {
            case YamahaReceiverBindingConstants.CHANNEL_POWER:
                updateState(channelUID, systemControlState.power ? OnOffType.ON : OnOffType.OFF);
                break;
            default:
                logger.error("Channel refresh for {} not implemented!", channelUID.getId());
        }
    }

    /**
     * Sets up a refresh timer (using the scheduler) with the CONFIG_REFRESH interval.
     *
     * @param initialWaitTime The delay before the first refresh. Maybe 0 to immediately
     *            initiate a refresh.
     */
    private void setupRefreshTimer(int initialWaitTime) {
        BigDecimal intervalConfig = (BigDecimal) thing.getConfiguration()
                .get(YamahaReceiverBindingConstants.CONFIG_REFRESH);
        if (intervalConfig != null && intervalConfig.intValue() != refrehInterval) {
            refrehInterval = intervalConfig.intValue();
        }

        if (refreshTimer != null) {
            refreshTimer.cancel(false);
        }
        refreshTimer = scheduler.scheduleWithFixedDelay(() -> updateAllZoneInformation(), initialWaitTime,
                refrehInterval, TimeUnit.SECONDS);
    }

    /**
     * Periodically and initially called. This must run in another thread, because
     * all update calls are blocking.
     */
    void updateAllZoneInformation() {
        logger.trace("updateAllZoneInformation");
        try {
            DeviceInformation deviceInformation = ProtocolFactory.DeviceInformation(connection, deviceInformationState);
            deviceInformation.update();
            zoneDiscoveryService.publishZones(deviceInformationState, thing.getUID());

            SystemControl systemControl = ProtocolFactory.SystemControl(connection, this);
            // Set power = true before calling systemControl.update(),
            // otherwise the systemControlStateChanged method would call updateAllZoneInformation() again
            systemControlState.power = true;
            systemControl.update();

            updateProperty(YamahaReceiverBindingConstants.PROPERTY_VERSION, deviceInformationState.version);
            updateProperty(YamahaReceiverBindingConstants.PROPERTY_ASSIGNED_NAME, deviceInformationState.name);

            updateStatus(ThingStatus.ONLINE);

            Bridge bridge = (Bridge) thing;
            for (Thing thing : bridge.getThings()) {
                YamahaZoneThingHandler handler = (YamahaZoneThingHandler) thing.getHandler();
                handler.setDeviceInformationState(deviceInformationState);
                // If thing still thinks that the bridge is offline, update its status.
                if (thing.getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
                    handler.bridgeStatusChanged(ThingStatusInfoBuilder.create(bridge.getStatus()).build());
                } else if (handler.isCorrectlyInitialized()) {
                    handler.updateZoneInformation();
                }
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            systemControlState.invalidate();
            deviceInformationState.invalidate();
            return;
        } catch (ReceivedMessageParseException e) {
            updateProperty(YamahaReceiverBindingConstants.PROPERTY_MENU_ERROR, e.getMessage());
            // Some AVRs send unexpected responses. We log parser exceptions therefore.
            logger.debug("Parse error!", e);
        } finally {
            loadingDone.countDown();
        }
    }

    /**
     * We handle the update ourself to avoid a costly dispose/initialize
     */
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

        // Check if host configuration has changed
        String hostConfig = (String) thing.getConfiguration().get(YamahaReceiverBindingConstants.CONFIG_HOST_NAME);
        if (hostConfig != null) {
            connection.setHost(hostConfig);
        }

        // Check if refresh configuration has changed
        BigDecimal intervalConfig = (BigDecimal) thing.getConfiguration()
                .get(YamahaReceiverBindingConstants.CONFIG_REFRESH);
        if (intervalConfig != null && intervalConfig.intValue() != refrehInterval) {
            setupRefreshTimer(intervalConfig.intValue());
        }

        // Read the configuration for the relative volume change factor.
        BigDecimal relativeVolumeChangeFactorBD = (BigDecimal) thing.getConfiguration()
                .get(YamahaReceiverBindingConstants.CONFIG_RELVOLUMECHANGE);
        if (relativeVolumeChangeFactorBD != null) {
            relativeVolumeChangeFactor = relativeVolumeChangeFactorBD.floatValue();
        } else {
            relativeVolumeChangeFactor = 0.5f;
        }
    }

    /**
     * We handle updates of this thing ourself.
     */
    @Override
    public void thingUpdated(Thing thing) {
        this.thing = thing;
    }

    /**
     * Calls createCommunicationObject if the host name is configured correctly.
     */
    @Override
    public void initialize() {
        // Read the configuration for the relative volume change factor.
        BigDecimal relativeVolumeChangeFactorBD = (BigDecimal) thing.getConfiguration()
                .get(YamahaReceiverBindingConstants.CONFIG_RELVOLUMECHANGE);
        if (relativeVolumeChangeFactorBD != null) {
            relativeVolumeChangeFactor = relativeVolumeChangeFactorBD.floatValue();
        }

        String host = (String) thing.getConfiguration().get(YamahaReceiverBindingConstants.CONFIG_HOST_NAME);
        BigDecimal port = (BigDecimal) thing.getConfiguration().get(YamahaReceiverBindingConstants.CONFIG_HOST_PORT);

        if (StringUtils.isEmpty(host) || port == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname or port not set!");
            return;
        }

        zoneDiscoveryService = new ZoneDiscoveryService(bundleContext);

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for data");
        ProtocolFactory.createConnection(host + ":" + String.valueOf(port.intValue()), this);
    }

    @Override
    public void connectionFailed(String host, Throwable throwable) {
        if (throwable != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, throwable.getMessage());
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
        this.connection = null;
    }

    @Override
    public void connectionEstablished(AbstractConnection connection) {
        this.connection = connection;
        setupRefreshTimer(0);
    }

    @Override
    public void systemControlStateChanged(SystemControlState msg) {
        // If the device was off and now turns on, we trigger a refresh of all zone things.
        // The user might have renamed some of the inputs etc.
        boolean needsCompleteRefresh = msg.power && !systemControlState.power;
        systemControlState = msg;
        updateState(YamahaReceiverBindingConstants.CHANNEL_POWER,
                systemControlState.power ? OnOffType.ON : OnOffType.OFF);
        if (needsCompleteRefresh) {
            updateAllZoneInformation();
        }
    }
}
