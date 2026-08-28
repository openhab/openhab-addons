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
package org.openhab.binding.dreame.internal.discovery;

import static org.openhab.binding.dreame.internal.DreameBindingConstants.THING_TYPE_MOWER;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dreame.internal.handler.DreameAccountHandler;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.binding.dreame.internal.util.DreameDiagnostics;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds mowers returned by a Dreamehome account to the inbox.
 *
 * @author Ronny Grun - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = DreameMowerDiscoveryService.class, configurationPid = "discovery.dreame")
@NonNullByDefault
public class DreameMowerDiscoveryService extends AbstractThingHandlerDiscoveryService<DreameAccountHandler>
        implements ThingHandlerService {
    private static final int DISCOVERY_TIMEOUT_SECONDS = 10;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_MOWER);

    private final Logger logger = LoggerFactory.getLogger(DreameMowerDiscoveryService.class);

    @Activate
    public DreameMowerDiscoveryService() {
        super(DreameAccountHandler.class, SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT_SECONDS, true);
    }

    @Override
    public void initialize() {
        thingHandler.setDiscoveryService(this);
        super.initialize();
        discoverDevices();
    }

    @Override
    protected void startScan() {
        discoverDevices();
    }

    public void discoverDevices() {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        for (DreameDevice device : thingHandler.getDevices()) {
            ThingUID thingUID = new ThingUID(THING_TYPE_MOWER, bridgeUID, thingId(device.id()));
            thingDiscovered(DiscoveryResultBuilder
                    .create(thingUID).withBridge(bridgeUID).withLabel(device.name()).withProperties(Map.of("deviceId",
                            device.id(), "model", device.model(), "firmwareVersion", device.version()))
                    .withRepresentationProperty("deviceId").build());
            logger.debug("Discovered mower {} ({})", DreameDiagnostics.maskIdentifier(device.id()), device.model());
        }
    }

    @Override
    public void dispose() {
        thingHandler.setDiscoveryService(null);
        super.dispose();
    }

    static String thingId(String deviceId) {
        String normalized = deviceId.replaceAll("[^A-Za-z0-9_-]", "_");
        if (normalized.startsWith("-")) {
            normalized = "n" + normalized.substring(1);
        }
        return "mower-" + normalized;
    }
}
