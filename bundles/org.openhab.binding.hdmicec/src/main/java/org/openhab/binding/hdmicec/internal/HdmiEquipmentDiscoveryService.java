/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.hdmicec.internal;

import static org.openhab.binding.hdmicec.internal.HdmiCecBindingConstants.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HdmiCecEquipmentHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sam Spencer - Initial contribution
 */

@NonNullByDefault
public class HdmiEquipmentDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(HdmiEquipmentDiscoveryService.class);

    private static final int SEARCH_TIME = 30;

    private HdmiCecBridgeHandler bridgeHandler;

    // @SuppressWarnings("null")
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream.of(THING_TYPE_BRIDGE, THING_TYPE_EQUIPMENT)
            .collect(Collectors.toSet());

    public HdmiEquipmentDiscoveryService(HdmiCecBridgeHandler bridgeHandler) {
        super(SUPPORTED_THING_TYPES, SEARCH_TIME, false);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public void startScan() {
        logger.trace("startScan() called");
        bridgeHandler.startDeviceDiscovery(this);
    }

    public void processDevices(ArrayList<DiscoveryResult> discoveryResults) {
        logger.trace("processDevices() with {} results", discoveryResults.size());
        for (DiscoveryResult discoveryResult : discoveryResults) {
            thingDiscovered(discoveryResult);
        }
    }
}
