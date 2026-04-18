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
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.THING_TYPE_DEVICE;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.THING_TYPE_FIREWALL_RULE;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.THING_TYPE_RADIO;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.THING_TYPE_WIRELESS_CLIENT;

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
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
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
            discoverWirelessClients(net);
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
                    device.getModel(), "firmware", device.getFirmware());

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

    private void discoverWirelessClients(DDWRTNetwork net) {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();
        final DDWRTNetworkCache cache = net.getCache();

        cache.getWirelessClients().forEach(client -> {
            if (client.getHostname().isEmpty()) {
                // Skip clients without a hostname — hostname is required for wireless client things
                logger.debug("Skipping wireless client without hostname: MAC={}", client.getMac());
                return;
            }
            final String thingId = client.getHostname().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
            final ThingUID thingUID = new ThingUID(THING_TYPE_WIRELESS_CLIENT, bridgeUID, thingId);

            logger.debug("Discovered wireless client: '{}'", thingUID);

            final Map<String, Object> props = new java.util.HashMap<>();
            props.put("hostname", client.getHostname());
            if (!client.getMac().isEmpty()) {
                props.put("mac", client.getMac());
            }

            final DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(client.getHostname()).withProperties(props).withRepresentationProperty("hostname")
                    .build();

            logger.debug(
                    "Submitting discovery result for wireless client: {} ({}) - AP: {}, SSID: {}, Channel: {}, Signal: {}dBm, SNR: {}",
                    thingUID, client.getHostname(), client.getApMac(), client.getSsid(), client.getChannel(),
                    client.getSignalDbm(), client.getSnr());
            thingDiscovered(result);
        });
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
