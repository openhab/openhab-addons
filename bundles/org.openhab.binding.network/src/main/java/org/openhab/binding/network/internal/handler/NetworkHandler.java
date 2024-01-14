/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import static org.openhab.binding.network.internal.utils.NetworkUtils.durationToMillis;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                presenceDetection.getValue(value -> updateState(CHANNEL_ONLINE, OnOffType.from(value.isReachable())));
                break;
            case CHANNEL_LATENCY:
                presenceDetection.getValue(value -> {
                    double latencyMs = durationToMillis(value.getLowestLatency());
                    updateState(CHANNEL_LATENCY, new QuantityType<>(latencyMs, MetricPrefix.MILLI(Units.SECOND)));
                });
                break;
            case CHANNEL_LASTSEEN:
                Instant lastSeen = presenceDetection.getLastSeen();
                if (lastSeen != null) {
                    updateState(CHANNEL_LASTSEEN, new DateTimeType(
                            ZonedDateTime.ofInstant(lastSeen, TimeZone.getDefault().toZoneId()).withFixedOffsetZone()));
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
        double latencyMs = durationToMillis(value.getLowestLatency());
        updateState(CHANNEL_ONLINE, OnOffType.ON);
        updateState(CHANNEL_LATENCY, new QuantityType<>(latencyMs, MetricPrefix.MILLI(Units.SECOND)));
    }

    @Override
    public void finalDetectionResult(PresenceDetectionValue value) {
        // We do not notify the framework immediately if a device presence detection failed and
        // the user configured retries to be > 1.
        retryCounter = value.isReachable() ? 0 : retryCounter + 1;

        if (retryCounter >= retries) {
            updateState(CHANNEL_ONLINE, OnOffType.OFF);
            updateState(CHANNEL_LATENCY, UnDefType.UNDEF);
            retryCounter = 0;
        }

        Instant lastSeen = presenceDetection.getLastSeen();
        if (value.isReachable() && lastSeen != null) {
            updateState(CHANNEL_LASTSEEN, new DateTimeType(
                    ZonedDateTime.ofInstant(lastSeen, TimeZone.getDefault().toZoneId()).withFixedOffsetZone()));
        } else if (!value.isReachable() && lastSeen == null) {
            updateState(CHANNEL_LASTSEEN, UnDefType.UNDEF);
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
        presenceDetection.setNetworkInterfaceNames(handlerConfiguration.networkInterfaceNames);
        presenceDetection.setPreferResponseTimeAsLatency(configuration.preferResponseTimeAsLatency);

        if (isTCPServiceDevice) {
            Integer port = handlerConfiguration.port;
            if (port == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No port configured!");
                return;
            }
            presenceDetection.setServicePorts(Set.of(port));
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
        presenceDetection.setRefreshInterval(Duration.ofMillis(handlerConfiguration.refreshInterval));
        presenceDetection.setTimeout(Duration.ofMillis(handlerConfiguration.timeout));

        wakeOnLanPacketSender = new WakeOnLanPacketSender(handlerConfiguration.macAddress,
                handlerConfiguration.hostname, handlerConfiguration.port, handlerConfiguration.networkInterfaceNames);

        updateStatus(ThingStatus.ONLINE);
        presenceDetection.startAutomaticRefresh();

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
        initialize(new PresenceDetection(this, scheduler,
                Duration.ofMillis(configuration.cacheDeviceStateTimeInMS.intValue())));
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
        return List.of(NetworkActions.class);
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
