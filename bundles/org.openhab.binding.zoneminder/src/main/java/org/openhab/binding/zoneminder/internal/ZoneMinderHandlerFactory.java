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
package org.openhab.binding.zoneminder.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.zoneminder.internal.handler.ZoneMinderServerBridgeHandler;
import org.openhab.binding.zoneminder.internal.handler.ZoneMinderThingMonitorHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZoneMinderHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin S. Eskildsen - Initial contribution
 *
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.zoneminder")
public class ZoneMinderHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(ZoneMinderHandlerFactory.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .unmodifiableSet(Stream.concat(ZoneMinderServerBridgeHandler.SUPPORTED_THING_TYPES.stream(),
                    ZoneMinderThingMonitorHandler.SUPPORTED_THING_TYPES.stream()).collect(Collectors.toSet()));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(ZoneMinderConstants.THING_TYPE_BRIDGE_ZONEMINDER_SERVER)) {
            logger.debug("[FACTORY]: creating handler for bridge thing '{}'", thing);
            ZoneMinderServerBridgeHandler bridge = new ZoneMinderServerBridgeHandler((Bridge) thing);

            return bridge;
        } else if (thingTypeUID.equals(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR)) {
            return new ZoneMinderThingMonitorHandler(thing);
        }

        return null;
    }

}
