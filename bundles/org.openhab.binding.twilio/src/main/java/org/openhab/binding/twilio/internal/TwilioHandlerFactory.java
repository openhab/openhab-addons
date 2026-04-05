/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.twilio.internal;

import static org.openhab.binding.twilio.internal.TwilioBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.twilio.internal.handler.TwilioAccountHandler;
import org.openhab.binding.twilio.internal.handler.TwilioPhoneHandler;
import org.openhab.binding.twilio.internal.servlet.TwilioCallbackServlet;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.items.ItemRegistry;
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
 * The {@link TwilioHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.twilio", service = ThingHandlerFactory.class)
public class TwilioHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_PHONE);

    private final HttpClient httpClient;
    private final TwilioCallbackServlet callbackServlet;
    private final ItemRegistry itemRegistry;

    @Activate
    public TwilioHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference TwilioCallbackServlet callbackServlet, final @Reference ItemRegistry itemRegistry) {
        this.callbackServlet = callbackServlet;
        this.itemRegistry = itemRegistry;
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            return new TwilioAccountHandler((Bridge) thing, httpClient);
        } else if (THING_TYPE_PHONE.equals(thingTypeUID)) {
            return new TwilioPhoneHandler(thing, callbackServlet, itemRegistry);
        }

        return null;
    }
}
