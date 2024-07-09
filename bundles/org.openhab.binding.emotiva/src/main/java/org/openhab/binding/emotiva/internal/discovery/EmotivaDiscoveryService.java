/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.emotiva.internal.discovery;

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.*;

import java.util.Objects;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emotiva.internal.EmotivaUdpBroadcastService;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for Emotiva devices.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.emotiva")
public class EmotivaDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(EmotivaDiscoveryService.class);

    @Nullable
    private final EmotivaUdpBroadcastService broadcastService = new EmotivaUdpBroadcastService(
            DISCOVERY_BROADCAST_ADDRESS);

    public EmotivaDiscoveryService() throws IllegalArgumentException, JAXBException {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SECONDS, false);
    }

    @Override
    protected void startScan() {
        logger.debug("Start scan for Emotiva devices");
        EmotivaUdpBroadcastService localBroadcastService = broadcastService;
        if (localBroadcastService != null) {
            try {
                localBroadcastService.discoverThings().ifPresent(this::thingDiscovered);
            } finally {
                removeOlderResults(getTimestampOfLastScan());
            }
        }
    }

    @Override
    protected void stopScan() {
        logger.debug("Stop scan for Emotiva devices");
        Objects.requireNonNull(broadcastService).closeDiscoverSocket();
        super.stopScan();
    }
}
