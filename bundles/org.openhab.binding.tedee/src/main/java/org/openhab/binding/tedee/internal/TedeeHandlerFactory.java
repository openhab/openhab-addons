/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tedee.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tedee.internal.handler.TedeeBridgeHandler;
import org.openhab.binding.tedee.internal.handler.TedeeCloudBridgeHandler;
import org.openhab.binding.tedee.internal.handler.TedeeLockHandler;
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
 * 
 * The {@link TedeeHandlerFactory} is responsible for creating the Tedee thing
 * handlers.
 *
 * @author Alex Goll - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.tedee", service = ThingHandlerFactory.class)
public class TedeeHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClient client;

    @Activate
    public TedeeHandlerFactory(final @Reference HttpClientFactory httpClientFactory) {
        client = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return TedeeBindingConstants.SUPPORTED.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (TedeeBindingConstants.CLOUD.equals(thingTypeUID)) {
            return new TedeeCloudBridgeHandler((Bridge) thing, client);
        }

        if (TedeeBindingConstants.BRIDGE.equals(thingTypeUID)) {
            return new TedeeBridgeHandler((Bridge) thing, client);
        }

        if (TedeeBindingConstants.LOCK.equals(thingTypeUID)) {
            return new TedeeLockHandler(thing);
        }

        return null;
    }
}
