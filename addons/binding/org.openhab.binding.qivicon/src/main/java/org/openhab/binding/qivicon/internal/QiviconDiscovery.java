/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.qivicon.internal;

import static org.openhab.binding.qivicon.internal.QiviconBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

@Component(service = DiscoveryService.class, immediate = true)
public class QiviconDiscovery extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(QiviconDiscovery.class);
    private HttpClient httpClient;
    private Gson gson = new Gson();
    private ESHThing[] eshThings;

    public QiviconDiscovery() {
        super(SUPPORTED_THING_TYPES_UIDS, 10, false);
    }

    @Override
    protected void startScan() {
        // TODO Auto-generated method stub

        // TODO: Add way to retrieve the config parameters
        // String networkAddress = thing.getConfiguration().get("networkAddress").toString();
        String networkAddress = "dummy";
        String requestAddress = "http://" + networkAddress + "/rest/things/";
        String restThings;
        try {
            restThings = httpClient.GET(requestAddress).getContentAsString();
            logger.debug("Response: {}", restThings);
            eshThings = gson.fromJson(restThings, ESHThing[].class);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Problem with API communication: {}", e);
        }

        for (int i = 0; i <= eshThings.length; i++) {
            ESHThing eshThing = eshThings[i];
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(new ThingUID(eshThing.getUID()))
                    .withThingType(new ThingTypeUID(eshThing.getThingTypeUID())).build();
            // .withLabel(deviceName).withProperties(device.getBulbInfo())
            // .withProperty(PARAMETER_NETWORK_ADDRESS, device.getNetworkAddress())
            thingDiscovered(discoveryResult);
        }
    }
}
