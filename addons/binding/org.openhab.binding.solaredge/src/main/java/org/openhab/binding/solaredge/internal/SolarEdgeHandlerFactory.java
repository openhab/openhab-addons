/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal;

import static org.openhab.binding.solaredge.SolarEdgeBindingConstants.*;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.solaredge.handler.GenericSolarEdgeHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarEdgeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Alexander Friese - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.solaredge")
public class SolarEdgeHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SolarEdgeHandlerFactory.class);

    /**
     * the shared http client
     */
    private HttpClient httpClient;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_GENERIC)) {
            return new GenericSolarEdgeHandler(thing, httpClient);
        } else {
            logger.warn("Unsupported Thing-Type: {}", thingTypeUID.getAsString());
        }

        return null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        logger.debug("setHttpClientFactory");
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        logger.debug("unsetHttpClientFactory");
        this.httpClient = null;
    }

}
