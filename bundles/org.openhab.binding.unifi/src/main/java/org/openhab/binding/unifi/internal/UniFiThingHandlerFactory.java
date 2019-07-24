/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.eclipse.smarthome.io.net.http.HttpClientInitializationException;
import org.openhab.binding.unifi.internal.handler.UniFiClientThingHandler;
import org.openhab.binding.unifi.internal.handler.UniFiControllerThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * The {@link UniFiThingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Matthew Bowman - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.unifi")
public class UniFiThingHandlerFactory extends BaseThingHandlerFactory {

    private HttpClient httpClient;

    public UniFiThingHandlerFactory() {
        // [wip] mgb: temporary work around until ssl issues are sorted
        httpClient = new HttpClient(new SslContextFactory(true));
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new HttpClientInitializationException("Could not start HttpClient", e);
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return UniFiControllerThingHandler.supportsThingType(thingTypeUID)
                || UniFiClientThingHandler.supportsThingType(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (UniFiControllerThingHandler.supportsThingType(thingTypeUID)) {
            return new UniFiControllerThingHandler((Bridge) thing, httpClient);
        } else if (UniFiClientThingHandler.supportsThingType(thingTypeUID)) {
            return new UniFiClientThingHandler(thing);
        }
        return null;
    }

    // @Reference // [wip] mgb: disabled due to missing common name attributes with certs
    public void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    public void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        // nop
    }

}
