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
package org.openhab.binding.juicenet.internal;

import static org.openhab.binding.juicenet.internal.JuiceNetBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.juicenet.internal.handler.JuiceNetBridgeHandler;
import org.openhab.binding.juicenet.internal.handler.JuiceNetDeviceHandler;
import org.openhab.core.i18n.TimeZoneProvider;
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
 * The {@link JuiceNetHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.juicenet", service = ThingHandlerFactory.class)
public class JuiceNetHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BRIDGE_THING_TYPE, DEVICE_THING_TYPE);
    private final HttpClientFactory httpClientFactory;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public JuiceNetHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference TimeZoneProvider timeZoneProvider) {
        this.httpClientFactory = httpClientFactory;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(BRIDGE_THING_TYPE)) {
            return new JuiceNetBridgeHandler((Bridge) thing, httpClientFactory.getCommonHttpClient());
        } else if (thingTypeUID.equals(DEVICE_THING_TYPE)) {
            return new JuiceNetDeviceHandler(thing, timeZoneProvider);
        }

        return null;
    }
}
