/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hs110.discovery;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.hs110.HS110BindingConstants;
import org.openhab.binding.hs110.internal.HS110;
import org.openhab.binding.hs110.internal.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The {@link HS110DiscoveryService} detects new Power Plugs by sending a UDP network broadcast
 * and parsing the answer into a thing.
 *
 * @author Christian Fischer - Initial contribution
 */
public class HS110DiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(HS110DiscoveryService.class);

    ///// Network
    private byte[] discoverbuffer = Util.encryptBytes(HS110.Command.SYSINFO.value);
    private final DatagramPacket discoverPacket;
    private boolean willbeclosed = false;
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[1024];
    private DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

    private final InetAddress broadcast;

    public HS110DiscoveryService() throws UnknownHostException {

        super(HS110BindingConstants.SUPPORTED_THING_TYPES_UIDS, 2, false);
        byte[] addr = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
        broadcast = InetAddress.getByAddress(addr);
        discoverPacket = new DatagramPacket(discoverbuffer, discoverbuffer.length, broadcast, 9999);

    }

    @Override
    protected void startScan() {

        try {
            datagramSocket = new DatagramSocket(null);
            datagramSocket.setBroadcast(true);
            datagramSocket.send(discoverPacket);

            if (logger.isTraceEnabled()) {
                String packetContent = new String(discoverPacket.getData(), StandardCharsets.UTF_8);
                logger.debug("Discovery package sent: {}", packetContent);
            }

            while (true) {
                datagramSocket.receive(packet);
                logger.debug("Discovery returned package with length {}", packet.getLength());
                detectThing(packet);
                packet.setLength(buffer.length);
            }
        } catch (IOException e) {
            logger.error("Error starting discovery", e);
        }

    }

    private void detectThing(DatagramPacket packet) throws IOException {
        String data = Util
                .decrypt(new ByteArrayInputStream(Arrays.copyOfRange(packet.getData(), 0, packet.getLength())), true);

        logger.debug("Detecting HS110 by data: {}", data);

        String inetAddress = packet.getAddress().getHostAddress();
        String id = HS110.parseDeviceId(data);
        logger.debug("HS110 with id {} found on {} ", id, inetAddress);
        ThingUID thingUID = new ThingUID(HS110BindingConstants.THING_TYPE_HS110, id);
        String label = "HS110 at " + inetAddress;
        Map<String, Object> properties = new TreeMap<>();
        properties.put(HS110BindingConstants.CONFIG_IP, inetAddress);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(label)
                .withProperties(properties).build();
        thingDiscovered(discoveryResult);
    }

}
