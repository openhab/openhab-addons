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
package org.openhab.binding.tailwind.internal;

import static org.openhab.binding.tailwind.internal.TailwindBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tailwind.internal.handler.TailwindHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link TailwindHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.tailwind", service = ThingHandlerFactory.class)
public class TailwindHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClient httpClient;
    private final NetworkAddressService networkAddressService;

    @Activate
    public TailwindHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference NetworkAddressService networkAddressService) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.networkAddressService = networkAddressService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_IQ3.equals(thing.getThingTypeUID())) {
            return new TailwindHandler(thing, httpClient, networkAddressService);
        }
        return null;
    }
}
