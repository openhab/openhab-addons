/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ddwrt.internal.handler;

import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.*;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTDiscoveryService;
import org.openhab.binding.ddwrt.internal.DDWRTNetworkConfiguration;
import org.openhab.binding.ddwrt.internal.api.DDWRTBaseDevice;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetwork;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetworkCache;
import org.openhab.binding.ddwrt.internal.api.DDWRTRadio;
import org.openhab.binding.ddwrt.internal.api.DhcpEventListener;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DDWRTNetworkBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTNetworkBridgeHandler extends BaseBridgeHandler implements DhcpEventListener {

    private final Logger logger = LoggerFactory.getLogger(DDWRTNetworkBridgeHandler.class);

    // DHCPACK events: "DHCPACK(br0) 192.168.0.83 9a:2a:33:74:e4:11 Lee-Pixel-8a"
    private static final Pattern DHCPACK_EVENT = Pattern
            .compile("DHCPACK\\(\\S+\\)\\s+(\\S+)\\s+([0-9a-fA-F:]{17})(?:\\s+(\\S+))?", Pattern.CASE_INSENSITIVE);

    private DDWRTNetworkConfiguration config = new DDWRTNetworkConfiguration();

    private volatile DDWRTNetwork network = new DDWRTNetwork(); /* volatile because accessed from multiple threads */

    private @Nullable ScheduledFuture<?> refreshJob;

    public DDWRTNetworkBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    // Public API

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(DDWRTDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Ignoring command = {} for channel = {} - the DDWRT Network is read-only!", command, channelUID);
    }

    @Override
    public void initialize() {
        config = getConfigAs(DDWRTNetworkConfiguration.class);

        logger.debug("Initializing DDWRT Network Bridge handler '{}' with config = {}.", getThing().getUID(), config);

        if (config.hostnames.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-hostnames");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        network.setBridgeUID(getThing().getUID());
        network.addDhcpEventListener(this);

        // execute setconfig in the background because it can trigger a refresh
        scheduler.schedule(() -> {
            network.setConfig(config);
            synchronized (this) {
                if (refreshJob == null) {
                    logger.debug("Scheduling refresh job every {}s", config.refreshInterval);
                    refreshJob = scheduler.scheduleWithFixedDelay(() -> {
                        network.refresh();
                        updateBridgeStatus();
                    }, 0, config.refreshInterval, TimeUnit.SECONDS);
                } else {
                    network.refresh();
                    updateBridgeStatus();
                }
            }

        }, 10, TimeUnit.MILLISECONDS);
    }

    public @Nullable DDWRTNetwork getNetwork() {
        return network;
    }

    private void updateBridgeStatus() {
        if (network.hasOnlineDevices()) {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
            updateBridgeChannels();
        } else if (network.getDevices().isEmpty() && !network.hasPendingDevices()) {
            updateStatus(ThingStatus.UNKNOWN);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.no-active-sessions");
        }
    }

    private void updateBridgeChannels() {
        DDWRTNetworkCache cache = network.getCache();
        // Compute network-wide client counts from cache
        // Total = active ARP entries + wireless MACs not in ARP
        int arpActive = 0;
        for (String mac : cache.getArpMacs()) {
            if (cache.isArpActive(mac)) {
                arpActive++;
            }
        }
        int wirelessClients = 0;
        for (DDWRTBaseDevice device : cache.getDevices()) {
            wirelessClients += device.getDeviceWirelessClients();
        }
        // Count wireless MACs not in ARP to avoid double-counting
        int wirelessNotInArp = 0;
        for (DDWRTBaseDevice device : cache.getDevices()) {
            for (DDWRTRadio radio : cache.getRadios()) {
                if (radio.getParentDeviceMac().equals(device.getMac())) {
                    for (String clientMac : radio.getAssoclist()) {
                        if (!cache.isArpActive(clientMac)) {
                            wirelessNotInArp++;
                        }
                    }
                }
            }
        }
        int totalClients = arpActive + wirelessNotInArp;
        int wiredClients = Math.max(0, totalClients - wirelessClients);
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_TOTAL_CLIENTS), new DecimalType(totalClients));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_WIRELESS_CLIENTS), new DecimalType(wirelessClients));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_WIRED_CLIENTS), new DecimalType(wiredClients));
        // DHCP metrics from cache (network-wide)
        int dhcpLeases = cache.getDhcpLeases().size();
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_DHCP_LEASES), new DecimalType(dhcpLeases));
        // Gateway-specific metrics (WAN IP, WAN traffic, DHCP pool)
        DDWRTBaseDevice gateway = null;
        for (DDWRTBaseDevice device : cache.getDevices()) {
            if (device.isGateway()) {
                gateway = device;
                break;
            }
        }
        if (gateway != null) {
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_WAN_IP), StringType.valueOf(gateway.getWanIp()));
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_WAN_IN), new DecimalType(gateway.getWanIn()));
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_WAN_OUT), new DecimalType(gateway.getWanOut()));
            int dhcpPoolSize = gateway.getDhcpPoolSize();
            if (dhcpPoolSize > 0) {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_DHCP_REMAINING),
                        new DecimalType(Math.max(0, dhcpPoolSize - dhcpLeases)));
            }
        } else {
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_WAN_IP), UnDefType.UNDEF);
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_WAN_IN), UnDefType.UNDEF);
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_WAN_OUT), UnDefType.UNDEF);
        }
        // DHCP event channel is now event-driven via DhcpEventListener, not polled
    }

    private void cancelRefreshJob() {
        synchronized (this) {
            final ScheduledFuture<?> rj = refreshJob;

            if (rj != null) {
                logger.debug("Cancelling refresh job");
                rj.cancel(true);
                refreshJob = null;
            }
        }
    }

    @Override
    public void dispose() {
        network.removeDhcpEventListener(this);
        cancelRefreshJob();
        network.dispose();
    }

    @Override
    public void onDhcpEvent(String hostname, String eventMessage) {
        // Only process DHCP events from gateway devices
        DDWRTNetworkCache cache = network.getCache();
        for (DDWRTBaseDevice device : cache.getDevices()) {
            if (device.isGateway() && device.getHostname().equals(hostname)) {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_LAST_DHCP_EVENT),
                        StringType.valueOf(eventMessage));
                triggerChannel(new ChannelUID(getThing().getUID(), CHANNEL_DHCP_EVENT), eventMessage);

                // Try to parse DHCPACK and update cache directly
                java.util.regex.Matcher m = DHCPACK_EVENT.matcher(eventMessage);
                if (m.find()) {
                    String ip = Objects.requireNonNull(m.group(1));
                    String clientMac = Objects.requireNonNull(m.group(2)).toLowerCase(Locale.ROOT);
                    String dhcpHostname = m.group(3); // may be null if hostname not in ACK

                    // Handle MAC randomization: merge if this hostname exists under a different MAC
                    if (dhcpHostname != null && !dhcpHostname.isEmpty() && !"*".equals(dhcpHostname)) {
                        cache.mergeRandomizedMac(clientMac, dhcpHostname);
                    }

                    final String finalHostname = (dhcpHostname != null && !"*".equals(dhcpHostname)) ? dhcpHostname
                            : "";

                    // Update the wireless client directly in the cache
                    cache.computeWirelessClient(clientMac, client -> {
                        // Set hostname if empty (allows DHCP to override OUI hostnames)
                        if (!finalHostname.isEmpty() && client.getPrimaryHostname().isEmpty()) {
                            client.setHostname(finalHostname);
                        }
                        if (!ip.isEmpty() && !ip.equals(client.getIpAddress())) {
                            client.setIpAddress(ip);
                        }
                        client.setOnline(true);
                        client.setLastSeen(Instant.now());
                        return client;
                    });
                    logger.debug("[DHCPACK] {} hostname={} ip={} on {}", clientMac, finalHostname, ip, hostname);
                }
                break;
            }
        }
    }
}
