/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.zm.internal;

import static org.openhab.binding.zm.internal.ZmBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.zm.internal.handler.ZmBridgeHandler;
import org.openhab.binding.zm.internal.handler.ZmMonitorHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ZmHandlerFactory} is responsible for creating thing handlers.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.zm", service = ThingHandlerFactory.class)
public class ZmHandlerFactory extends BaseThingHandlerFactory {

    private final TimeZoneProvider timeZoneProvider;
    private final HttpClient httpClient;
    private final ZmStateDescriptionOptionsProvider stateDescriptionProvider;

    @Activate
    public ZmHandlerFactory(@Reference TimeZoneProvider timeZoneProvider,
            @Reference HttpClientFactory httpClientFactory,
            @Reference ZmStateDescriptionOptionsProvider stateDescriptionProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.timeZoneProvider = timeZoneProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (SUPPORTED_SERVER_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new ZmBridgeHandler((Bridge) thing, httpClient, stateDescriptionProvider);
        } else if (SUPPORTED_MONITOR_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new ZmMonitorHandler(thing, timeZoneProvider);
        }
        return null;
    }
}
