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
package org.openhab.binding.nest.internal.wwn;

import static org.openhab.binding.nest.internal.wwn.WWNBindingConstants.*;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.wwn.handler.WWNAccountHandler;
import org.openhab.binding.nest.internal.wwn.handler.WWNCameraHandler;
import org.openhab.binding.nest.internal.wwn.handler.WWNSmokeDetectorHandler;
import org.openhab.binding.nest.internal.wwn.handler.WWNStructureHandler;
import org.openhab.binding.nest.internal.wwn.handler.WWNThermostatHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;

/**
 * The {@link WWNThingHandlerFactory} is responsible for creating WWN thing handlers.
 *
 * @author David Bennett - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.nest")
public class WWNThingHandlerFactory extends BaseThingHandlerFactory {

    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;

    @Activate
    public WWNThingHandlerFactory(@Reference ClientBuilder clientBuilder,
            @Reference SseEventSourceFactory eventSourceFactory) {
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
    }

    /**
     * The things this factory supports creating.
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Creates a handler for the specific thing. This also creates the discovery service when the bridge is created.
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            return new WWNAccountHandler((Bridge) thing, clientBuilder, eventSourceFactory);
        } else if (THING_TYPE_CAMERA.equals(thingTypeUID)) {
            return new WWNCameraHandler(thing);
        } else if (THING_TYPE_SMOKE_DETECTOR.equals(thingTypeUID)) {
            return new WWNSmokeDetectorHandler(thing);
        } else if (THING_TYPE_STRUCTURE.equals(thingTypeUID)) {
            return new WWNStructureHandler(thing);
        } else if (THING_TYPE_THERMOSTAT.equals(thingTypeUID)) {
            return new WWNThermostatHandler(thing);
        }

        return null;
    }
}
