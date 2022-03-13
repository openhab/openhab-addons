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
package org.openhab.binding.goecharger.internal;

import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.goecharger.internal.handler.GoEChargerHandler;
import org.openhab.binding.goecharger.internal.handler.GoEChargerV2Handler;
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
 * The {@link GoEChargerHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Samuel Brucksch - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.goecharger", service = ThingHandlerFactory.class)
public class GoEChargerHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_GOE);
    private final HttpClient httpClient;

    @Activate
    public GoEChargerHandlerFactory(final @Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        var apiVersion = thing.getConfiguration().as(GoEChargerConfiguration.class).apiVersion;

        if (THING_TYPE_GOE.equals(thingTypeUID)) {
            if (apiVersion == 1) {
                return new GoEChargerHandler(thing, httpClient);
            }
            if (apiVersion == 2) {
                return new GoEChargerV2Handler(thing, httpClient);
            }
        }

        return null;
    }
}
