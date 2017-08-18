/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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

import org.eclipse.smarthome.config.core.Configuration;
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
import org.openhab.binding.network.internal.PresenceDetection;
import org.openhab.binding.network.internal.PresenceDetection.PingMethod;
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

    boolean allowSystemPings;
    boolean allowDHCPlisten;
    String arpPingToolPath;
    final boolean isTCPServiceDevice;

    // How many retries before a device is deemed offline
    int retries = 1;
    // Retry counter. Will be reset as soon as a device presence detection succeed.
    private int retryCounter = 0;
    private int cacheDeviceStateTimeInMS;

    /**
     * Do not call this directly, but use the {@see NetworkHandlerBuilder} instead.
     */
    NetworkHandler(Thing thing, boolean isTCPServiceDevice, boolean allowSystemPings, boolean allowDHCPlisten,
            int cacheDeviceStateTimeInMS, String arpPingToolPath) {
        super(thing);
        this.isTCPServiceDevice = isTCPServiceDevice;
        this.allowSystemPings = allowSystemPings;
        this.allowDHCPlisten = allowDHCPlisten;
        this.cacheDeviceStateTimeInMS = cacheDeviceStateTimeInMS;
        this.arpPingToolPath = arpPingToolPath;
    }

    private void refreshValue(ChannelUID channelUID) {
        // We are not yet even initialised, don't do anything
        if (!presenceDetection.isAutomaticRefreshing()) {
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_ONLINE:
                presenceDetection.getValue((PresenceDetectionValue value) -> updateState(CHANNEL_ONLINE,
                        value.isReachable() ? OnOffType.ON : OnOffType.OFF));
                break;
            case CHANNEL_LATENCY:
                presenceDetection.getValue((PresenceDetectionValue value) -> updateState(CHANNEL_LATENCY,
                        new DecimalType(value.getLowestLatency())));
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
    }

    @Override
    public void finalDetectionResult(PresenceDetectionValue value) {
        // We do not notify the framework immediately if a device presence detection failed and
        // the user configured retries to be > 1.
        retryCounter = !value.isReachable() ? retryCounter + 1 : 0;

        if (retryCounter >= this.retries) {
            updateState(CHANNEL_ONLINE, OnOffType.OFF);
            updateState(CHANNEL_LATENCY, UnDefType.UNDEF);
            retryCounter = 0;
        }

        if (value.isReachable()) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(presenceDetection.getLastSeen());
            updateState(CHANNEL_LASTSEEN, new DateTimeType(c));
        }

        updateProperty(NetworkBindingConstants.PROPERTY_PRESENCE_DETECTION_TYPE, value.getSuccessfulDetectionTypes());
    }

    int confValueToInt(Object value) {
        return value instanceof java.math.BigDecimal ? ((java.math.BigDecimal) value).intValue()
                : Integer.valueOf((String) value);
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
        this.presenceDetection = presenceDetection;
        Configuration conf = this.getConfig();

        try {
            presenceDetection.setHostname(String.valueOf(conf.get(PARAMETER_HOSTNAME)));
        } catch (UnknownHostException e) {
            logger.error("Configuration for hostname is faulty", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
            return;
        }
        Object value;

        if (isTCPServiceDevice) {
            value = conf.get(PARAMETER_PORT);
            if (value == null) {
                logger.error("You need to configure the port for a service device");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No port configured!");
                return;
            }
            presenceDetection.setServicePorts(Collections.singleton(confValueToInt(value)));
        } else {
            // It does not harm to send an additional UDP packet to a device,
            // therefore we assume all ping devices are iOS devices. If this
            // does not work for all users for some obscure reason, we can make
            // this a thing configuration variable.
            presenceDetection.setIOSDevice(true);
            // Hand over binding configurations to the network service
            presenceDetection.setDHCPsniffing(allowDHCPlisten);
            presenceDetection.setPingMethod(allowSystemPings ? PingMethod.SYSTEM_PING : PingMethod.JAVA_PING);
            presenceDetection.setUseARPping(networkUtils.isNativeARPpingWorking(arpPingToolPath), arpPingToolPath);
        }

        value = conf.get(PARAMETER_RETRY);
        if (value != null) {
            this.retries = confValueToInt(value);
        }

        value = conf.get(PARAMETER_REFRESH_INTERVAL);
        if (value != null) {
            presenceDetection.setRefreshInterval(
                    value instanceof java.math.BigDecimal ? ((java.math.BigDecimal) value).longValue()
                            : Integer.valueOf((String) value));
        }

        value = conf.get(PARAMETER_TIMEOUT);
        if (value != null) {
            presenceDetection.setTimeout(confValueToInt(value));
        }

        // Update properties
        updateProperty(NetworkBindingConstants.PROPERTY_ARP_ON, presenceDetection.isUseARPping() ? "On" : "Off");
        updateProperty(NetworkBindingConstants.PROPERTY_DHCP_ON, presenceDetection.getDhcpState());
        updateProperty(NetworkBindingConstants.PROPERTY_PRESENCE_DETECTION_TYPE, "");
        updateProperty(NetworkBindingConstants.PROPERTY_IOS_WAKEUP, presenceDetection.isIOSdevice() ? "On" : "Off");

        updateStatus(ThingStatus.ONLINE);
        presenceDetection.startAutomaticRefresh(scheduler);
    }

    // Create a new network service and apply all configurations.
    @Override
    public void initialize() {
        initialize(new PresenceDetection(this, cacheDeviceStateTimeInMS));
    }
}
