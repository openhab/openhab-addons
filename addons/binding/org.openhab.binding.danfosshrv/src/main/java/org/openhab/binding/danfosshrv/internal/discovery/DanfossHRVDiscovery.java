package org.openhab.binding.danfosshrv.internal.discovery;

import static org.openhab.binding.danfosshrv.DanfossHRVBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DanfossHRVDiscovery extends AbstractDiscoveryService {

    private static final int BROADCAST_PORT = 30045;
    private static final byte[] DISCOVER_SEND = { 0x0c, 0x00, 0x30, 0x00, 0x11, 0x00, 0x12, 0x00, 0x13 };
    private static final byte[] DISCOVER_RECEIVE = { 0x0d, 0x00, 0x07, 0x00, 0x02, 0x02, 0x00 };

    private Logger logger = LoggerFactory.getLogger(DanfossHRVDiscovery.class);

    public DanfossHRVDiscovery() {
        super(SUPPORTED_THING_TYPES_UIDS, 15, true);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Danfoss Air CCM background discovery");
        scheduler.schedule(() -> discover(), 0, TimeUnit.SECONDS);
    }

    @Override
    public void startScan() {
        logger.debug("Start Danfoss Air CCM scan");
        discover();
    }

    private synchronized void discover() {
        logger.debug("Try to discover all Danfoss Air CCM devices");

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(5000);

            NetworkInterface netInf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            InetAddress broadcastAddress = null;
            for (InterfaceAddress intAddr : netInf.getInterfaceAddresses()) {
                broadcastAddress = intAddr.getBroadcast();
                if (broadcastAddress != null) {
                    break;
                }
            }

            if (broadcastAddress == null) {
                return;
            }

            // send discover
            byte[] sendBuffer = DISCOVER_SEND;
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, broadcastAddress,
                    BROADCAST_PORT);
            socket.send(sendPacket);
            logger.debug("Disover message sent");

            // wait for responses
            while (true) {
                byte[] receiveBuffer = new byte[7];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    socket.receive(receivePacket);
                } catch (SocketTimeoutException e) {
                    break; // leave the endless loop
                }

                byte[] data = receivePacket.getData();
                if (Arrays.equals(data, DISCOVER_RECEIVE)) {
                    logger.debug("Disover received correct response");

                    String host = receivePacket.getAddress().getHostName();
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("host", host);

                    logger.debug("Adding a new Danfoss Air Unit CCM '{}' to inbox", host);

                    ThingUID uid = new ThingUID(THING_TYPE_HRV, String.valueOf(receivePacket.getAddress().hashCode()));

                    DiscoveryResult result = DiscoveryResultBuilder.create(uid).withRepresentationProperty(host)
                            .withProperties(properties).withLabel("Danfoss HRV").build();
                    thingDiscovered(result);

                    logger.debug("Thing discovered '{}'", result);
                }
            }
        } catch (IOException e) {
            logger.debug("No Danfoss Air CCM device found. Diagnostic: {}", e.getMessage());
        }
    }
}
