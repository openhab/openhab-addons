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
package org.openhab.binding.ddwrt.internal;

import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.THING_TYPE_DEVICE;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetwork;
import org.openhab.binding.ddwrt.internal.handler.DDWRTNetworkBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.ddwrt")
/**
 * The {@link ddwrtConfiguration} class is the discovery service for detecting things in DD-WRT network.
 *
 * @author Lee Ballard - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = DDWRTDiscoveryService.class)
@NonNullByDefault
public class DDWRTDiscoveryService extends AbstractThingHandlerDiscoveryService<DDWRTNetworkBridgeHandler> {

    private static final int DISCOVERY_TIMEOUT_SECONDS = 120;

    private final Logger logger = LoggerFactory.getLogger(DDWRTDiscoveryService.class);

    public DDWRTDiscoveryService() {
        super(DDWRTNetworkBridgeHandler.class, SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SECONDS);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting DD-WRT discovery scan");

        final DDWRTNetwork net = thingHandler.getNetwork();
        if (net == null) {
            return;
        }
        discoverDevices(net);
    }

    // :TODO: ballle98/openhab-addons#15 Implement Background Discovery
    // @Override
    // protected void startBackgroundDiscovery() {
    // logger.debug("Start DD-WRT device background discovery");
    // if (DDWRTDiscoveryJob == null || DDWRTDiscoveryJob.isCancelled()) {
    // DDWRTDiscoveryJob = scheduler.scheduleWithFixedDelay(DDWRTDiscoveryRunnable, 0, refreshInterval,
    // TimeUnit.SECONDS);
    // }
    // }

    // @Override
    // protected void stopBackgroundDiscovery() {
    // logger.debug("Stop DDWRT device background discovery");
    // if (DDWRTDiscoveryJob != null) {
    // DDWRTDiscoveryJob.cancel(true);
    // DDWRTDiscoveryJob = null;
    // }
    // }

    private void discoverDevices(DDWRTNetwork net) {
        final ThingUID bridgeUID = thingHandler.getThing().getUID();
        final DDWRTNetworkConfiguration netCfg = net.getConfig();
        if (netCfg == null) {
            logger.warn("No configuration available for discovery.");
            return;
        }

        net.getDevices().forEach(device -> {
            final DDWRTDeviceConfiguration devCfg = device.getConfig();

            // final String macClean = device.getMac().toLowerCase().replace(":", "");
            final ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, bridgeUID, device.getName());

            logger.debug("discovered device: \'{}\'", thingUID);

            final Map<String, Object> props = Map.of("hostname", devCfg.hostname, "port", devCfg.port, "user",
                    devCfg.user, "password", devCfg.password, "refreshInterval", devCfg.refreshInterval, "mac",
                    device.getMac(), "name", device.getName(), "model", device.getModel(), "firmware",
                    device.getFirmware());

            final DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(device.getName()).withProperties(props).withRepresentationProperty("mac").build();

            thingDiscovered(result);
        });

        return;
    }
}
