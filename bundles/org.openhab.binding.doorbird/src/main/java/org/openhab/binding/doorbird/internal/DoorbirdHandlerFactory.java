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
package org.openhab.binding.doorbird.internal;

import static org.openhab.binding.doorbird.internal.DoorbirdBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.doorbird.internal.handler.ControllerHandler;
import org.openhab.binding.doorbird.internal.handler.DoorbellHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link DoorbirdHandlerFactory} is responsible for creating Doorbird thing
 * handlers.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.doorbird", service = ThingHandlerFactory.class)
public class DoorbirdHandlerFactory extends BaseThingHandlerFactory {
    private final TimeZoneProvider timeZoneProvider;
    private final HttpClient httpClient;

    @Activate
    public DoorbirdHandlerFactory(@Reference TimeZoneProvider timeZoneProvider,
            @Reference HttpClientFactory httpClientFactory) {
        this.timeZoneProvider = timeZoneProvider;
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_D101.equals(thingTypeUID) || THING_TYPE_D210X.equals(thingTypeUID)) {
            return new DoorbellHandler(thing, timeZoneProvider, httpClient);
        } else if (THING_TYPE_A1081.equals(thingTypeUID)) {
            return new ControllerHandler(thing);
        }
        return null;
    }
}
