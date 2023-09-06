/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.yamahareceiver.internal.handler;

import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.yamahareceiver.internal.config.YamahaBridgeConfig;
import org.openhab.binding.yamahareceiver.internal.discovery.ZoneDiscoveryService;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.ConnectionStateListener;
import org.openhab.binding.yamahareceiver.internal.protocol.DeviceInformation;
import org.openhab.binding.yamahareceiver.internal.protocol.InputConverter;
import org.openhab.binding.yamahareceiver.internal.protocol.ProtocolFactory;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.protocol.SystemControl;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLProtocolFactory;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlState;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlStateListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private YamahaBridgeConfig bridgeConfig;
    private InputConverter inputConverter;

    private ScheduledFuture<?> refreshTimer;
    private ZoneDiscoveryService zoneDiscoveryService;

    private final DeviceInformationState deviceInformationState = new DeviceInformationState();

    private SystemControl systemControl;
    private SystemControlState systemControlState = new SystemControlState();

    private final CountDownLatch loadingDone = new CountDownLatch(1);

    private boolean disposed = false;

    private ProtocolFactory protocolFactory;
    private AbstractConnection connection;

    public YamahaBridgeHandler(Bridge bridge) {
        super(bridge);
        protocolFactory = new XMLProtocolFactory();
    }

    /**
     * Return the input mapping converter
     */
    public InputConverter getInputConverter() {
        return inputConverter;
    }

    /**
     * @return Return the protocol communication object. This may be null
     *         if the bridge is offline.
     */
    public AbstractConnection getConnection() {
        return connection;
    }

    /**
     * Gets the current protocol factory.
     *
     * @return
     */
    public ProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    /**
     * Sets the current protocol factory.
     *
     * @param protocolFactory
     */
    public void setProtocolFactory(ProtocolFactory protocolFactory) {
        this.protocolFactory = protocolFactory;
    }

    @Override
    public void dispose() {
        cancelRefreshTimer();

        super.dispose();
        disposed = true;
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
    public YamahaBridgeConfig getConfiguration() {
        return bridgeConfig;
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
                case CHANNEL_POWER:
                    systemControl.setPower(((OnOffType) command) == OnOffType.ON);
                    break;
                case CHANNEL_PARTY_MODE:
                    systemControl.setPartyMode(((OnOffType) command) == OnOffType.ON);
                    break;
                case CHANNEL_PARTY_MODE_MUTE:
                    systemControl.setPartyModeMute(((OnOffType) command) == OnOffType.ON);
                    break;
                case CHANNEL_PARTY_MODE_VOLUME:
                    if (command instanceof IncreaseDecreaseType) {
                        systemControl
                                .setPartyModeVolume(((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE);
                    } else {
                        logger.warn("Only {} and {} commands are supported for {}", IncreaseDecreaseType.DECREASE,
                                IncreaseDecreaseType.DECREASE, id);
                    }
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
            case CHANNEL_POWER:
                updateState(channelUID, systemControlState.power ? OnOffType.ON : OnOffType.OFF);
                break;
            case CHANNEL_PARTY_MODE:
                updateState(channelUID, systemControlState.partyMode ? OnOffType.ON : OnOffType.OFF);
                break;
            case CHANNEL_PARTY_MODE_MUTE:
            case CHANNEL_PARTY_MODE_VOLUME:
                // no state updates available
                break;
            default:
                logger.warn("Channel refresh for {} not implemented!", channelUID.getId());
        }
    }

    /**
     * Sets up a refresh timer (using the scheduler) with the given interval.
     *
     * @param initialWaitTime The delay before the first refresh. Maybe 0 to immediately
     *            initiate a refresh.
     */
    private void setupRefreshTimer(int initialWaitTime) {
        cancelRefreshTimer();
        logger.trace("Setting up refresh timer with fixed delay {} seconds, starting in {} seconds",
                bridgeConfig.getRefreshInterval(), initialWaitTime);
        refreshTimer = scheduler.scheduleWithFixedDelay(() -> updateAllZoneInformation(), initialWaitTime,
                bridgeConfig.getRefreshInterval(), TimeUnit.SECONDS);
    }

    /**
     * Cancels the refresh timer (if one was setup).
     */
    private void cancelRefreshTimer() {
        if (refreshTimer != null) {
            refreshTimer.cancel(false);
            refreshTimer = null;
        }
    }

    /**
     * Periodically and initially called. This must run in another thread, because all update calls are blocking.
     */
    void updateAllZoneInformation() {
        if (disposed) {
            logger.trace("updateAllZoneInformation will be skipped because the bridge is disposed");
            return;
        }

        if (!ensureConnectionInitialized()) {
            // The initialization did not yet happen and the device is still offline (or not reachable)
            return;
        }

        logger.trace("updateAllZoneInformation");
        try {
            // Set power = true before calling systemControl.update(),
            // otherwise the systemControlStateChanged method would call updateAllZoneInformation() again
            systemControlState.power = true;
            systemControl.update();

            updateStatus(ThingStatus.ONLINE);

            Bridge bridge = (Bridge) thing;
            for (Thing thing : bridge.getThings()) {
                YamahaZoneThingHandler handler = (YamahaZoneThingHandler) thing.getHandler();

                // Ensure the handler has been already assigned
                if (handler != null && handler.isCorrectlyInitialized()) {
                    handler.updateZoneInformation();
                }
            }
        } catch (IOException e) {
            systemControlState.invalidate();
            onConnectivityError(e);
            return;
        } catch (ReceivedMessageParseException e) {
            String message = e.getMessage();
            updateProperty(PROPERTY_MENU_ERROR, message != null ? message : "");
            // Some AVRs send unexpected responses. We log parser exceptions therefore.
            logger.debug("Parse error!", e);
        } finally {
            loadingDone.countDown();
        }
    }

    /**
     * We handle the update ourselves to avoid a costly dispose/initialize
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

        bridgeConfig = configurationObject.as(YamahaBridgeConfig.class);
        logger.trace("Update configuration of {} with host '{}' and port {}", getThing().getLabel(),
                bridgeConfig.getHost(), bridgeConfig.getPort());

        Optional<String> host = bridgeConfig.getHostWithPort();
        if (host.isPresent()) {
            connection.setHost(host.get());
            onConnectionCreated(connection);
        }

        inputConverter = protocolFactory.InputConverter(connection, bridgeConfig.getInputMapping());
        setupRefreshTimer(bridgeConfig.getRefreshInterval());
    }

    /**
     * We handle updates of this thing ourself.
     */
    @Override
    public void thingUpdated(Thing thing) {
        this.thing = thing;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(ZoneDiscoveryService.class);
    }

    /**
     * Called by the zone discovery service to let this handler have a reference.
     */
    public void setZoneDiscoveryService(ZoneDiscoveryService s) {
        this.zoneDiscoveryService = s;
    }

    /**
     * Calls createCommunicationObject if the host name is configured correctly.
     */
    @Override
    public void initialize() {
        bridgeConfig = getConfigAs(YamahaBridgeConfig.class);
        logger.trace("Initialize of {} with host '{}' and port {}", getThing().getLabel(), bridgeConfig.getHost(),
                bridgeConfig.getPort());

        Optional<String> host = bridgeConfig.getHostWithPort();
        if (!host.isPresent()) {
            String msg = "Host or port not set. Double check your thing settings.";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            logger.warn(msg);
            return;
        }

        if (zoneDiscoveryService == null) {
            logger.warn("Zone discovery service not ready!");
            return;
        }

        protocolFactory.createConnection(host.get(), this);
    }

    @Override
    public void onConnectionCreated(AbstractConnection connection) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                "Waiting for connection with Yamaha device");

        this.connection = connection;
        this.systemControl = null;

        if (!ensureConnectionInitialized()) {
            logger.warn("Communication error. Please review your Yamaha thing configuration.");
        }

        setupRefreshTimer(0);
    }

    /**
     * Attempts to perform a one-time initialization after a connection is created.
     *
     * @return true if initialization was successful
     */
    private boolean ensureConnectionInitialized() {
        if (systemControl != null) {
            return true;
        }

        logger.trace("Initializing connection");

        try {
            DeviceInformation deviceInformation = protocolFactory.DeviceInformation(connection, deviceInformationState);
            deviceInformation.update();

            updateProperty(PROPERTY_VERSION, deviceInformationState.version);
            updateProperty(PROPERTY_ASSIGNED_NAME, deviceInformationState.name);

            zoneDiscoveryService.publishZones(deviceInformationState, thing.getUID());

            systemControl = protocolFactory.SystemControl(connection, this, deviceInformationState);
            inputConverter = protocolFactory.InputConverter(connection, bridgeConfig.getInputMapping());
        } catch (IOException | ReceivedMessageParseException e) {
            deviceInformationState.invalidate();
            onConnectivityError(e);
            return false;
        }
        return true;
    }

    private void onConnectivityError(Exception e) {
        String description = e.getMessage();
        logger.debug(
                "Communication error. Either the Yamaha thing configuration is invalid or the device is offline. Details: {}",
                description);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, description);
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
