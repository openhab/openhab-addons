/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal;

import static org.openhab.binding.nibeuplink.NibeUplinkBindingConstants.*;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.nibeuplink.handler.GenericHandler;
import org.openhab.binding.nibeuplink.internal.model.F1145Channels;
import org.openhab.binding.nibeuplink.internal.model.F1155Channels;
import org.openhab.binding.nibeuplink.internal.model.F730Channels;
import org.openhab.binding.nibeuplink.internal.model.F750Channels;
import org.openhab.binding.nibeuplink.internal.model.VVM310Channels;
import org.openhab.binding.nibeuplink.internal.model.VVM320Channels;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NibeUplinkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Alexander Friese - initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.nibeuplink")
public class NibeUplinkHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(NibeUplinkHandlerFactory.class);

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

        if (thingTypeUID.equals(THING_TYPE_VVM320)) {
            return new GenericHandler(thing, httpClient, VVM320Channels.getInstance());
        } else if (thingTypeUID.equals(THING_TYPE_VVM310)) {
            return new GenericHandler(thing, httpClient, VVM310Channels.getInstance());
        } else if (thingTypeUID.equals(THING_TYPE_F750)) {
            return new GenericHandler(thing, httpClient, F750Channels.getInstance());
        } else if (thingTypeUID.equals(THING_TYPE_F730)) {
            return new GenericHandler(thing, httpClient, F730Channels.getInstance());
        } else if (thingTypeUID.equals(THING_TYPE_F1145)) {
            return new GenericHandler(thing, httpClient, F1145Channels.getInstance());
        } else if (thingTypeUID.equals(THING_TYPE_F1155)) {
            return new GenericHandler(thing, httpClient, F1155Channels.getInstance());
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
