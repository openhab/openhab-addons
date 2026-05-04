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
package org.openhab.binding.ddwrt.internal;

import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.THING_TYPE_CLIENT;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.THING_TYPE_DEVICE;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.THING_TYPE_FIREWALL_RULE;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.THING_TYPE_RADIO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.api.DDWRTBaseDevice;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetwork;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetworkCache;
import org.openhab.binding.ddwrt.internal.api.RefreshListener;
import org.openhab.binding.ddwrt.internal.handler.DDWRTNetworkBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DDWRTDiscoveryService} is the discovery service for detecting things in DD-WRT network.
 *
 * @author Lee Ballard - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = DDWRTDiscoveryService.class)
@NonNullByDefault
public class DDWRTDiscoveryService extends AbstractThingHandlerDiscoveryService<DDWRTNetworkBridgeHandler>
        implements RefreshListener {

    private static final int DISCOVERY_TIMEOUT_SECONDS = 120;
    private static final int BACKGROUND_DISCOVERY_INITIAL_DELAY_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(DDWRTDiscoveryService.class);

    private @Nullable ScheduledFuture<?> backgroundDiscoveryJob;
    private @Nullable ScheduledFuture<?> refreshTriggeredScan;

    @Reference
    private @Nullable Inbox inbox;

    public DDWRTDiscoveryService() {
        super(DDWRTNetworkBridgeHandler.class, SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SECONDS);
    }

    @Override
    protected void startScan() {
        try {
            logger.debug("Starting DD-WRT discovery scan");

            final DDWRTNetwork net = thingHandler.getNetwork();
            if (net == null) {
                return;
            }
            discoverDevices(net);
            discoverRadios(net);
            discoverClients(net);
            discoverFirewallRules(net);
        } catch (Exception e) {
            logger.warn("Error during DD-WRT discovery scan: {}", e.getMessage(), e);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting DD-WRT background discovery");
        ScheduledFuture<?> job = backgroundDiscoveryJob;
        if (job == null || job.isCancelled()) {
            backgroundDiscoveryJob = scheduler.scheduleWithFixedDelay(this::startScan,
                    BACKGROUND_DISCOVERY_INITIAL_DELAY_SECONDS, DISCOVERY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        // Register as refresh listener so discovery runs immediately after device refresh
        DDWRTNetwork net = thingHandler.getNetwork();
        if (net != null) {
            net.addRefreshListener(this);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping DD-WRT background discovery");
        ScheduledFuture<?> job = backgroundDiscoveryJob;
        if (job != null) {
            job.cancel(true);
            backgroundDiscoveryJob = null;
        }
        ScheduledFuture<?> pending = refreshTriggeredScan;
        if (pending != null) {
            pending.cancel(false);
            refreshTriggeredScan = null;
        }
        // Unregister refresh listener
        DDWRTNetwork net = thingHandler.getNetwork();
        if (net != null) {
            net.removeRefreshListener(this);
        }
    }

    @Override
    public void onRefreshComplete(DDWRTBaseDevice device) {
        // Debounce: schedule a scan 2s from now, replacing any pending scan.
        // This coalesces rapid refresh events (e.g., multiple devices refreshing)
        // into a single discovery scan.
        ScheduledFuture<?> pending = refreshTriggeredScan;
        if (pending != null) {
            pending.cancel(false);
        }
        refreshTriggeredScan = scheduler.schedule((Runnable) this::startScan, 2, TimeUnit.SECONDS);
    }

    private void discoverDevices(DDWRTNetwork net) {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();
        final DDWRTNetworkConfiguration netCfg = net.getConfig();
        if (netCfg == null) {
            logger.warn("No configuration available for discovery.");
            return;
        }

        net.getDevices().forEach(device -> {
            final DDWRTDeviceConfiguration devCfg = device.getConfig();
            final String label = device.getHostname().isEmpty() ? device.getMac() : device.getHostname();
            final String macClean = device.getMac().toLowerCase(Locale.ROOT).replace(":", "");

            final ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, bridgeUID,
                    macClean.isEmpty() ? devCfg.hostname : macClean);

            logger.debug("Discovered device: '{}'", thingUID);

            final Map<String, Object> props = Map.of("hostname", devCfg.hostname, "port", devCfg.port, "user",
                    devCfg.user, "refreshInterval", devCfg.refreshInterval, "mac", device.getMac(), "model",
                    device.getModel());

            final DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(label).withProperties(props).withRepresentationProperty("mac").build();

            thingDiscovered(result);
        });
    }

    private void discoverRadios(DDWRTNetwork net) {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();
        final DDWRTNetworkCache cache = net.getCache();

        cache.getRadios().forEach(radio -> {
            // Get parent device for hostname
            DDWRTBaseDevice device = cache.getDevice(radio.getParentDeviceMac());
            final String hostname = device != null && !device.getHostname().isEmpty() ? device.getHostname()
                    : radio.getParentDeviceMac();

            // ID: deviceMac-interface (e.g., 24f5a2c61659-wlan0)
            final String id = radio.getParentDeviceMac().replace(":", "-") + "-" + radio.getIfaceName();
            final ThingUID thingUID = new ThingUID(THING_TYPE_RADIO, bridgeUID, id);

            // Label: hostname interface (e.g., "gateway-ap wlan0")
            final String label = hostname + " " + radio.getIfaceName();

            logger.debug("Discovered radio: '{}'", thingUID);

            final Map<String, Object> props = Map.of("interfaceId", radio.getInterfaceId(), "parentDeviceMac",
                    radio.getParentDeviceMac(), "ifaceName", radio.getIfaceName());

            final DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(label).withProperties(props).withRepresentationProperty("interfaceId").build();

            thingDiscovered(result);
        });
    }

    private void discoverClients(DDWRTNetwork net) {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();
        final DDWRTNetworkCache cache = net.getCache();

        cache.getWirelessClients().forEach(client -> {
            if (client.getHostname().isEmpty()) {
                // Skip clients without a hostname — hostname is required for client things
                logger.debug("Skipping client without hostname: MAC={}", client.getMac());
                return;
            }
            final String thingId = client.getHostname().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
            final ThingUID thingUID = new ThingUID(THING_TYPE_CLIENT, bridgeUID, thingId);

            logger.debug("Discovered client: '{}'", thingUID);

            final Map<String, Object> props = new java.util.HashMap<>();
            props.put("hostname", client.getHostname());
            props.put("mac", client.getMac());

            final DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(client.getHostname()).withProperties(props)
                    // Keep hostname as the representation property. Some clients use
                    // MAC randomization, so the MAC is not stable enough to be the primary
                    // representation key in the inbox/UI.
                    .withRepresentationProperty("hostname").build();

            logger.debug(
                    "Submitting discovery result for client: {} ({}) - AP: {}, SSID: {}, Channel: {}, Signal: {}dBm, SNR: {}",
                    thingUID, client.getHostname(), client.getApMac(), client.getSsid(), client.getChannel(),
                    client.getSignalDbm(), client.getSnr());

            // If this client is already sitting in the discovery inbox under an
            // older placeholder hostname (for example an OUI-generated TPLink-e916b1) or an
            // earlier MAC, replace the pending inbox entry before submitting the new result.
            // Matching is intentionally by hostname OR MAC. Hostname remains the preferred
            // representation property because MAC randomization can cause the MAC to change
            // for some devices.
            replacePendingClientInboxDuplicates(thingUID, client.getHostname(), client.getMac());

            thingDiscovered(result);
        });
    }

    private void replacePendingClientInboxDuplicates(ThingUID newThingUID, String hostname, String mac) {
        String normalizedHostname = normalizeHostname(hostname);
        String normalizedMac = normalizeMac(mac);

        if (normalizedHostname.isEmpty() && normalizedMac.isEmpty()) {
            return;
        }

        Collection<DiscoveryResult> entries = inbox.getAll();
        if (entries.isEmpty()) {
            return;
        }

        for (DiscoveryResult entry : new ArrayList<>(entries)) {
            ThingUID existingUID = entry.getThingUID();
            if (existingUID == null || existingUID.equals(newThingUID)) {
                continue;
            }

            // Only operate on pending client inbox entries
            if (!existingUID.getId().startsWith("client")) {
                continue;
            }

            // Only operate on entries that belong to the same bridge
            ThingUID existingBridgeUID = entry.getBridgeUID();
            ThingUID newBridgeUID = thingHandler.getThing().getUID();
            if (existingBridgeUID == null || newBridgeUID == null || !existingBridgeUID.equals(newBridgeUID)) {
                continue;
            }

            String existingHostname = normalizeHostname(String.valueOf(entry.getProperties().get("hostname")));
            String existingMac = normalizeMac(String.valueOf(entry.getProperties().get("mac")));

            boolean sameHostname = !normalizedHostname.isEmpty() && normalizedHostname.equals(existingHostname);
            boolean sameMac = !normalizedMac.isEmpty() && normalizedMac.equals(existingMac);

            if (sameHostname || sameMac) {
                logger.debug(
                        "Removing stale client inbox entry {} before adding replacement {} (hostname match={}, mac match={})",
                        existingUID, newThingUID, sameHostname, sameMac);
                inbox.remove(existingUID);
            }
        }
    }

    private static String normalizeHostname(String hostname) {
        if (hostname == null) {
            return "";
        }
        String trimmed = hostname.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
            return "";
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private static String normalizeMac(String mac) {
        if (mac == null) {
            return "";
        }
        String trimmed = mac.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
            return "";
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private void discoverFirewallRules(DDWRTNetwork net) {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();
        final DDWRTNetworkCache cache = net.getCache();

        cache.getFirewallRules().forEach(rule -> {
            final String id = rule.getRuleId().replaceAll("[^a-zA-Z0-9_]", "_");
            final ThingUID thingUID = new ThingUID(THING_TYPE_FIREWALL_RULE, bridgeUID, id);

            final String label = rule.getDescription().isEmpty() ? rule.getRuleId() : rule.getDescription();

            logger.debug("Discovered firewall rule: '{}'", thingUID);

            final Map<String, Object> props = Map.of("ruleId", rule.getRuleId());

            final DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(label).withProperties(props).withRepresentationProperty("ruleId").build();

            thingDiscovered(result);
        });
    }
}
