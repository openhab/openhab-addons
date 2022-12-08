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
package org.openhab.binding.philipsair.internal;

import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link PhilipsAirHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Michal Boronski - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.philipsair", service = ThingHandlerFactory.class)
public class PhilipsAirHandlerFactory extends BaseThingHandlerFactory {
    private @NonNullByDefault({}) HttpClientFactory httpClientFactory;

    public HttpClientFactory getHttpClientFactory() {
        return httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (SUPPORTED_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new PhilipsAirHandler(thing, httpClientFactory.getCommonHttpClient());
        }
        return null;
    }

    @Reference
    void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClientFactory = null;
    }
}
