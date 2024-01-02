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
package org.openhab.binding.mcd.internal;

import static org.openhab.binding.mcd.internal.McdBindingConstants.THING_TYPE_MCD_BRIDGE;
import static org.openhab.binding.mcd.internal.McdBindingConstants.THING_TYPE_SENSOR;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.mcd.internal.handler.McdBridgeHandler;
import org.openhab.binding.mcd.internal.handler.SensorThingHandler;
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
 * The {@link McdHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Simon Dengler - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.mcd", service = ThingHandlerFactory.class)
public class McdHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_MCD_BRIDGE,
            THING_TYPE_SENSOR);
    private final HttpClient httpClient;

    @Activate
    public McdHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_MCD_BRIDGE.equals(thingTypeUID)) {
            return new McdBridgeHandler((Bridge) thing, httpClient);
        }

        if (THING_TYPE_SENSOR.equals(thingTypeUID)) {
            return new SensorThingHandler(thing, httpClient);
        }

        return null;
    }
}
