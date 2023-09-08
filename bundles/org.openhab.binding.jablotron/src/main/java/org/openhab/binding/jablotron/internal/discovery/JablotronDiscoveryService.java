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
package org.openhab.binding.jablotron.internal.discovery;

import static org.openhab.binding.jablotron.JablotronBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jablotron.internal.handler.JablotronBridgeHandler;
import org.openhab.binding.jablotron.internal.model.JablotronDiscoveredService;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JablotronDiscoveryService} is responsible for the thing discovery
 * process.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(JablotronDiscoveryService.class);

    private @Nullable JablotronBridgeHandler bridgeHandler;

    private @Nullable ScheduledFuture<?> discoveryJob = null;

    public JablotronDiscoveryService() {
        super(DISCOVERY_TIMEOUT_SEC);
        logger.debug("Creating discovery service");
    }

    private void startDiscovery() {
        JablotronBridgeHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null && ThingStatus.ONLINE == localBridgeHandler.getThing().getStatus()) {
            discoverServices();
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler thingHandler) {
        if (thingHandler instanceof JablotronBridgeHandler bridgeHandler) {
            this.bridgeHandler = bridgeHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    protected void stopBackgroundDiscovery() {
        super.stopBackgroundDiscovery();
        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob != null) {
            localDiscoveryJob.cancel(true);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting Jablotron background discovery");
        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob == null || localDiscoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::startDiscovery, 10, 3600, TimeUnit.SECONDS);
        }
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting scanning for items...");
        startDiscovery();
    }

    public void oasisDiscovered(String label, String serviceId) {
        JablotronBridgeHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            ThingUID thingUID = new ThingUID(THING_TYPE_OASIS, localBridgeHandler.getThing().getUID(), serviceId);

            Map<String, Object> properties = new HashMap<>();
            properties.put(PROPERTY_SERVICE_ID, serviceId);

            logger.debug("Detected an OASIS alarm with service id: {}", serviceId);
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_OASIS).withLabel(label)
                    .withProperties(properties).withRepresentationProperty(PROPERTY_SERVICE_ID)
                    .withBridge(localBridgeHandler.getThing().getUID()).build());
        }
    }

    public void ja100Discovered(String label, String serviceId) {
        JablotronBridgeHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            ThingUID thingUID = new ThingUID(THING_TYPE_JA100, localBridgeHandler.getThing().getUID(), serviceId);
            Map<String, Object> properties = new HashMap<>();
            properties.put(PROPERTY_SERVICE_ID, serviceId);

            logger.debug("Detected a JA100 alarm with service id: {}", serviceId);
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_JA100).withLabel(label)
                    .withProperties(properties).withRepresentationProperty(PROPERTY_SERVICE_ID)
                    .withBridge(localBridgeHandler.getThing().getUID()).build());
        }
    }

    public void ja100fDiscovered(String label, String serviceId) {
        JablotronBridgeHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            ThingUID thingUID = new ThingUID(THING_TYPE_JA100F, localBridgeHandler.getThing().getUID(), serviceId);
            Map<String, Object> properties = new HashMap<>();
            properties.put(PROPERTY_SERVICE_ID, serviceId);

            logger.debug("Detected a JA100+ alarm with service id: {}", serviceId);
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_JA100F).withLabel(label)
                    .withProperties(properties).withRepresentationProperty(PROPERTY_SERVICE_ID)
                    .withBridge(localBridgeHandler.getThing().getUID()).build());
        }
    }

    private synchronized void discoverServices() {
        JablotronBridgeHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            List<JablotronDiscoveredService> services = localBridgeHandler.discoverServices();

            if (services == null || services.isEmpty()) {
                logger.debug("Cannot find any Jablotron device");
                return;
            }

            for (JablotronDiscoveredService service : services) {
                String serviceId = String.valueOf(service.getId());
                logger.debug("Found Jablotron service: {} id: {}", service.getName(), serviceId);

                String serviceType = service.getServiceType().toLowerCase();
                if (serviceType.equals(THING_TYPE_OASIS.getId())) {
                    oasisDiscovered("Jablotron OASIS Alarm : " + service.getName(), serviceId);
                } else if (serviceType.equals(THING_TYPE_JA100.getId())) {
                    ja100Discovered("Jablotron JA100 Alarm : " + service.getName(), serviceId);
                } else if (serviceType.equals(THING_TYPE_JA100F.getId())) {
                    ja100fDiscovered("Jablotron JA100+ Alarm : " + service.getName(), serviceId);
                } else {
                    logger.info("Unsupported device type discovered: {} with serviceId: {} and type: {}",
                            service.getName(), serviceId, service.getServiceType());
                    logger.info("Please create a new issue and attach the above information");
                }
            }
        }
    }
}
