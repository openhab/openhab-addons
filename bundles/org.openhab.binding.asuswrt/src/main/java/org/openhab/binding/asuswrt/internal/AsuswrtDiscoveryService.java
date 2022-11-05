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
package org.openhab.binding.asuswrt.internal;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingConstants.*;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.*;
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtClientInfo;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtClientList;
import org.openhab.binding.asuswrt.internal.things.AsuswrtRouter;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AsuswrtDiscoveryService} is responsible for discover clients
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtDiscoveryService.class);
    protected @NonNullByDefault({}) AsuswrtRouter router;

    /***********************************
     *
     * INITIALIZATION
     * 
     ************************************/
    public AsuswrtDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_S, false);
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof AsuswrtRouter) {
            AsuswrtRouter router = (AsuswrtRouter) handler;
            router.setDiscoveryService(this);
            this.router = router;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.router;
    }

    /***********************************
     *
     * SCAN HANDLING
     *
     ************************************/

    /**
     * Start scan manually
     */
    @Override
    public void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        if (router != null) {
            router.queryDeviceData(false);
            AsuswrtClientList clientList = router.getDeviceInfo().getClients();
            handleScanResults(clientList);
        }
    }

    /**
     * work with result from get clients from router
     * 
     * @param deviceList
     */
    public void handleScanResults(AsuswrtClientList clientList) {
        try {
            for (AsuswrtClientInfo client : clientList) {
                DiscoveryResult discoveryResult = createDiscoveryResult(client);
                thingDiscovered(discoveryResult);
            }
        } catch (Exception e) {
            logger.debug("error handling scan results", e);
        }
    }

    public DiscoveryResult createDiscoveryResult(AsuswrtClientInfo clientInfo) {
        String clientMac = clientInfo.getMac();
        String unformatedMac = unformatMac(clientMac);
        String clientName;
        String nickName;
        String label;

        /* create label and thing names */
        clientName = stringOrDefault(clientInfo.getName(), "client_" + unformatedMac);
        nickName = stringOrDefault(clientInfo.getNickName(), clientName);
        if (nickName.equals(clientName)) {
            label = nickName;
        } else {
            label = nickName + " (" + clientName + ")";
        }

        /* create properties */
        Map<String, Object> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_MAC_ADDRESS, clientInfo.getMac());
        properties.put(Thing.PROPERTY_VENDOR, clientInfo.getVendor());
        properties.put(PROPERTY_CLIENT_NAME, clientName);
        properties.put(CHANNEL_CLIENT_NICKNAME, nickName);

        logger.debug("client '{}'' discovered", clientMac);
        if (this.router != null) {
            ThingUID bridgeUID = router.getUID();
            ThingUID thingUID = new ThingUID(THING_TYPE_CLIENT, bridgeUID, unformatedMac);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(CLIENT_REPRASENTATION_PROPERTY).withBridge(bridgeUID).withLabel(label)
                    .build();
        } else {
            ThingUID thingUID = new ThingUID(BINDING_ID, unformatedMac);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(CLIENT_REPRASENTATION_PROPERTY).withLabel(label).build();
        }
    }
}
