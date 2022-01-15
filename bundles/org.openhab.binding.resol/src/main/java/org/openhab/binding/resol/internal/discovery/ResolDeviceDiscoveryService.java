/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.resol.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.resol.handler.ResolBridgeHandler;
import org.openhab.binding.resol.internal.ResolBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ResolDeviceDiscoveryService} class handles the discovery of things.
 *
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class ResolDeviceDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private static final String THING_PROPERTY_TYPE = "type";

    private final Logger logger = LoggerFactory.getLogger(ResolDeviceDiscoveryService.class);

    private @Nullable ResolBridgeHandler resolBridgeHandler;

    public ResolDeviceDiscoveryService() throws IllegalArgumentException {
        super(Set.of(ResolBindingConstants.THING_TYPE_UID_DEVICE), 15, false);
    }

    public void addThing(ThingUID bridgeUID, String thingType, String type, String name) {
        logger.trace("Adding new Resol thing: {}", type);
        ThingUID thingUID = null;
        switch (thingType) {
            case ResolBindingConstants.THING_ID_DEVICE:
                thingUID = new ThingUID(ResolBindingConstants.THING_TYPE_UID_DEVICE, bridgeUID, type);
                break;
        }

        if (thingUID != null) {
            logger.trace("Adding new Discovery thingType: {} bridgeType: {}", thingUID.getAsString(),
                    bridgeUID.getAsString());

            Map<String, Object> properties = new HashMap<>(1);
            properties.put(THING_PROPERTY_TYPE, type);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withRepresentationProperty(THING_PROPERTY_TYPE).withProperties(properties).withLabel(name).build();
            logger.trace("call register: {} label: {}", discoveryResult.getBindingId(), discoveryResult.getLabel());
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Discovered Thing is unsupported: type '{}'", type);
        }
    }

    @Override
    public void activate() {
        ResolBridgeHandler resolBridgeHandler = this.resolBridgeHandler;
        if (resolBridgeHandler != null) {
            resolBridgeHandler.registerDiscoveryService(this);
        }
    }

    @Override
    public void deactivate() {
        ResolBridgeHandler resolBridgeHandler = this.resolBridgeHandler;
        if (resolBridgeHandler != null) {
            resolBridgeHandler.unregisterDiscoveryService();
        }
    }

    @Override
    protected void startScan() {
        ResolBridgeHandler resolBridgeHandler = this.resolBridgeHandler;
        if (resolBridgeHandler != null) {
            resolBridgeHandler.startScan();
        }
    }

    @Override
    protected void stopScan() {
        ResolBridgeHandler resolBridgeHandler = this.resolBridgeHandler;
        if (resolBridgeHandler != null) {
            resolBridgeHandler.stopScan();
        }
        super.stopScan();
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof ResolBridgeHandler) {
            resolBridgeHandler = (ResolBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return resolBridgeHandler;
    }
}
