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
package org.openhab.binding.dbquery.internal;

import static org.openhab.binding.dbquery.internal.DBQueryBindingConstants.THING_TYPE_INFLUXDB2_BRIDGE;
import static org.openhab.binding.dbquery.internal.DBQueryBindingConstants.THING_TYPE_QUERY;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * DBQuery binding factory that is responsible for creating things and thing handlers.
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.dbquery", service = ThingHandlerFactory.class)
public class DBQueryHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_INFLUXDB2_BRIDGE,
            THING_TYPE_QUERY);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_QUERY.equals(thingTypeUID)) {
            return new QueryHandler(thing);
        } else if (THING_TYPE_INFLUXDB2_BRIDGE.equals(thingTypeUID)) {
            return new InfluxDB2BridgeHandler((Bridge) thing);
        } else {
            return null;
        }
    }
}
