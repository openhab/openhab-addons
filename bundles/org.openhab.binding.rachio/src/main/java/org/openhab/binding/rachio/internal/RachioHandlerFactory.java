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
package org.openhab.binding.rachio.internal;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioApi;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.handler.AbstractRachioBridgeHandler;
import org.openhab.binding.rachio.internal.handler.AbstractRachioThingHandler;
import org.openhab.binding.rachio.internal.handler.RachioBaseStationHandler;
import org.openhab.binding.rachio.internal.handler.RachioBridgeHandler;
import org.openhab.binding.rachio.internal.handler.RachioDeviceHandler;
import org.openhab.binding.rachio.internal.handler.RachioFlexScheduleHandler;
import org.openhab.binding.rachio.internal.handler.RachioScheduleHandler;
import org.openhab.binding.rachio.internal.handler.RachioValveHandler;
import org.openhab.binding.rachio.internal.handler.RachioValveProgramHandler;
import org.openhab.binding.rachio.internal.handler.RachioZoneHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RachioHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class, RachioHandlerFactory.class }, immediate = true)
public class RachioHandlerFactory extends BaseThingHandlerFactory {

    public class RachioBridge {
        @Nullable
        RachioBridgeHandler cloudHandler;
        @Nullable
        ThingUID uid;
    }

    private final Logger logger = LoggerFactory.getLogger(RachioHandlerFactory.class);
    private final Map<String, RachioBridge> bridgeList = new ConcurrentHashMap<>();

    RachioHandlerFactory() {
        logger.debug("RachioHandlerFactory: Initialized Rachio Thing handler.");
    }

