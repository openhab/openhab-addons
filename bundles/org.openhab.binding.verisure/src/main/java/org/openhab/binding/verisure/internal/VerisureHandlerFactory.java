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
package org.openhab.binding.verisure.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.verisure.internal.handler.VerisureAlarmThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureBridgeHandler;
import org.openhab.binding.verisure.internal.handler.VerisureBroadbandConnectionThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureClimateDeviceThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureDoorWindowThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureEventLogThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureGatewayThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureMiceDetectionThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureSmartLockThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureSmartPlugThingHandler;
import org.openhab.binding.verisure.internal.handler.VerisureUserPresenceThingHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VerisureHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jarle Hjortland - Initial contribution
 * @author Jan Gustafsson - Further development
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
        SUPPORTED_THING_TYPES.addAll(VerisureMiceDetectionThingHandler.SUPPORTED_THING_TYPES);
        SUPPORTED_THING_TYPES.addAll(VerisureEventLogThingHandler.SUPPORTED_THING_TYPES);
        SUPPORTED_THING_TYPES.addAll(VerisureGatewayThingHandler.SUPPORTED_THING_TYPES);
    }

    private final Logger logger = LoggerFactory.getLogger(VerisureHandlerFactory.class);
    private final HttpClient httpClient;

    @Activate
    public VerisureHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.debug("createHandler this: {}", thing);
        final ThingHandler thingHandler;
        if (VerisureBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("Create VerisureBridgeHandler");
            thingHandler = new VerisureBridgeHandler((Bridge) thing, httpClient);
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
        } else if (VerisureMiceDetectionThingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("Create VerisureMiceDetectionThingHandler {}", thing.getThingTypeUID());
            thingHandler = new VerisureMiceDetectionThingHandler(thing);
        } else if (VerisureEventLogThingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("Create VerisureEventLogThingHandler {}", thing.getThingTypeUID());
            thingHandler = new VerisureEventLogThingHandler(thing);
        } else if (VerisureGatewayThingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("Create VerisureGatewayThingHandler {}", thing.getThingTypeUID());
            thingHandler = new VerisureGatewayThingHandler(thing);
        } else {
            logger.debug("Not possible to create thing handler for thing {}", thing);
            thingHandler = null;
        }
        return thingHandler;
    }
}
