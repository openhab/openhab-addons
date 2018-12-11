/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.http.internal.handler.HttpThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static org.openhab.binding.http.internal.HttpBindingConstants.THING_TYPE_HTTP;

/**
 * Handler factory for creating handlers for things of type http.
 *
 * @author Brian J. Tarricone - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.http")
public class HttpHandlerFactory extends BaseThingHandlerFactory {

    private @NonNullByDefault({}) HttpClient httpClient;

    @Override
    protected ThingHandler createHandler(final Thing thing) {
        if (supportsThingType(thing.getThingTypeUID())) {
            return new HttpThingHandler(thing, this.httpClient);
        } else {
            return null;
        }
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return THING_TYPE_HTTP.equals(thingTypeUID);
    }

    @Reference
    @SuppressWarnings("unused")
    protected void setHttpClientFactory(final HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @SuppressWarnings("unused")
    protected void unsetHttpClientFactory(final HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }
}
