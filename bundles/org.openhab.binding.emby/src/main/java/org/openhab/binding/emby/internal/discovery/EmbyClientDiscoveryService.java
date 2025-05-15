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
package org.openhab.binding.emby.internal.discovery;

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CONFIG_DEVICE_ID;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.THING_TYPE_EMBY_DEVICE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emby.internal.handler.EmbyBridgeHandler;
import org.openhab.binding.emby.internal.model.EmbyPlayStateModel;
import org.openhab.binding.emby.internal.protocol.EmbyDeviceEncoder;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@EmbyClientDiscoveryService} handles the discovery of devices which are playing media on an emby server which
 * has been setup as a bridge. This discovery service receives events from the corresponding {@EmbyBridgeHandler} that
 * it is attached to
 *
 * @author Zachary Christiansen - Initial contribution
 */
@Component(service = DiscoveryService.class, factory = "emby:client", configurationPid = "discovery.embydevice", property = {
        "discovery.interval:Integer=0", "thingTypeUIDs=emby:device" })
@NonNullByDefault
public class EmbyClientDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(EmbyClientDiscoveryService.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_EMBY_DEVICE);
    // DS will inject the one-and-only bridge handler whose thingUID matches
    private @Nullable EmbyBridgeHandler embyBridgeHandler;

    public EmbyClientDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 0, true);
    }

    /*
     * ------------------------------------------------------------------
     * Called once by EmbyHandlerFactory immediately after it creates the
     * discovery service instance.
     * ------------------------------------------------------------------
     */
    public void setBridge(EmbyBridgeHandler handler) {
        this.embyBridgeHandler = handler;
    }

    /*
     * ------------------------------------------------------------------
     * Called by the factory just before it disposes the ComponentInstance.
     * ------------------------------------------------------------------
     */
    public void clearBridge(EmbyBridgeHandler handler) {
        if (Objects.equals(this.embyBridgeHandler, handler)) {
            this.embyBridgeHandler = null;
        }
    }

    @Override
    public void startScan() {
        // this discovery service does not do any scanning all of the scanning is handled by the bridge handler and
        // passed in to this service
    }

    public void addDeviceIDDiscover(EmbyPlayStateModel playstate) {
        logger.debug("adding new emby device");
        ThingUID thingUID = getThingUID(playstate);
        ThingTypeUID thingTypeUID = THING_TYPE_EMBY_DEVICE;
        EmbyDeviceEncoder encode = new EmbyDeviceEncoder();
        String modelId = encode.encodeDeviceID(playstate.getDeviceId());
        if (thingUID != null) {
            ThingUID bridgeUID = requireNonNull(embyBridgeHandler,
                    "EmbyClientDiscoveryService: Bridge Handler Cannot be null").getThing().getUID();
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(CONFIG_DEVICE_ID, modelId);
            logger.debug("Disovered device {} with id {}", playstate.getDeviceName(), modelId);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID).withRepresentationProperty(CONFIG_DEVICE_ID)
                    .withLabel(playstate.getDeviceName()).build();
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("discovered unsupported device of type '{}' and model '{}' with id {}",
                    playstate.getDeviceName(), modelId, playstate.getDeviceId());
        }
    }

    private @Nullable ThingUID getThingUID(EmbyPlayStateModel playstate) {
        ThingUID bridgeUID = requireNonNull(embyBridgeHandler,
                "EmbyClientDiscoveryService: Bridge Handler Cannot be null").getThing().getUID();
        ThingTypeUID thingTypeUID = THING_TYPE_EMBY_DEVICE;
        return new ThingUID(thingTypeUID, bridgeUID, playstate.getDeviceId());
    }
}