    /**
     * OSGi activation callback.
     *
     */
    @Activate
    public RachioHandlerFactory(ComponentContext componentContext) {
        super.activate(componentContext);
        logger.debug("RachioHandlerFactory: Initialized Rachio Thing handler.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        try {
            ThingTypeUID thingTypeUID = thing.getThingTypeUID();
            logger.debug("RachioHandlerFactory: Create thing handler for type {}", thingTypeUID.toString());
            if (SUPPORTED_BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)) {
                return createBridge((Bridge) thing);
            } else if (SUPPORTED_ZONE_THING_TYPES_UIDS.contains(thingTypeUID)) {
                return createZone(thing);
            } else if (thingTypeUID.equals(THING_TYPE_SCHEDULE)) {
                return createSchedule(thing);
            } else if (thingTypeUID.equals(THING_TYPE_FLEX_SCHEDULE)) {
                return createFlexSchedule(thing);
            } else if (thingTypeUID.equals(THING_TYPE_BASE_STATION)) {
                return createBaseStation(thing);
            } else if (thingTypeUID.equals(THING_TYPE_VALVE)) {
                return createValve(thing);
            } else if (thingTypeUID.equals(THING_TYPE_VALVE_PROGRAM)) {
                return createValveProgram(thing);
            } else if (SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUID)) {
                return createDevice(thing);
            }
        } catch (RuntimeException e) {
            logger.debug("RachioHandlerFactory: Exception while creating Rachio Thing handler: {}", e.toString());
        }

        logger.debug("RachioHandlerFactory: Unable to create thing handler");
        return null;
    }

    @Override
    protected void removeHandler(final ThingHandler thingHandler) {
        logger.debug("Removing Rachio handler");
        if (thingHandler instanceof AbstractRachioBridgeHandler bridgeHandler) {
            bridgeHandler.shutdown();
        }
        if (thingHandler instanceof RachioBridgeHandler bridgeHandler) {
            bridgeList.remove(bridgeHandler.getThing().getUID().toString());
        }
        if (thingHandler instanceof AbstractRachioThingHandler rachioThingHandler) {
            rachioThingHandler.shutdown();
        }
    }

    /**
     * Called from the webhook servlet. event.externalId is used to route the event to the corresponding bridge handler
     *
     * @param ipAddress source IP address from the servlet request
     * @param event parsed webhook event
     */
    public boolean webHookEvent(String ipAddress, RachioEventGsonDTO event) {
        try {
            logger.debug("RachioCloud: Event for device {} received", event.deviceName);

            // process event parameters
            for (Map.Entry<String, RachioBridge> be : bridgeList.entrySet()) {
                RachioBridge bridge = be.getValue();
                RachioBridgeHandler cloudHandler = bridge.cloudHandler;
                if (cloudHandler == null) {
                    continue;
                }
                @Nullable
                String externalId = cloudHandler.getExternalId();
                if (externalId != null && externalId.equals(event.externalId)) {
                    return cloudHandler.webHookEvent(event);
                }
            }

            // invalid externalId, could be an indicator for unauthorized access
            logger.warn("RachioCloud: Unauthorized webhook event (wrong externalId: {}, source ip: {})",
                    event.externalId, ipAddress);
            return false;
        } catch (RuntimeException e) {
            logger.debug("RachioCloud: Unable to process event", e);
        }

        logger.debug("RachioCloud: Unable to route event to bridge, externalId={}, deviceId={}", event.externalId,
                event.deviceId);
        return false;
    }

    public boolean isValidWebHookSignature(@Nullable String signature, byte[] requestBody) {
        boolean apiKeyAvailable = false;

        for (Map.Entry<String, RachioBridge> be : bridgeList.entrySet()) {
            RachioBridge bridge = be.getValue();
            RachioBridgeHandler cloudHandler = bridge.cloudHandler;
            if (cloudHandler == null) {
                continue;
            }
            String apikey = cloudHandler.getApiKey();
            if (apikey.isEmpty()) {
                continue;
            }
            apiKeyAvailable = true;
            if (RachioApi.isValidWebHookSignature(signature, requestBody, apikey)) {
                return true;
            }
        }

        if (!apiKeyAvailable) {
            logger.warn("RachioCloud: Unable to validate webhook signature because no API key is configured");
        }
        return false;
    }

    private @Nullable RachioBridgeHandler createBridge(Bridge bridgeThing) {
        try {
            RachioBridge bridge = new RachioBridge();
            ThingUID bridgeUID = bridgeThing.getUID();
            RachioBridgeHandler cloudHandler = new RachioBridgeHandler(bridgeThing);
            bridge.uid = bridgeUID;
            bridge.cloudHandler = cloudHandler;
            bridgeList.put(bridgeUID.toString(), bridge);
            return cloudHandler;
        } catch (RuntimeException e) {
            logger.warn("RachioFactory: Unable to create bridge thing: {}: ", e.getMessage());
        }
        return null;
    }

    private RachioDeviceHandler createDevice(Thing thing) {
        return new RachioDeviceHandler(thing);
    }

    private RachioZoneHandler createZone(Thing thing) {
        logger.debug("Zone handler created: thingUid={}, bridgeUid={}", thing.getUID(), thing.getBridgeUID());
        return new RachioZoneHandler(thing);
    }

    private RachioScheduleHandler createSchedule(Thing thing) {
        logger.debug("Schedule handler created: thingUid={}, bridgeUid={}", thing.getUID(), thing.getBridgeUID());
        return new RachioScheduleHandler(thing);
    }

    private RachioFlexScheduleHandler createFlexSchedule(Thing thing) {
        logger.debug("Flex schedule handler created: thingUid={}, bridgeUid={}", thing.getUID(), thing.getBridgeUID());
        return new RachioFlexScheduleHandler(thing);
    }

    private RachioBaseStationHandler createBaseStation(Thing thing) {
        logger.debug("BaseStation handler created: thingUid={}, bridgeUid={}", thing.getUID(), thing.getBridgeUID());
        return new RachioBaseStationHandler(thing);
    }

    private RachioValveHandler createValve(Thing thing) {
        logger.debug("Valve handler created: thingUid={}, bridgeUid={}", thing.getUID(), thing.getBridgeUID());
        return new RachioValveHandler(thing);
    }

    private RachioValveProgramHandler createValveProgram(Thing thing) {
        logger.debug("Valve Program handler created: thingUid={}, bridgeUid={}", thing.getUID(), thing.getBridgeUID());
        return new RachioValveProgramHandler(thing);
    }
}
