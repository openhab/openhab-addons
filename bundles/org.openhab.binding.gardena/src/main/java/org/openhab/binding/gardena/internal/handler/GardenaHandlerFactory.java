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
package org.openhab.binding.gardena.internal.handler;

import static org.openhab.binding.gardena.internal.GardenaBindingConstants.BINDING_ID;
import static org.openhab.binding.gardena.internal.GardenaBindingConstants.THING_TYPE_ACCOUNT;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.eclipse.smarthome.io.net.http.WebSocketFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link GardenaHandlerFactory} is responsible for creating Gardena things and thing handlers.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.gardena")
public class GardenaHandlerFactory extends BaseThingHandlerFactory {
    private HttpClientFactory httpClientFactory;
    private WebSocketFactory webSocketFactory;

    @Activate
    public GardenaHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference WebSocketFactory webSocketFactory) {
        this.httpClientFactory = httpClientFactory;
        this.webSocketFactory = webSocketFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_ACCOUNT.equals(thing.getThingTypeUID())) {
            return new GardenaAccountHandler((Bridge) thing, httpClientFactory, webSocketFactory);
        } else {
            return new GardenaThingHandler(thing);
        }
    }
}
