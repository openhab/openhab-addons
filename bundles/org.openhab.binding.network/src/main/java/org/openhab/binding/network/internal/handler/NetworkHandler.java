/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * @author Ravi Nadahar - Made class thread-safe
 */
@NonNullByDefault
public class NetworkHandler extends BaseThingHandler
        implements PresenceDetectionListener, NetworkBindingConfigurationListener {
    private final Logger logger = LoggerFactory.getLogger(NetworkHandler.class);

    /* All access must be guarded by "this" */
    private @Nullable PresenceDetection presenceDetection;

    /* All access must be guarded by "this" */
    private @Nullable ScheduledFuture<?> refreshJob;

    /* All access must be guarded by "this" */
    private @Nullable WakeOnLanPacketSender wakeOnLanPacketSender;

    private final boolean isTCPServiceDevice;
    private final NetworkBindingConfiguration configuration;

    // How many retries before a device is deemed offline
    volatile int retries;
    // Retry counter. Will be reset as soon as a device presence detection succeed.
    private volatile int retryCounter = 0;
    private final ScheduledExecutorService executor;

    /**
     * Creates a new instance using the specified parameters.
     */
    public NetworkHandler(Thing thing, ScheduledExecutorService executor, boolean isTCPServiceDevice,
            NetworkBindingConfiguration configuration) {
        super(thing);
        this.executor = executor;
        this.isTCPServiceDevice = isTCPServiceDevice;
        this.configuration = configuration;
        this.configuration.addNetworkBindingConfigurationListener(this);
    }

    private void refreshValue(ChannelUID channelUID) {
        PresenceDetection pd;
        ScheduledFuture<?> rj;
        synchronized (this) {
            pd = presenceDetection;
            rj = refreshJob;
        }
        // We are not yet even initialized, don't do anything
        if (pd == null || rj == null) {
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_ONLINE:
                pd.getValue(value -> updateState(CHANNEL_ONLINE, OnOffType.from(value.isReachable())));
                break;
            case CHANNEL_LATENCY:
                pd.getValue(value -> {
                    double latencyMs = durationToMillis(value.getLowestLatency());
                    updateState(CHANNEL_LATENCY, new QuantityType<>(latencyMs, MetricPrefix.MILLI(Units.SECOND)));
                });
                break;
            case CHANNEL_LASTSEEN:
                // We should not set the last seen state to UNDEF, it prevents restoreOnStartup from working
                // For reference: https://github.com/openhab/openhab-addons/issues/17404
                Instant lastSeen = pd.getLastSeen();
                if (lastSeen != null) {
                    updateState(CHANNEL_LASTSEEN, new DateTimeType(lastSeen));
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
        if (value.isReachable()) {
            retryCounter = 0;
        } else {
            retryCounter++;
        }

        if (retryCounter >= retries) {
            updateState(CHANNEL_ONLINE, OnOffType.OFF);
            updateState(CHANNEL_LATENCY, UnDefType.UNDEF);
            retryCounter = 0;
        }

        PresenceDetection pd;
        synchronized (this) {
            pd = presenceDetection;
        }
        Instant lastSeen = pd == null ? null : pd.getLastSeen();
        if (value.isReachable() && lastSeen != null) {
            updateState(CHANNEL_LASTSEEN, new DateTimeType(lastSeen));
        }
        // We should not set the last seen state to UNDEF, it prevents restoreOnStartup from working
        // For reference: https://github.com/openhab/openhab-addons/issues/17404

        updateNetworkProperties();
    }

    @Override
    public void dispose() {
        synchronized (this) {
            ScheduledFuture<?> refreshJob = this.refreshJob;
            if (refreshJob != null) {
                refreshJob.cancel(true);
                this.refreshJob = null;
            }
            presenceDetection = null;
        }
    }

    /**
     * Initialize with a presenceDetection object.
     * Used by testing for injecting.
     */
    void initialize(PresenceDetection presenceDetection) {
        NetworkHandlerConfiguration config = getConfigAs(NetworkHandlerConfiguration.class);

        presenceDetection.setHostname(config.hostname);
        presenceDetection.setNetworkInterfaceNames(config.networkInterfaceNames);
        presenceDetection.setPreferResponseTimeAsLatency(configuration.preferResponseTimeAsLatency);

        if (isTCPServiceDevice) {
            Integer port = config.port;
            if (port == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No port configured!");
                return;
            }
            presenceDetection.setServicePorts(Set.of(port));
        } else {
            presenceDetection.setIOSDevice(config.useIOSWakeUp);
            // Hand over binding configurations to the network service
            presenceDetection.setUseDhcpSniffing(configuration.allowDHCPlisten);
            presenceDetection.setUseIcmpPing(config.useIcmpPing ? configuration.allowSystemPings : null);
            presenceDetection.setUseArpPing(config.useArpPing, configuration.arpPingToolPath,
                    configuration.arpPingUtilMethod);
        }

        this.retries = config.retry.intValue();
        presenceDetection.setTimeout(Duration.ofMillis(config.timeout));
        synchronized (this) {
            this.presenceDetection = presenceDetection;
            wakeOnLanPacketSender = new WakeOnLanPacketSender(config.macAddress, config.hostname, config.port,
                    config.networkInterfaceNames);
            if (config.refreshInterval > 0) {
                refreshJob = executor.scheduleWithFixedDelay(presenceDetection::refresh, 0, config.refreshInterval,
                        TimeUnit.MILLISECONDS);
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    private void updateNetworkProperties() {
        // Update properties (after startAutomaticRefresh, to get the correct dhcp state)
        Map<String, String> properties = editProperties();
        synchronized (this) {
            PresenceDetection pd = presenceDetection;
            if (pd == null) {
                logger.warn("Can't update network properties because presenceDetection is null");
                return;
            }
            properties.put(NetworkBindingConstants.PROPERTY_ARP_STATE, pd.getArpPingState());
            properties.put(NetworkBindingConstants.PROPERTY_ICMP_STATE, pd.getIPPingState());
            properties.put(NetworkBindingConstants.PROPERTY_PRESENCE_DETECTION_TYPE, "");
            properties.put(NetworkBindingConstants.PROPERTY_DHCP_STATE, pd.getDhcpState());
        }
        updateProperties(properties);
    }

    // Create a new network service and apply all configurations.
    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        executor.submit(() -> {
            initialize(new PresenceDetection(this, Duration.ofMillis(configuration.cacheDeviceStateTimeInMS.intValue()),
                    executor));
        });
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
        synchronized (this) {
            PresenceDetection pd = presenceDetection;
            if (pd != null) {
                pd.setPreferResponseTimeAsLatency(configuration.preferResponseTimeAsLatency);
            }
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(NetworkActions.class);
    }

    public void sendWakeOnLanPacketViaIp() {
        WakeOnLanPacketSender sender;
        synchronized (this) {
            sender = wakeOnLanPacketSender;
        }
        if (sender != null) {
            // Hostname can't be null
            sender.sendWakeOnLanPacketViaIp();
        } else {
            logger.warn("Failed to send WoL packet via IP because sender is null");
        }
    }

    public void sendWakeOnLanPacketViaMac() {
        NetworkHandlerConfiguration config = getConfigAs(NetworkHandlerConfiguration.class);
        if (config.macAddress.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot send WoL packet because the 'macAddress' is not configured for " + thing.getUID());
        }
        WakeOnLanPacketSender sender;
        synchronized (this) {
            sender = wakeOnLanPacketSender;
        }
        if (sender != null) {
            sender.sendWakeOnLanPacketViaMac();
        } else {
            logger.warn("Failed to send WoL packet via MAC because sender is null");
        }
    }
}
