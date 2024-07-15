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
package org.openhab.binding.tsmart.internal;

import static org.openhab.binding.tsmart.internal.TSmartBindingConstants.PROPERTY_HOSTNAME;
import static org.openhab.binding.tsmart.internal.TSmartBindingConstants.PROPERTY_ID;
import static org.openhab.binding.tsmart.internal.TSmartBindingConstants.THING_TYPE_T_SMART;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TSmartDiscoveryService} is used to discover T-Smart devices that are connected to the local network.
 *
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.tsmart")
public class TSmartDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(TSmartDiscoveryService.class);

    public TSmartDiscoveryService() {
        super(Set.of(THING_TYPE_T_SMART), 60, false);
    }

    /**
     * Start Discovery of T-Smart devices
     */
    @Override
    protected void startScan() {
        TSmartUDPListener.startDiscovery(this);

        try {
            logger.debug("Started discovery by sending UDP packet to broadcast address");
            new TSmartUDPUtils().sendUDPPacket(InetAddress.getByName("255.255.255.255"),
                    new byte[] { (byte) 0x01, (byte) 0x00, (byte) 0x00 });
        } catch (UnknownHostException e) {
            logger.debug("Unknown host: {}", e.getMessage());
        }
    }

    /**
     * End Discovery of T-Smart devices
     */
    @Override
    protected void stopScan() {
        super.stopScan();
        TSmartUDPListener.stopDiscovery();
    }

    /**
     * Receive the UDP packet responses for the discovery broadcast packet.
     * Parse these and route to the Inbox
     *
     * @param addr Network address of device
     * @param buffer Packet bytes
     */
    public void handleDiscoveryResponse(InetAddress addr, byte[] buffer) {
        String name = new String(Arrays.copyOfRange(buffer, 9, 20));
        String id = String.format("%02X%02X%02X%02X", buffer[8], buffer[7], buffer[6], buffer[5]);

        Map<String, Object> properties = new HashMap<>(3);
        properties.put(PROPERTY_HOSTNAME, addr.getHostAddress());
        properties.put(PROPERTY_ID, id);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_T_SMART, id))
                .withProperties(properties).withRepresentationProperty(PROPERTY_ID).withLabel(name).build();
        thingDiscovered(discoveryResult);
    }
}
