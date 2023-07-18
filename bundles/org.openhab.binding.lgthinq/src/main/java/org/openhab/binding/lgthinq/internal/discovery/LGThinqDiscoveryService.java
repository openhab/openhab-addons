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
package org.openhab.binding.lgthinq.internal.discovery;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;
import static org.openhab.core.thing.Thing.PROPERTY_MODEL_ID;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.errors.LGThinqException;
import org.openhab.binding.lgthinq.internal.handler.LGThinQBridgeHandler;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientServiceFactory;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientServiceFactory.LGThinQGeneralApiClientService;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinqDiscoveryService}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinqDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(LGThinqDiscoveryService.class);
    private @Nullable LGThinQBridgeHandler bridgeHandler;
    private @Nullable ThingUID bridgeHandlerUID;
    private @Nullable LGThinQGeneralApiClientService lgApiClientService;

    public LGThinqDiscoveryService() {
        super(SUPPORTED_THING_TYPES, SEARCH_TIME);
    }

    @Override
    protected void startScan() {
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof LGThinQBridgeHandler) {
            bridgeHandler = (LGThinQBridgeHandler) handler;
            bridgeHandlerUID = handler.getThing().getUID();
            lgApiClientService = LGThinQApiClientServiceFactory.newGeneralApiClientService(bridgeHandler.getHttpClientFactory());
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void activate() {
        if (bridgeHandler != null) {
            bridgeHandler.registerDiscoveryListener(this);
        }
    }

    @Override
    public void deactivate() {
        ThingHandlerService.super.deactivate();
    }

    public void removeLgDeviceDiscovery(LGDevice device) {
        try {
            ThingUID thingUID = getThingUID(device);
            thingRemoved(thingUID);
        } catch (LGThinqException e) {
            logger.error("Error getting Thing UID");
        }
    }

    public void addLgDeviceDiscovery(LGDevice device) {
        String modelId = device.getModelName();
        ThingUID thingUID;
        ThingTypeUID thingTypeUID;
        try {
            // load capability to cache and troubleshooting
            lgApiClientService.loadDeviceCapability(device.getDeviceId(), device.getModelJsonUri(), false);
            thingUID = getThingUID(device);
            thingTypeUID = getThingTypeUID(device);
        } catch (LGThinqException e) {
            logger.debug("Discovered unsupported LG device of type '{}'({}) and model '{}' with id {}",
                    device.getDeviceType(), device.getDeviceTypeId(), modelId, device.getDeviceId());
            return;
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put(DEVICE_ID, device.getDeviceId());
        properties.put(DEVICE_ALIAS, device.getAlias());
        properties.put(MODEL_URL_INFO, device.getModelJsonUri());
        properties.put(PLATFORM_TYPE, device.getPlatformType());
        properties.put(PROPERTY_MODEL_ID, modelId);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperties(properties).withBridge(bridgeHandlerUID).withRepresentationProperty(DEVICE_ID)
                .withLabel(device.getAlias()).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID getThingUID(LGDevice device) throws LGThinqException {
        ThingTypeUID thingTypeUID = getThingTypeUID(device);
        return new ThingUID(thingTypeUID,
                Objects.requireNonNull(bridgeHandlerUID, "bridgeHandleUid should never be null here"),
                device.getDeviceId());
    }

    private ThingTypeUID getThingTypeUID(LGDevice device) throws LGThinqException {
        // Short switch, but is here because it is going to be increase after new LG Devices were added
        switch (device.getDeviceType()) {
            case AIR_CONDITIONER:
                return THING_TYPE_AIR_CONDITIONER;
            case HEAT_PUMP:
                return THING_TYPE_HEAT_PUMP;
            case WASHERDRYER_MACHINE:
                return THING_TYPE_WASHING_MACHINE;
            case WASHING_TOWER:
                return THING_TYPE_WASHING_TOWER;
            case DRYER_TOWER:
                return THING_TYPE_DRYER_TOWER;
            case DRYER:
                return THING_TYPE_DRYER;
            case REFRIGERATOR:
                return THING_TYPE_FRIDGE;
            default:
                throw new LGThinqException(String.format("device type [%s] not supported", device.getDeviceType()));
        }
    }
}
