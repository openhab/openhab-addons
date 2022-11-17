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
package org.openhab.binding.emby.internal.discovery;

import static org.openhab.binding.emby.internal.EmbyBindingConstants.DEVICE_ID;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.THING_TYPE_EMBY_DEVICE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emby.internal.handler.EmbyBridgeHandler;
import org.openhab.binding.emby.internal.model.EmbyPlayStateModel;
import org.openhab.binding.emby.internal.protocol.EmbyDeviceEncoder;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@EmbyClientDiscoveryService} handles the discovery of devices which are playing media on an emby server which
 * has been setup as a bridge. This discovery service receives events from the corresponding {@EmbyBridgeHandler} that
 * it is attached to
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class EmbyClientDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(EmbyClientDiscoveryService.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_EMBY_DEVICE);
    private final EmbyBridgeHandler embyBridgeHandler;

    public EmbyClientDiscoveryService(EmbyBridgeHandler embyBridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, 10, true);
        this.embyBridgeHandler = embyBridgeHandler;
    }

    public void activate() {
        embyBridgeHandler.registerDeviceFoundListener(this);
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
            ThingUID bridgeUID = embyBridgeHandler.getThing().getUID();
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(DEVICE_ID, modelId);
            logger.debug("Disovered device {} with id {}", playstate.getDeviceName(), modelId);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID).withRepresentationProperty(DEVICE_ID)
                    .withLabel(playstate.getDeviceName()).build();
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("discovered unsupported device of type '{}' and model '{}' with id {}",
                    playstate.getDeviceName(), modelId, playstate.getDeviceId());
        }
    }

    private @Nullable ThingUID getThingUID(EmbyPlayStateModel playstate) {
        ThingUID bridgeUID = embyBridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = THING_TYPE_EMBY_DEVICE;
        if (playstate.getDeviceId() != null) {
            return new ThingUID(thingTypeUID, bridgeUID, playstate.getDeviceId());
        } else {
            return null;
        }
    }
}
