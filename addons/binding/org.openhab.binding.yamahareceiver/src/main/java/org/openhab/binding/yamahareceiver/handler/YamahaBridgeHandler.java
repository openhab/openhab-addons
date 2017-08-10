/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

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
import org.openhab.binding.yamahareceiver.discovery.ZoneDiscoveryService;
import org.openhab.binding.yamahareceiver.internal.protocol.HttpXMLSendReceive;
import org.openhab.binding.yamahareceiver.internal.protocol.SystemControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * The {@link YamahaBridgeHandler} is responsible for fetching basic information about the
 * found AVR and start the zone detection.
 *
 * @author David Graeff <david.graeff@web.de>
 */
public class YamahaBridgeHandler extends BaseBridgeHandler {
    private Logger logger = LoggerFactory.getLogger(YamahaBridgeHandler.class);
    private int refrehInterval = 60; // Default: Every 1min
    private float relativeVolumeChangeFactor = 0.5f; // Default: 0.5 percent
    private ScheduledFuture<?> refreshTimer;
    private ZoneDiscoveryService zoneDiscoveryService;

    private HttpXMLSendReceive xml;
    private SystemControl.State state = new SystemControl.State();

    public YamahaBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    public float getRelativeVolumeChangeFactor() {
        return relativeVolumeChangeFactor;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (xml == null || state.host == null) {
            return;
        }

        String id = channelUID.getId();

        try {
            if (command instanceof RefreshType) {
                refreshFromState(channelUID);
                return;
            }

            // Might be extended in the future, therefore a switch statement
            switch (id) {
                case YamahaReceiverBindingConstants.CHANNEL_POWER:
                    boolean oldState = state.power;
                    SystemControl basicDeviceInformation = new SystemControl();
                    basicDeviceInformation.setPower(xml, ((OnOffType) command) == OnOffType.ON, state);
                    if (!oldState && state.power) {
                        // If the device was off and now turns on, we trigger a refresh of all zone things.
                        // The user might have renamed some of the inputs etc.
                        updateAllZoneInformation();
                    }
                    break;
                default:
                    logger.error(
                            "Channel {} not supported on the yamaha device directly! Try with the zone things instead.",
                            id);
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void refreshFromState(ChannelUID channelUID) {
        // Might be extended in the future, therefore a switch statement
        switch (channelUID.getId()) {
            case YamahaReceiverBindingConstants.CHANNEL_POWER:
                updateState(channelUID, state.power ? OnOffType.ON : OnOffType.OFF);
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
        if (state == null) {
            return;
        }

        BigDecimal intervalConfig = (BigDecimal) thing.getConfiguration()
                .get(YamahaReceiverBindingConstants.CONFIG_REFRESH);
        if (intervalConfig != null && intervalConfig.intValue() != refrehInterval) {
            refrehInterval = intervalConfig.intValue();
        }

        if (refreshTimer != null) {
            refreshTimer.cancel(false);
        }
        refreshTimer = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateAllZoneInformation();
            }
        }, initialWaitTime, refrehInterval, TimeUnit.SECONDS);
    }

    /**
     * Periodically and initially called. Updates
     */
    void updateAllZoneInformation() {
        try {
            SystemControl basicDeviceInformation = new SystemControl();
            basicDeviceInformation.fetchDeviceInformation(xml, state);
            basicDeviceInformation.fetchPowerInformation(xml, state);

            updateProperty(YamahaReceiverBindingConstants.PROPERTY_VERSION, state.version);
            updateProperty(YamahaReceiverBindingConstants.PROPERTY_ASSIGNED_NAME, state.name);

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            state.invalidate();
            return;
        } catch (ParserConfigurationException | SAXException e) {
            // Some AVRs send unexpected responses. We log parser exceptions therefore.
            logger.debug("Parse error!", e);
        }

        Bridge bridge = (Bridge) thing;
        List<Thing> things = bridge.getThings();
        for (Thing thing : things) {
            YamahaZoneThingHandler handler = (YamahaZoneThingHandler) thing.getHandler();
            // If thing still thinks that the bridge is offline, update its status.
            if (thing.getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
                handler.bridgeStatusChanged(ThingStatusInfoBuilder.create(bridge.getStatus()).build());
            } else {
                handler.updateZoneInformation();
            }
        }
        updateStatus(ThingStatus.ONLINE);
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

        // Check if host configuration has changed
        String hostConfig = (String) thing.getConfiguration().get(YamahaReceiverBindingConstants.CONFIG_HOST_NAME);
        if (hostConfig != null) {
            xml.setHost(hostConfig);
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

        if (host == null || host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname not set!");
            return;
        }

        xml = new HttpXMLSendReceive(host);

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for data");
        setupRefreshTimer(0);

        zoneDiscoveryService = new ZoneDiscoveryService(state, thing.getUID());
        zoneDiscoveryService.start(bundleContext);
        zoneDiscoveryService.detectZones();
    }

    @Override
    public void dispose() {
        if (zoneDiscoveryService != null) {
            zoneDiscoveryService.stop();
            zoneDiscoveryService = null;
        }
    }

    /**
     * @return Return the protocol communication object. This may be null
     *         if the bridge is offline.
     */
    public HttpXMLSendReceive getCommunication() {
        return xml;
    }
}
