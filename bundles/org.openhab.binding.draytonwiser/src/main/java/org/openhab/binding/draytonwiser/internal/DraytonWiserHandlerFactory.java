/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.draytonwiser.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.draytonwiser.internal.handler.ControllerHandler;
import org.openhab.binding.draytonwiser.internal.handler.HeatHubHandler;
import org.openhab.binding.draytonwiser.internal.handler.HotWaterHandler;
import org.openhab.binding.draytonwiser.internal.handler.RoomHandler;
import org.openhab.binding.draytonwiser.internal.handler.RoomStatHandler;
import org.openhab.binding.draytonwiser.internal.handler.SmartPlugHandler;
import org.openhab.binding.draytonwiser.internal.handler.TRVHandler;
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

/**
 * The {@link DraytonWiserHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andrew Schofield - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.draytonwiser")
@NonNullByDefault
public class DraytonWiserHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;

    @Activate
    public DraytonWiserHandlerFactory(@Reference final HttpClientFactory factory) {
        httpClient = factory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return DraytonWiserBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (DraytonWiserBindingConstants.THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new HeatHubHandler((Bridge) thing, httpClient);
        } else if (DraytonWiserBindingConstants.THING_TYPE_ROOM.equals(thingTypeUID)) {
            return new RoomHandler(thing);
        } else if (DraytonWiserBindingConstants.THING_TYPE_ROOMSTAT.equals(thingTypeUID)) {
            return new RoomStatHandler(thing);
        } else if (DraytonWiserBindingConstants.THING_TYPE_ITRV.equals(thingTypeUID)) {
            return new TRVHandler(thing);
        } else if (DraytonWiserBindingConstants.THING_TYPE_CONTROLLER.equals(thingTypeUID)) {
            return new ControllerHandler(thing);
        } else if (DraytonWiserBindingConstants.THING_TYPE_HOTWATER.equals(thingTypeUID)) {
            return new HotWaterHandler(thing);
        } else if (DraytonWiserBindingConstants.THING_TYPE_SMARTPLUG.equals(thingTypeUID)) {
            return new SmartPlugHandler(thing);
        }

        return null;
    }
}
