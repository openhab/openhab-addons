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
package org.openhab.binding.ipobserver.internal;

import static org.openhab.binding.ipobserver.internal.IpObserverBindingConstants.THING_WEATHER_STATION;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

/**
 * The {@link IpObserverHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.ipobserver", service = ThingHandlerFactory.class)
public class IpObserverHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_WEATHER_STATION);
    private final IpObserverUpdateReceiver ipObserverUpdateReceiver;
    protected final HttpClient httpClient;

    @Activate
    public IpObserverHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference HttpService httpService) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        ipObserverUpdateReceiver = new IpObserverUpdateReceiver(httpService);
    }

    protected HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_WEATHER_STATION.equals(thingTypeUID)) {
            return new IpObserverHandler(thing, httpClient, ipObserverUpdateReceiver);
        }

        return null;
    }
}
