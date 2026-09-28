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

import static org.openhab.binding.dreame.internal.DreameBindingConstants.THING_TYPE_VACUUM;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dreame.internal.handler.DreameAccountHandler;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds vacuum candidates returned by a Dreamehome account to the inbox.
 *
 * @author Ronny Grun - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = DreameVacuumDiscoveryService.class, configurationPid = "discovery.dreame.vacuum")
@NonNullByDefault
public class DreameVacuumDiscoveryService extends AbstractThingHandlerDiscoveryService<DreameAccountHandler>
        implements ThingHandlerService {
    private static final int DISCOVERY_TIMEOUT_SECONDS = 10;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_VACUUM);

    private final Logger logger = LoggerFactory.getLogger(DreameVacuumDiscoveryService.class);

    @Activate
    public DreameVacuumDiscoveryService() {
        super(DreameAccountHandler.class, SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT_SECONDS, true);
    }

    @Override
    public void initialize() {
        thingHandler.setVacuumDiscoveryService(this);
        super.initialize();
        discoverDevices();
    }

    @Override
    protected void startScan() {
        discoverDevices();
    }

    public void discoverDevices() {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        for (DreameDevice device : thingHandler.getVacuumDevices()) {
            ThingUID thingUID = new ThingUID(THING_TYPE_VACUUM, bridgeUID, thingId(device.id()));
            if (isAlreadyConfigured(thingHandler.getThing().getThings(), thingUID, device.id())) {
                thingRemoved(thingUID);
                logger.trace("Skipping already configured vacuum model {}", device.model());
                continue;
            }
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel("Dreame Vacuum")
                    .withProperties(Map.of("deviceId", device.id(), "model", device.model()))
                    .withRepresentationProperty("deviceId").build());
            logger.debug("Discovered vacuum model {}", device.model());
        }
    }

    @Override
    public void dispose() {
        thingHandler.setVacuumDiscoveryService(null);
        super.dispose();
    }

    static boolean isAlreadyConfigured(Iterable<Thing> things, ThingUID discoveredThingUID, String deviceId) {
        for (Thing thing : things) {
            if (discoveredThingUID.equals(thing.getUID())
                    || deviceId.equals(thing.getConfiguration().get("deviceId"))) {
                return true;
            }
        }
        return false;
    }

    static String thingId(String deviceId) {
        return "vacuum-" + UUID.nameUUIDFromBytes(deviceId.getBytes(StandardCharsets.UTF_8));
    }
}
