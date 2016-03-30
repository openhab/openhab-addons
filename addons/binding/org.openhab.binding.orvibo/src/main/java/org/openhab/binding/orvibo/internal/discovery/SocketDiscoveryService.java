/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.orvibo.internal.discovery;

import java.net.SocketException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.orvibo.OrviboBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tavalin.s20.S20Client;
import com.github.tavalin.s20.S20Client.SocketDiscoveryListener;
import com.github.tavalin.s20.socket.Socket;

public class SocketDiscoveryService extends AbstractDiscoveryService implements SocketDiscoveryListener {

    private final Logger logger = LoggerFactory.getLogger(SocketDiscoveryService.class);
    private final static int SEARCH_TIME = 60;
    private S20Client s20Client;

    public SocketDiscoveryService() throws SocketException {
        super(getSupportedThingTypeUIDs(), SEARCH_TIME);
    }

    private static Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(OrviboBindingConstants.THING_TYPE_S20);
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        try {
            s20Client = S20Client.getInstance();
            super.activate(configProperties);
        } catch (SocketException ex) {
            logger.error("Error occured while activating S20 discovery service: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void startScan() {
        logger.debug("starting manual scan");
        s20Client.addSocketDiscoveryListener(this);
        s20Client.globalDiscovery();
        for (Socket socket : s20Client.getAllSocketsCollection().values()) {
            doThingDiscovered(socket);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("starting automatic background scan");
        s20Client.addSocketDiscoveryListener(this);
        s20Client.globalDiscovery();

    }

    @Override
    protected void stopBackgroundDiscovery() {
        s20Client.removeSocketDiscoveryListener(this);
    }

    @Override
    public void socketDiscovered(Socket socket) {
        doThingDiscovered(socket);
    }

    private DiscoveryResult createDiscoveryResult(Socket socket) {
        ThingUID thingUID = getUID(socket);
        String label = socket.getLabel();
        if (StringUtils.isBlank(label)) {
            label = "S20";
        }
        return DiscoveryResultBuilder.create(thingUID).withLabel(label)
                .withProperty(OrviboBindingConstants.CONFIG_PROPERTY_DEVICE_ID, socket.getDeviceId()).build();
    }

    private ThingUID getUID(Socket socket) {
        ThingUID thingUID = new ThingUID(OrviboBindingConstants.THING_TYPE_S20, socket.getDeviceId());
        return thingUID;
    }

    private void doThingDiscovered(Socket socket) {
        DiscoveryResult discoveryResult = createDiscoveryResult(socket);
        thingDiscovered(discoveryResult);
    }

}
