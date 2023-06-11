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
package org.openhab.binding.network.internal.handler;

import static org.openhab.binding.network.internal.NetworkBindingConstants.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.network.internal.NetworkBindingConfiguration;
import org.openhab.binding.network.internal.NetworkBindingConfigurationListener;
import org.openhab.binding.network.internal.NetworkBindingConstants;
import org.openhab.binding.network.internal.NetworkHandlerConfiguration;
import org.openhab.binding.network.internal.PresenceDetection;
import org.openhab.binding.network.internal.PresenceDetectionListener;
import org.openhab.binding.network.internal.PresenceDetectionValue;
import org.openhab.binding.network.internal.WakeOnLanPacketSender;
import org.openhab.binding.network.internal.action.NetworkActions;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler is handling the CHANNEL_ONLINE (boolean) and CHANNEL_TIME (time in ms)
 * commands and is starting a {@see NetworkService} instance for the configured device.
 *
 * @author Marc Mettke - Initial contribution
 * @author David Graeff - Rewritten
 * @author Wouter Born - Add Wake-on-LAN thing action support
 */
@NonNullByDefault
public class NetworkHandler extends BaseThingHandler
        implements PresenceDetectionListener, NetworkBindingConfigurationListener {
    private final Logger logger = LoggerFactory.getLogger(NetworkHandler.class);
    private @NonNullByDefault({}) PresenceDetection presenceDetection;
    private @NonNullByDefault({}) WakeOnLanPacketSender wakeOnLanPacketSender;

    private boolean isTCPServiceDevice;
    private NetworkBindingConfiguration configuration;

    // How many retries before a device is deemed offline
    int retries;
    // Retry counter. Will be reset as soon as a device presence detection succeed.
    private int retryCounter = 0;
    private NetworkHandlerConfiguration handlerConfiguration = new NetworkHandlerConfiguration();

    /**
     * Do not call this directly, but use the {@see NetworkHandlerBuilder} instead.
     */
    public NetworkHandler(Thing thing, boolean isTCPServiceDevice, NetworkBindingConfiguration configuration) {
        super(thing);
        this.isTCPServiceDevice = isTCPServiceDevice;
        this.configuration = configuration;
        this.configuration.addNetworkBindingConfigurationListener(this);
    }

    private void refreshValue(ChannelUID channelUID) {
        // We are not yet even initialized, don't do anything
        if (presenceDetection == null || !presenceDetection.isAutomaticRefreshing()) {
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
                    updateState(CHANNEL_LATENCY,
                            new QuantityType<>(value.getLowestLatency(), MetricPrefix.MILLI(Units.SECOND)));
                    updateState(CHANNEL_DEPRECATED_TIME, new DecimalType(value.getLowestLatency()));
                });
                break;
            case CHANNEL_LASTSEEN:
                if (presenceDetection.getLastSeen() > 0) {
                    Instant instant = Instant.ofEpochMilli(presenceDetection.getLastSeen());
                    updateState(CHANNEL_LASTSEEN, new DateTimeType(
                            ZonedDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId()).withFixedOffsetZone()));
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
        updateState(CHANNEL_LATENCY, new QuantityType<>(value.getLowestLatency(), MetricPrefix.MILLI(Units.SECOND)));
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
            Instant instant = Instant.ofEpochMilli(presenceDetection.getLastSeen());
            updateState(CHANNEL_LASTSEEN, new DateTimeType(
                    ZonedDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId()).withFixedOffsetZone()));
        }

        updateNetworkProperties();
    }

    @Override
    public void dispose() {
        PresenceDetection detection = presenceDetection;
        if (detection != null) {
            detection.stopAutomaticRefresh();
        }
        presenceDetection = null;
    }

    /**
     * Initialize with a presenceDetection object.
     * Used by testing for injecting.
     */
    void initialize(PresenceDetection presenceDetection) {
        handlerConfiguration = getConfigAs(NetworkHandlerConfiguration.class);

        this.presenceDetection = presenceDetection;
        presenceDetection.setHostname(handlerConfiguration.hostname);
        presenceDetection.setPreferResponseTimeAsLatency(configuration.preferResponseTimeAsLatency);

        if (isTCPServiceDevice) {
            Integer port = handlerConfiguration.port;
            if (port == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No port configured!");
                return;
            }
            presenceDetection.setServicePorts(Collections.singleton(port));
        } else {
            // It does not harm to send an additional UDP packet to a device,
            // therefore we assume all ping devices are iOS devices. If this
            // does not work for all users for some obscure reason, we can make
            // this a thing configuration variable.
            presenceDetection.setIOSDevice(true);
            // Hand over binding configurations to the network service
            presenceDetection.setUseDhcpSniffing(configuration.allowDHCPlisten);
            presenceDetection.setUseIcmpPing(configuration.allowSystemPings);
            presenceDetection.setUseArpPing(true, configuration.arpPingToolPath, configuration.arpPingUtilMethod);
        }

        this.retries = handlerConfiguration.retry.intValue();
        presenceDetection.setRefreshInterval(handlerConfiguration.refreshInterval.longValue());
        presenceDetection.setTimeout(handlerConfiguration.timeout.intValue());

        wakeOnLanPacketSender = new WakeOnLanPacketSender(handlerConfiguration.macAddress,
                handlerConfiguration.hostname, handlerConfiguration.port);

        updateStatus(ThingStatus.ONLINE);
        presenceDetection.startAutomaticRefresh(scheduler);

        updateNetworkProperties();
    }

    private void updateNetworkProperties() {
        // Update properties (after startAutomaticRefresh, to get the correct dhcp state)
        Map<String, String> properties = editProperties();
        properties.put(NetworkBindingConstants.PROPERTY_ARP_STATE, presenceDetection.getArpPingState());
        properties.put(NetworkBindingConstants.PROPERTY_ICMP_STATE, presenceDetection.getIPPingState());
        properties.put(NetworkBindingConstants.PROPERTY_PRESENCE_DETECTION_TYPE, "");
        properties.put(NetworkBindingConstants.PROPERTY_IOS_WAKEUP, presenceDetection.isIOSdevice() ? "Yes" : "No");
        properties.put(NetworkBindingConstants.PROPERTY_DHCP_STATE, presenceDetection.getDhcpState());
        updateProperties(properties);
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

    @Override
    public void bindingConfigurationChanged() {
        // Make sure that changed binding configuration is reflected
        presenceDetection.setPreferResponseTimeAsLatency(configuration.preferResponseTimeAsLatency);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(NetworkActions.class);
    }

    public void sendWakeOnLanPacketViaIp() {
        // Hostname can't be null
        wakeOnLanPacketSender.sendWakeOnLanPacketViaIp();
    }

    public void sendWakeOnLanPacketViaMac() {
        if (handlerConfiguration.macAddress.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot send WoL packet because the 'macAddress' is not configured for " + thing.getUID());
        }
        wakeOnLanPacketSender.sendWakeOnLanPacketViaMac();
    }
}
