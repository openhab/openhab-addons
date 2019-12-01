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
package org.openhab.binding.touchwand.internal;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link TouchWandHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Roie Geron - Initial contribution
 */

@Component(configurationPid = "binding.touchwand", service = ThingHandlerFactory.class)
@NonNullByDefault
public class TouchWandHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream
                    .of(TouchWandSwitchHandler.SUPPORTED_THING_TYPES.stream(),
                            TouchWandShutterHandler.SUPPORTED_THING_TYPES.stream(),
                            TouchWandBridgeHandler.SUPPORTED_THING_TYPES.stream())
                    .flatMap(i -> i).collect(Collectors.toSet()));

    private @Nullable HttpClient httpClient;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new TouchWandBridgeHandler((Bridge) thing, httpClient, bundleContext);
        } else if (THING_TYPE_SWITCH.equals(thingTypeUID)) {
            return new TouchWandSwitchHandler(thing);
        } else if (THING_TYPE_SHUTTER.equals(thingTypeUID)) {
            return new TouchWandShutterHandler(thing);
        } else if (THING_TYPE_WALLCONTROLLER.equals(thingTypeUID)) {
            return new TouchWandWallControllerHandler(thing);
        }

        return null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }

}
