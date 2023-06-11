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
package org.openhab.binding.lcn.internal.pchkdiscovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Discovers LCN-PCK gateways, such as LCN-PCHK.
 *
 * Scan approach:
 * 1. Determines all local network interfaces
 * 2. Send a multicast message on each interface to the PCHK multicast address 234.5.6.7 (not configurable by user).
 * 3. Evaluate multicast responses of PCK gateways in the network
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.lcn")
public class LcnPchkDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(LcnPchkDiscoveryService.class);
    private static final String HOSTNAME = "hostname";
    private static final String PORT = "port";
    private static final String MAC_ADDRESS = "macAddress";
    private static final String PCHK_DISCOVERY_MULTICAST_ADDRESS = "234.5.6.7";
    private static final int PCHK_DISCOVERY_PORT = 4220;
    private static final int INTERFACE_TIMEOUT_SEC = 2;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set
            .of(LcnBindingConstants.THING_TYPE_PCK_GATEWAY);
    private static final String DISCOVER_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ServicesRequest xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"servicesrequest.xsd\"><Version major=\"1\" minor=\"0\" /><Requester requestId=\"1\" type=\"openHAB\" major=\"1\" minor=\"0\">openHAB</Requester><Requests><Request xsi:type=\"EnumServices\" major=\"1\" minor=\"0\" name=\"LcnPchkBus\" /></Requests></ServicesRequest>";

    public LcnPchkDiscoveryService() throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, 0, false);
    }

    private List<InetAddress> getLocalAddresses() {
        List<InetAddress> result = new LinkedList<>();
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                try {
                    if (networkInterface.isUp() && !networkInterface.isLoopback()
                            && !networkInterface.isPointToPoint()) {
                        result.addAll(Collections.list(networkInterface.getInetAddresses()));
                    }
                } catch (SocketException exception) {
                    // ignore
                }
            }
        } catch (SocketException exception) {
            return Collections.emptyList();
        }
        return result;
    }

    @Override
    protected void startScan() {
        try {
            InetAddress multicastAddress = InetAddress.getByName(PCHK_DISCOVERY_MULTICAST_ADDRESS);

            getLocalAddresses().forEach(localInterfaceAddress -> {
                logger.debug("Searching on {} ...", localInterfaceAddress.getHostAddress());
                try (MulticastSocket socket = new MulticastSocket(PCHK_DISCOVERY_PORT)) {
                    socket.setInterface(localInterfaceAddress);
                    socket.setReuseAddress(true);
                    socket.setSoTimeout(INTERFACE_TIMEOUT_SEC * 1000);
                    socket.joinGroup(multicastAddress);

                    byte[] requestData = DISCOVER_REQUEST.getBytes(LcnDefs.LCN_ENCODING);
                    DatagramPacket request = new DatagramPacket(requestData, requestData.length, multicastAddress,
                            PCHK_DISCOVERY_PORT);
                    socket.send(request);

                    do {
                        byte[] rxbuf = new byte[8192];
                        DatagramPacket packet = new DatagramPacket(rxbuf, rxbuf.length);
                        socket.receive(packet);

                        InetAddress addr = packet.getAddress();
                        String response = new String(packet.getData(), LcnDefs.LCN_ENCODING);

                        if (response.contains("ServicesRequest")) {
                            continue;
                        }

                        ServicesResponse deserialized = xmlToServiceResponse(response);

                        String macAddress = deserialized.getServer().getMachineId().replace(":", "");
                        ThingUID thingUid = new ThingUID(LcnBindingConstants.THING_TYPE_PCK_GATEWAY, macAddress);

                        Map<String, Object> properties = new HashMap<>(3);
                        properties.put(HOSTNAME, addr.getHostAddress());
                        properties.put(PORT, deserialized.getExtServices().getExtService().getLocalPort());
                        properties.put(MAC_ADDRESS, macAddress);

                        DiscoveryResultBuilder discoveryResult = DiscoveryResultBuilder.create(thingUid)
                                .withProperties(properties).withRepresentationProperty(MAC_ADDRESS)
                                .withLabel(deserialized.getServer().getContent() + " ("
                                        + deserialized.getServer().getMachineName() + ")");

                        thingDiscovered(discoveryResult.build());
                    } while (true); // left by SocketTimeoutException
                } catch (IOException e) {
                    logger.debug("Discovery failed for {}: {}", localInterfaceAddress, e.getMessage());
                }
            });
        } catch (UnknownHostException e) {
            logger.warn("Discovery failed: {}", e.getMessage());
        }
    }

    ServicesResponse xmlToServiceResponse(String response) {
        XStream xstream = new XStream(new StaxDriver());
        xstream.allowTypesByWildcard(new String[] { ServicesResponse.class.getPackageName() + ".**" });
        xstream.setClassLoader(getClass().getClassLoader());
        xstream.autodetectAnnotations(true);
        xstream.alias("ServicesResponse", ServicesResponse.class);
        xstream.alias("Server", Server.class);
        xstream.alias("Version", Server.class);
        xstream.alias("ExtServices", ExtServices.class);
        xstream.alias("ExtService", ExtService.class);

        return (ServicesResponse) xstream.fromXML(response);
    }
}
