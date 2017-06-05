/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.discovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.knx.KNXBindingConstants;
import org.openhab.binding.knx.KNXBridgeListener;
import org.openhab.binding.knx.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.handler.physical.GroupAddressThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;

/**
 * The {@link IndividualAddressDiscoveryService} class provides a discovery
 * mechanism for KNX Individual Addresses
 *
 * @author Karel Goderis - Initial contribution
 */
public class IndividualAddressDiscoveryService extends AbstractDiscoveryService
        implements KNXBridgeListener, KNXBusListener {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets
            .newHashSet(KNXBindingConstants.THING_TYPE_GENERIC);

    private final static int SEARCH_TIME = 600;
    private boolean searchOngoing = false;

    private KNXBridgeBaseThingHandler bridgeHandler;
    protected Set<AreaLine> seenNetworks = new CopyOnWriteArraySet<>();

    protected Logger logger = LoggerFactory.getLogger(KNXBridgeBaseThingHandler.class);

    public IndividualAddressDiscoveryService(KNXBridgeBaseThingHandler bridgeHandler) throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME, false);
        this.bridgeHandler = bridgeHandler;
        bridgeHandler.registerKNXBusListener(this);
    }

    public class AreaLine {
        public int area;
        public int line;

        public AreaLine(int area, int line) {
            this.area = area;
            this.line = line;
        }

        @Override
        public boolean equals(Object o) {
            return (area == ((AreaLine) o).area && line == ((AreaLine) o).line);
        }
    }

    @Override
    protected void startScan() {
        searchOngoing = true;

        Set<AreaLine> scannedNetworks = new HashSet<>();

        for (int area = 0; area < 16; area++) {
            for (int line = 0; line < 16; line++) {
                if (searchOngoing) {

                    for (AreaLine al : seenNetworks) {
                        if (!scannedNetworks.contains(al)) {
                            logger.debug("Scanning the already seen network {}.{} for KNX actors", al.area, al.line);
                            IndividualAddress[] addresses = bridgeHandler.scanNetworkDevices(al.area, al.line);
                            processResults(addresses);
                            scannedNetworks.add(al);
                        }
                    }

                    logger.debug("Scanning {}.{} for KNX actors", area, line);
                    IndividualAddress[] addresses = bridgeHandler.scanNetworkDevices(area, line);
                    processResults(addresses);
                }
            }
        }
    }

    private void processResults(IndividualAddress[] newAddresses) {

        for (int i = 0; i < newAddresses.length; i++) {

            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            ThingUID thingUID = new ThingUID(KNXBindingConstants.THING_TYPE_GENERIC,
                    newAddresses[i].toString().replace(".", "_"), bridgeUID.getId());

            Map<String, Object> properties = new HashMap<>(1);
            properties.put(GroupAddressThingHandler.ADDRESS, newAddresses[i].toString());
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeUID).withLabel("Individual Address " + newAddresses[i].toString()).build();

            thingDiscovered(discoveryResult);
        }
    }

    @Override
    protected void stopScan() {
        searchOngoing = false;
    }

    public void activate() {
        bridgeHandler.registerKNXBridgeListener(this);
    }

    @Override
    public void deactivate() {
        bridgeHandler.unregisterKNXBridgeListener(this);
    }

    @Override
    public void onBridgeDisconnected(KNXBridgeBaseThingHandler bridge) {
        stopScan();
    }

    @Override
    public void onBridgeConnected(KNXBridgeBaseThingHandler bridge) {
        // When a bridge connects, it is up the user to trigger a search
    }

    @Override
    public void onActivity(IndividualAddress source, GroupAddress destination, byte[] asdu) {

        if (source != null) {
            seenNetworks.add(new AreaLine(source.getArea(), source.getLine()));
        }
    }

}
