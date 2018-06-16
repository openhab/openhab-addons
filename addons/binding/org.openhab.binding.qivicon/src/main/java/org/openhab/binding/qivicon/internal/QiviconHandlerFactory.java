/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.qivicon.internal;

import static org.openhab.binding.qivicon.internal.QiviconBindingConstants.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link QiviconHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Claudius Ellsel - Initial contribution
 */
@Component(configurationPid = "binding.qivicon", service = ThingHandlerFactory.class)
public class QiviconHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(QiviconHandlerFactory.class);
    private HttpClient httpClient;
    private Gson gson = new Gson();
    private ESHThing[] eshThings;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        String networkAddress = thing.getConfiguration().get("networkAddress").toString();
        String requestAddress = "http://" + networkAddress + "/rest/things/";
        String restThings;
        try {
            restThings = httpClient.GET(requestAddress).getContentAsString();
            logger.debug("Response: {}", restThings);
            eshThings = gson.fromJson(restThings, ESHThing[].class);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Problem with API communication: {}", e);
        }

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            return new QiviconHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_CONNECTED_DEVICE)) {
            ESHThing eshThing = null;
            for (int i = 0; i <= eshThings.length; i++) {
                if (thing.getUID().getAsString() == eshThings[i].getUID()) {
                    eshThing = eshThings[i];
                }
            }
            return new QiviconConnectedDeviceHandler(thing, httpClient, eshThing);
        } else {
            return null;
        }
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }
}
