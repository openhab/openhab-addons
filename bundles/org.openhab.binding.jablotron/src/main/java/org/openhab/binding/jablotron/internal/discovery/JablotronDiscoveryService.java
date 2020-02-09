/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.jablotron.internal.discovery;

import static org.openhab.binding.jablotron.JablotronBindingConstants.*;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.jablotron.internal.handler.JablotronBridgeHandler;
import org.openhab.binding.jablotron.internal.model.JablotronDiscoveredService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JablotronDiscoveryService} is responsible for the thing discovery
 * process.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(JablotronDiscoveryService.class);
    private JablotronBridgeHandler bridge;
    private @Nullable DiscoveryServiceCallback discoveryServiceCallback;

    private @Nullable ScheduledFuture<?> discoveryJob = null;

    private static final int DISCOVERY_TIMEOUT_SEC = 10;

    public JablotronDiscoveryService(JablotronBridgeHandler bridgeHandler) {
        super(DISCOVERY_TIMEOUT_SEC);
        logger.debug("Creating discovery service");
        this.bridge = bridgeHandler;
    }

    private void startDiscovery() {
        if (this.bridge.getThing().getStatus() == ThingStatus.ONLINE) {
            discoverServices();
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        super.stopBackgroundDiscovery();
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting Jablotron background discovery");

        if (discoveryJob == null || discoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::startDiscovery, 10, 3600,
                    TimeUnit.SECONDS);
        }

    }

    /**
     * Called on component activation.
     */
    @Override
    @Activate
    public void activate(@Nullable Map<String, @Nullable Object> configProperties) {
        super.activate(configProperties);
    }

    @Deactivate
    @Override
    public void deactivate() {
        super.deactivate();
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting scanning for items...");
        startDiscovery();
    }

    public void oasisDiscovered(String label, String serviceId) {
        ThingUID thingUID = new ThingUID(THING_TYPE_OASIS, bridge.getThing().getUID(), serviceId);

        logger.debug("Detected an OASIS alarm with service id: {}", serviceId);
        thingDiscovered(
                DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_OASIS)
                        .withLabel(label)
                        .withBridge(bridge.getThing().getUID()).build());
    }

    public void ja100Discovered(String label, String serviceId) {
        ThingUID thingUID = new ThingUID(THING_TYPE_JA100, bridge.getThing().getUID(), serviceId);

        logger.debug("Detected a JA100 alarm with service id: {}", serviceId);
        thingDiscovered(
                DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_JA100)
                        .withLabel(label)
                        .withBridge(bridge.getThing().getUID()).build());
    }

    public void ja100fDiscovered(String label, String serviceId) {
        ThingUID thingUID = new ThingUID(THING_TYPE_JA100F, bridge.getThing().getUID(), serviceId);

        logger.debug("Detected a JA100+ alarm with service id: {}", serviceId);
        thingDiscovered(
                DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_JA100F)
                        .withLabel(label)
                        .withBridge(bridge.getThing().getUID()).build());
    }

    private synchronized void discoverServices() {
        try {
            List<JablotronDiscoveredService> services = bridge.discoverServices();

            if (services == null || services.size() == 0) {
                logger.info("Cannot find any Jablotron device");
                return;
            }

            for (JablotronDiscoveredService service : services) {
                String serviceId = String.valueOf(service.getId());
                logger.debug("Found Jablotron service: {} id: {}", service.getName(), serviceId);

                if (service.getServiceType().toLowerCase().equals(THING_TYPE_OASIS.getId())) {
                    oasisDiscovered("Jablotron OASIS Alarm : " + service.getName(), serviceId);
                } else if (service.getServiceType().equals(THING_TYPE_JA100.getId())) {
                    ja100Discovered("Jablotron JA100 Alarm : " + service.getName(), serviceId);
                } else if (service.getServiceType().equals(THING_TYPE_JA100F.getId().toUpperCase())) {
                    ja100fDiscovered("Jablotron JA100+ Alarm : " + service.getName(), serviceId);
                } else {
                    logger.info("Unsupported device type discovered: {} with serviceId: {} and type: {}", service.getName(), serviceId, service.getServiceType());
                }
            }
        } catch (Exception ex) {
            logger.debug("Cannot discover Jablotron services!", ex);
        }
    }
}
