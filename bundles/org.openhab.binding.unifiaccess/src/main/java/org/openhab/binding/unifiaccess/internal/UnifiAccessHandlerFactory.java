/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifiaccess.internal;

import static org.openhab.binding.unifiaccess.internal.UnifiAccessBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifiaccess.internal.handler.UnifiAccessBridgeHandler;
import org.openhab.binding.unifiaccess.internal.handler.UnifiAccessDeviceHandler;
import org.openhab.binding.unifiaccess.internal.handler.UnifiAccessDoorHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link UnifiAccessHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.unifiaccess", service = ThingHandlerFactory.class)
public class UnifiAccessHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BRIDGE_THING_TYPE, DOOR_THING_TYPE,
            DEVICE_THING_TYPE);

    private @Nullable HttpClientFactory httpClientFactory;

    @Reference
    protected void setHttpClientFactory(HttpClientFactory factory) {
        this.httpClientFactory = factory;
    }

    protected void unsetHttpClientFactory(HttpClientFactory factory) {
        if (factory.equals(this.httpClientFactory)) {
            this.httpClientFactory = null;
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_THING_TYPE.equals(thingTypeUID)) {
            HttpClientFactory httpClientFactory = this.httpClientFactory;
            if (httpClientFactory == null) {
                return null;
            }
            return new UnifiAccessBridgeHandler((Bridge) thing, httpClientFactory);
        }

        if (DOOR_THING_TYPE.equals(thingTypeUID)) {
            return new UnifiAccessDoorHandler(thing);
        }

        if (DEVICE_THING_TYPE.equals(thingTypeUID)) {
            return new UnifiAccessDeviceHandler(thing);
        }

        return null;
    }
}
