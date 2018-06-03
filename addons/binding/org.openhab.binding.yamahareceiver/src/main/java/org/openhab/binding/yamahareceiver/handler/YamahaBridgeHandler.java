/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.handler;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.internal.config.YamahaBridgeConfiguration;
import org.openhab.binding.yamahareceiver.internal.discovery.ZoneDiscoveryService;
import org.openhab.binding.yamahareceiver.internal.protocol.*;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlState;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.*;

/**
 * The {@link YamahaBridgeHandler} is responsible for fetching basic information about the
 * found AVR and start the zone detection.
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - Input mapping fix, volumeDB fix, better feature detection, added config object
 */
public class YamahaBridgeHandler extends BaseBridgeHandler
        implements ConnectionStateListener, SystemControlStateListener {

    private final Logger logger = LoggerFactory.getLogger(YamahaBridgeHandler.class);

    private YamahaBridgeConfiguration bridgeConfiguration;
    private InputConverter inputConverter;

    private ScheduledFuture<?> refreshTimer;
    private ZoneDiscoveryService zoneDiscoveryService;

    private AbstractConnection connection;
    private final DeviceInformationState deviceInformationState = new DeviceInformationState();
    private SystemControlState systemControlState = new SystemControlState();
    private final CountDownLatch loadingDone = new CountDownLatch(1);

    public YamahaBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * Return the input mapping converter
     */
    public InputConverter getInputConverter() {
        return inputConverter;
    }

    /**
     * @return Return the protocol communication object. This may be null
     * if the bridge is offline.
     */
    public AbstractConnection getCommunication() {
        return connection;
    }

    /**
     * Returns the device information
     */
    public DeviceInformationState getDeviceInformationState() {
        return deviceInformationState;
    }

    /**
     * Returns the device configuration
     */
    public YamahaBridgeConfiguration getConfiguration() {
        return bridgeConfiguration;
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
                    logger.warn("Channel {} not supported on the yamaha device directly! Try with the zone things instead.", id);
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
     * Sets up a refresh timer (using the scheduler) with the given interval.
     *
     * @param initialWaitTime The delay before the first refresh. Maybe 0 to immediately
     *                        initiate a refresh.
     */
    private void setupRefreshTimer(int initialWaitTime) {
        if (refreshTimer != null) {
            refreshTimer.cancel(false);
        }
        refreshTimer = scheduler.scheduleWithFixedDelay(() -> updateAllZoneInformation(), initialWaitTime, bridgeConfiguration.getRefreshInterval(), TimeUnit.SECONDS);
    }

    /**
     * Periodically and initially called. This must run in another thread, because all update calls are blocking.
     */
    void updateAllZoneInformation() {
        logger.trace("updateAllZoneInformation");
        try {
            SystemControl systemControl = ProtocolFactory.SystemControl(connection, this);
            // Set power = true before calling systemControl.update(),
            // otherwise the systemControlStateChanged method would call updateAllZoneInformation() again
            systemControlState.power = true;
            systemControl.update();

            updateStatus(ThingStatus.ONLINE);

            Bridge bridge = (Bridge) thing;
            for (Thing thing : bridge.getThings()) {
                YamahaZoneThingHandler handler = (YamahaZoneThingHandler) thing.getHandler();

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
            return;
        } catch (ReceivedMessageParseException e) {
            updateProperty(PROPERTY_MENU_ERROR, e.getMessage());
            // Some AVRs send unexpected responses. We log parser exceptions therefore.
            logger.debug("Parse error!", e);
        } finally {
            loadingDone.countDown();
        }
    }

    private void updateDeviceInformation() throws IOException, ReceivedMessageParseException {
        logger.trace("updateDeviceInformation");

        DeviceInformation deviceInformation = ProtocolFactory.DeviceInformation(connection, deviceInformationState);
        deviceInformation.update();

        updateProperty(PROPERTY_VERSION, deviceInformationState.version);
        updateProperty(PROPERTY_ASSIGNED_NAME, deviceInformationState.name);

        zoneDiscoveryService.publishZones(deviceInformationState, thing.getUID());
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
        Configuration configurationObject = editConfiguration();
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            configurationObject.put(configurationParameter.getKey(), configurationParameter.getValue());
        }

        updateConfiguration(configurationObject);

        bridgeConfiguration = configurationObject.as(YamahaBridgeConfiguration.class);
        logger.trace("Update configuration of {} with host '{}' and port {}", getThing().getLabel(), bridgeConfiguration.getHost(), bridgeConfiguration.getPort());

        Optional<String> host = bridgeConfiguration.getHostWithPort();
        if (host.isPresent()) {
            connection.setHost(host.get());
            connectionEstablished(connection);
        }

        inputConverter = ProtocolFactory.InputConverter(connection, bridgeConfiguration.getInputMapping());
        setupRefreshTimer(bridgeConfiguration.getRefreshInterval());
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
        zoneDiscoveryService = new ZoneDiscoveryService(bundleContext);

        bridgeConfiguration = getConfigAs(YamahaBridgeConfiguration.class);
        logger.trace("Initialize of {} with host '{}' and port {}", getThing().getLabel(), bridgeConfiguration.getHost(), bridgeConfiguration.getPort());

        Optional<String> host = bridgeConfiguration.getHostWithPort();
        if (!host.isPresent()) {
            String msg = "Host or port not set. Double check your thing settings.";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            logger.warn(msg);
            return;
        }

        ProtocolFactory.createConnection(host.get(), this);
        inputConverter = ProtocolFactory.InputConverter(connection, bridgeConfiguration.getInputMapping());
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
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for data");

        this.connection = connection;
        try {
            updateDeviceInformation();
        } catch (IOException | ReceivedMessageParseException e) {
            logger.warn("Communication error. Please review your Yamaha thing configuration.", e);
            deviceInformationState.invalidate();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }
        setupRefreshTimer(0);
    }

    @Override
    public void systemControlStateChanged(SystemControlState msg) {
        // If the device was off and now turns on, we trigger a refresh of all zone things.
        // The user might have renamed some of the inputs etc.
        boolean needsCompleteRefresh = msg.power && !systemControlState.power;
        systemControlState = msg;

        updateState(CHANNEL_POWER, systemControlState.power ? OnOffType.ON : OnOffType.OFF);
        if (needsCompleteRefresh) {
            updateAllZoneInformation();
        }
    }
}
