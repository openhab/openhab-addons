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
package org.openhab.binding.gardena.internal.handler;

import static org.openhab.binding.gardena.internal.GardenaBindingConstants.BINDING_ID;
import static org.openhab.binding.gardena.internal.GardenaBindingConstants.THING_TYPE_ACCOUNT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.WebSocketFactory;
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
 * The {@link GardenaHandlerFactory} is responsible for creating Gardena things and thing handlers.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.gardena")
public class GardenaHandlerFactory extends BaseThingHandlerFactory {
    private HttpClientFactory httpClientFactory;
    private WebSocketFactory webSocketFactory;
    private TimeZoneProvider timeZoneProvider;

    @Activate
    public GardenaHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference WebSocketFactory webSocketFactory, final @Reference TimeZoneProvider timeZoneProvider) {
        this.httpClientFactory = httpClientFactory;
        this.webSocketFactory = webSocketFactory;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_ACCOUNT.equals(thing.getThingTypeUID())) {
            return new GardenaAccountHandler((Bridge) thing, httpClientFactory, webSocketFactory);
        } else {
            return new GardenaThingHandler(thing, timeZoneProvider);
        }
    }
}
