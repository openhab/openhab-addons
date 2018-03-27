/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.handler;

import static org.openhab.binding.network.NetworkBindingConstants.*;

import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.network.NetworkBindingConstants;
import org.openhab.binding.network.internal.NetworkBindingConfiguration;
import org.openhab.binding.network.internal.NetworkHandlerConfiguration;
import org.openhab.binding.network.internal.PresenceDetection;
import org.openhab.binding.network.internal.PresenceDetectionListener;
import org.openhab.binding.network.internal.PresenceDetectionValue;
import org.openhab.binding.network.internal.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler is handling the CHANNEL_ONLINE (boolean) and CHANNEL_TIME (time in ms)
 * commands and is starting a {@see NetworkService} instance for the configured device.
 *
 * @author Marc Mettke
 * @author David Graeff
 */
public class NetworkHandler extends BaseThingHandler implements PresenceDetectionListener {
    private final Logger logger = LoggerFactory.getLogger(NetworkHandler.class);
    private PresenceDetection presenceDetection;
    NetworkUtils networkUtils = new NetworkUtils();

    private boolean isTCPServiceDevice;
    private NetworkBindingConfiguration configuration;

    // How many retries before a device is deemed offline
    int retries;
    // Retry counter. Will be reset as soon as a device presence detection succeed.
    private int retryCounter = 0;
    private NetworkHandlerConfiguration handlerConfiguration;

    /**
     * Do not call this directly, but use the {@see NetworkHandlerBuilder} instead.
     */
    public NetworkHandler(@NonNull Thing thing, boolean isTCPServiceDevice,
            @NonNull NetworkBindingConfiguration configuration) {
        super(thing);
        this.isTCPServiceDevice = isTCPServiceDevice;
        this.configuration = configuration;
    }

    private void refreshValue(ChannelUID channelUID) {
        // We are not yet even initialised, don't do anything
        if (!presenceDetection.isAutomaticRefreshing()) {
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_ONLINE:
                presenceDetection.getValue(
                        value -> updateState(CHANNEL_ONLINE, value.isReachable() ? OnOffType.ON : OnOffType.OFF));
                break;
            case CHANNEL_LATENCY:
            case CHANNEL_DEPRECATED_TIME:
                presenceDetection.getValue(value -> {
                    updateState(CHANNEL_LATENCY, new DecimalType(value.getLowestLatency()));
                    updateState(CHANNEL_DEPRECATED_TIME, new DecimalType(value.getLowestLatency()));
                });
                break;
            case CHANNEL_LASTSEEN:
                if (presenceDetection.getLastSeen() > 0) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(presenceDetection.getLastSeen());
                    updateState(CHANNEL_LASTSEEN, new DateTimeType(c));
                } else {
                    updateState(CHANNEL_LASTSEEN, UnDefType.UNDEF);
                }
                break;
            default:
                logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                break;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshValue(channelUID);
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    @Override
    public void partialDetectionResult(PresenceDetectionValue value) {
        updateState(CHANNEL_ONLINE, OnOffType.ON);
        updateState(CHANNEL_LATENCY, new DecimalType(value.getLowestLatency()));
        updateState(CHANNEL_DEPRECATED_TIME, new DecimalType(value.getLowestLatency()));
    }

    @Override
    public void finalDetectionResult(PresenceDetectionValue value) {
        // We do not notify the framework immediately if a device presence detection failed and
        // the user configured retries to be > 1.
        retryCounter = !value.isReachable() ? retryCounter + 1 : 0;

        if (retryCounter >= this.retries) {
            updateState(CHANNEL_ONLINE, OnOffType.OFF);
            updateState(CHANNEL_LATENCY, UnDefType.UNDEF);
            updateState(CHANNEL_DEPRECATED_TIME, UnDefType.UNDEF);
            retryCounter = 0;
        }

        if (value.isReachable()) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(presenceDetection.getLastSeen());
            updateState(CHANNEL_LASTSEEN, new DateTimeType(c));
        }

        updateProperty(NetworkBindingConstants.PROPERTY_PRESENCE_DETECTION_TYPE, value.getSuccessfulDetectionTypes());
    }

    @Override
    public void dispose() {
        presenceDetection.stopAutomaticRefresh();
        presenceDetection = null;
    }

    /**
     * Initialize with a presenceDetection object.
     * Used by testing for injecting.
     */
    void initialize(PresenceDetection presenceDetection) {
        handlerConfiguration = getConfigAs(NetworkHandlerConfiguration.class);
        this.presenceDetection = presenceDetection;

        try {
            presenceDetection.setHostname(handlerConfiguration.hostname);
        } catch (UnknownHostException e) {
            logger.error("Configuration for hostname is faulty", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
            return;
        }

        if (isTCPServiceDevice) {
            if (handlerConfiguration.port == null) {
                logger.error("You need to configure the port for a service device");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No port configured!");
                return;
            }
            presenceDetection.setServicePorts(Collections.singleton(handlerConfiguration.port.intValue()));
        } else {
            // It does not harm to send an additional UDP packet to a device,
            // therefore we assume all ping devices are iOS devices. If this
            // does not work for all users for some obscure reason, we can make
            // this a thing configuration variable.
            presenceDetection.setIOSDevice(true);
            // Hand over binding configurations to the network service
            presenceDetection.setUseDhcpSniffing(configuration.allowDHCPlisten);
            presenceDetection.setUseIcmpPing(configuration.allowSystemPings);
            presenceDetection.setUseArpPing(true, configuration.arpPingToolPath);
        }

        this.retries = handlerConfiguration.retry.intValue();
        presenceDetection.setRefreshInterval(handlerConfiguration.refreshInterval.longValue());
        presenceDetection.setTimeout(handlerConfiguration.timeout.intValue());

        updateStatus(ThingStatus.ONLINE);
        presenceDetection.startAutomaticRefresh(scheduler);

        // Update properties (after startAutomaticRefresh, to get the correct dhcp state)
        updateProperty(NetworkBindingConstants.PROPERTY_ARP_STATE,
                presenceDetection.arpPingMethod() != null ? presenceDetection.arpPingMethod().name() : "Disabled");
        updateProperty(NetworkBindingConstants.PROPERTY_ICMP_STATE,
                presenceDetection.getPingMethod() != null ? presenceDetection.getPingMethod().name() : "Disabled");
        updateProperty(NetworkBindingConstants.PROPERTY_PRESENCE_DETECTION_TYPE, "");
        updateProperty(NetworkBindingConstants.PROPERTY_IOS_WAKEUP, presenceDetection.isIOSdevice() ? "On" : "Off");
        updateProperty(NetworkBindingConstants.PROPERTY_DHCP_STATE, presenceDetection.getDhcpState());
    }

    // Create a new network service and apply all configurations.
    @Override
    public void initialize() {
        initialize(new PresenceDetection(this, configuration.cacheDeviceStateTimeInMS.intValue()));
    }

    /**
     * Returns true if this handler is for a TCP service device.
     */
    public boolean isTCPServiceDevice() {
        return isTCPServiceDevice;
    }
}
