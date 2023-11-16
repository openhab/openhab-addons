/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtClientInfo;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtClientList;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtInterfaceList;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtIpInfo;
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
 * The {@link AsuswrtDiscoveryService} is responsible for discovering clients.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtDiscoveryService.class);
    private String uid = "";
    protected @NonNullByDefault({}) AsuswrtRouter router;

    public AsuswrtDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_S, false);
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
        super.deactivate();
        removeAllResults();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof AsuswrtRouter router) {
            router.setDiscoveryService(this);
            this.router = router;
            this.uid = router.getUID().getAsString();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.router;
    }

    /*
     * Scan handling
     */

    /**
     * Starts a manual scan.
     */
    @Override
    public void startScan() {
        logger.trace("{} starting scan", uid);
        if (router != null) {
            /* query Data */
            router.queryDeviceData(false);
            /* discover interfaces */
            AsuswrtInterfaceList ifList = router.getInterfaces();
            handleInterfaceScan(ifList);
            /* discover clients */
            AsuswrtClientList clientList = router.getClients();
            handleClientScan(clientList);
        }
    }

    @Override
    public void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    /**
     * Removes all scan results.
     */
    private void removeAllResults() {
        removeOlderResults(new Date().getTime());
    }

    /**
     * Creates {@link DiscoveryResult}s from the provided {@link AsuswrtInterfaceList}.
     */
    private void handleInterfaceScan(AsuswrtInterfaceList ifList) {
        try {
            for (AsuswrtIpInfo ifInfo : ifList) {
                DiscoveryResult discoveryResult = createInterfaceResult(ifInfo);
                thingDiscovered(discoveryResult);
            }
        } catch (Exception e) {
            logger.debug("Error while handling interface scan reults", e);
        }
    }

    /**
     * Creates {@link DiscoveryResult}s from the provided {@link AsuswrtClientList}.
     */
    public void handleClientScan(AsuswrtClientList clientList) {
        try {
            for (AsuswrtClientInfo client : clientList) {
                DiscoveryResult discoveryResult = createClientResult(client);
                thingDiscovered(discoveryResult);
            }
        } catch (Exception e) {
            logger.debug("Error while handling client scan results", e);
        }
    }

    /*
     * Discovery result creation
     */

    /**
     * Creates a {@link DiscoveryResult} from the provided {@link AsuswrtIpInfo}.
     */
    private DiscoveryResult createInterfaceResult(AsuswrtIpInfo interfaceInfo) {
        String ifName = interfaceInfo.getName();
        String macAddress = interfaceInfo.getMAC();
        String label = "AwrtInterface_" + ifName;

        Map<String, Object> properties = new HashMap<>();
        properties.put(NETWORK_REPRESENTATION_PROPERTY, ifName);
        properties.put(Thing.PROPERTY_MAC_ADDRESS, macAddress);

        logger.debug("{} thing discovered: '{}", uid, label);
        if (this.router != null) {
            ThingUID bridgeUID = router.getUID();
            ThingUID thingUID = new ThingUID(THING_TYPE_INTERFACE, bridgeUID, ifName);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(NETWORK_REPRESENTATION_PROPERTY).withBridge(bridgeUID).withLabel(label)
                    .build();
        } else {
            ThingUID thingUID = new ThingUID(BINDING_ID, ifName);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(NETWORK_REPRESENTATION_PROPERTY).withLabel(label).build();
        }
    }

    /**
     * Creates a {@link DiscoveryResult} from the provided {@link AsuswrtClientInfo}.
     */
    private DiscoveryResult createClientResult(AsuswrtClientInfo clientInfo) {
        String macAddress = clientInfo.getMac();
        String unformatedMac = unformatMac(macAddress);
        String clientName;
        String nickName;
        String label = "AwrtClient_";

        // Create label and thing names
        clientName = stringOrDefault(clientInfo.getName(), "client_" + unformatedMac);
        nickName = stringOrDefault(clientInfo.getNickName(), clientName);
        if (nickName.equals(clientName)) {
            label += nickName;
        } else {
            label += nickName + " (" + clientName + ")";
        }

        // Create properties
        Map<String, Object> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_MAC_ADDRESS, macAddress);
        properties.put(Thing.PROPERTY_VENDOR, clientInfo.getVendor());
        properties.put(PROPERTY_CLIENT_NAME, clientName);
        properties.put(CHANNEL_CLIENT_NICKNAME, nickName);

        logger.debug("{} thing discovered: '{}", uid, label);
        if (this.router != null) {
            ThingUID bridgeUID = router.getUID();
            ThingUID thingUID = new ThingUID(THING_TYPE_CLIENT, bridgeUID, unformatedMac);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(CLIENT_REPRESENTATION_PROPERTY).withTTL(DISCOVERY_AUTOREMOVE_S)
                    .withBridge(bridgeUID).withLabel(label).build();
        } else {
            ThingUID thingUID = new ThingUID(BINDING_ID, unformatedMac);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(CLIENT_REPRESENTATION_PROPERTY).withTTL(DISCOVERY_AUTOREMOVE_S)
                    .withLabel(label).build();
        }
    }
}
