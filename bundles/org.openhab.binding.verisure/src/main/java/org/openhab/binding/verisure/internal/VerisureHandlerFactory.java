/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.verisure.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.verisure.internal.discovery.VerisureThingDiscoveryService;
import org.openhab.binding.verisure.internal.handler.VerisureAlarmThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureBridgeHandler;
import org.openhab.binding.verisure.internal.handler.VerisureBroadbandConnectionThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureClimateDeviceThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureDoorWindowThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureSmartLockThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureSmartPlugThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureUserPresenceThingHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VerisureHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jarle Hjortland - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.verisure")
public class VerisureHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>();
    static {
        SUPPORTED_THING_TYPES.addAll(VerisureBridgeHandler.SUPPORTED_THING_TYPES);
        SUPPORTED_THING_TYPES.addAll(VerisureAlarmThingHandler.SUPPORTED_THING_TYPES);
        SUPPORTED_THING_TYPES.addAll(VerisureSmartLockThingHandler.SUPPORTED_THING_TYPES);
        SUPPORTED_THING_TYPES.addAll(VerisureSmartPlugThingHandler.SUPPORTED_THING_TYPES);
        SUPPORTED_THING_TYPES.addAll(VerisureClimateDeviceThingHandler.SUPPORTED_THING_TYPES);
        SUPPORTED_THING_TYPES.addAll(VerisureBroadbandConnectionThingHandler.SUPPORTED_THING_TYPES);
        SUPPORTED_THING_TYPES.addAll(VerisureDoorWindowThingHandler.SUPPORTED_THING_TYPES);
        SUPPORTED_THING_TYPES.addAll(VerisureUserPresenceThingHandler.SUPPORTED_THING_TYPES);
    }

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(VerisureHandlerFactory.class);

    private @NonNullByDefault({}) HttpClient httpClient;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    public VerisureHandlerFactory() {
        super();
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.debug("createHandler this: {}", thing);
        final ThingHandler thingHandler;
        if (VerisureBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("Create VerisureBridgeHandler");
            thingHandler = new VerisureBridgeHandler((Bridge) thing, httpClient);
            registerObjectDiscoveryService((VerisureBridgeHandler) thingHandler);
        } else if (VerisureAlarmThingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("Create VerisureAlarmThingHandler {}", thing.getThingTypeUID());
            thingHandler = new VerisureAlarmThingHandler(thing);
        } else if (VerisureSmartLockThingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("Create VerisureSmartLockThingHandler {}", thing.getThingTypeUID());
            thingHandler = new VerisureSmartLockThingHandler(thing);
        } else if (VerisureSmartPlugThingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("Create VerisureSmartPlugThingHandler {}", thing.getThingTypeUID());
            thingHandler = new VerisureSmartPlugThingHandler(thing);
        } else if (VerisureClimateDeviceThingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("Create VerisureClimateDeviceThingHandler {}", thing.getThingTypeUID());
            thingHandler = new VerisureClimateDeviceThingHandler(thing);
        } else if (VerisureBroadbandConnectionThingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("Create VerisureBroadbandConnectionThingHandler {}", thing.getThingTypeUID());
            thingHandler = new VerisureBroadbandConnectionThingHandler(thing);
        } else if (VerisureDoorWindowThingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("Create VerisureDoorWindowThingHandler {}", thing.getThingTypeUID());
            thingHandler = new VerisureDoorWindowThingHandler(thing);
        } else if (VerisureUserPresenceThingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("Create VerisureUserPresenceThingHandler {}", thing.getThingTypeUID());
            thingHandler = new VerisureUserPresenceThingHandler(thing);
        } else {
            logger.debug("Not possible to create thing handler for thing {}", thing);
            thingHandler = null;
        }
        return thingHandler;
    }

    private synchronized void registerObjectDiscoveryService(VerisureBridgeHandler bridgeHandler) {
        VerisureThingDiscoveryService discoveryService = new VerisureThingDiscoveryService(bridgeHandler);
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        logger.debug("setHttpClientFactory this: {}", this);
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        logger.debug("unsetHttpClientFactory this: {}", this);
        this.httpClient = null;
    }

}
