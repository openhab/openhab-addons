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
package org.openhab.binding.roku.internal.discovery;

import static org.openhab.binding.roku.internal.RokuBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.roku.internal.RokuHttpException;
import org.openhab.binding.roku.internal.communication.RokuCommunicator;
import org.openhab.binding.roku.internal.dto.DeviceInfo;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RokuDiscoveryService} is responsible for discovery of Roku devices on the local network
 *
 * @author William Welliver - Initial contribution
 * @author Dan Cunningham - Refactoring and Improvements
 * @author Michael Lobstein - Modified for Roku binding
 */

@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.roku")
public class RokuDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(RokuDiscoveryService.class);
    private static final String ROKU_DISCOVERY_MESSAGE = "M-SEARCH * HTTP/1.1\r\n" + "Host: 239.255.255.250:1900\r\n"
            + "Man: \"ssdp:discover\"\r\n" + "ST: roku:ecp\r\n" + "\r\n";

    private static final Pattern USN_PATTERN = Pattern.compile("^(uuid:roku:)?ecp:([0-9a-zA-Z]{1,16})");

    private static final Pattern IP_HOST_PATTERN = Pattern
            .compile("([0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}):([0-9]{1,5})");

    private static final String ROKU_SSDP_MATCH = "uuid:roku:ecp";
    private static final int BACKGROUND_SCAN_INTERVAL_SECONDS = 300;

    private final HttpClient httpClient;

    private @Nullable ScheduledFuture<?> scheduledFuture;

    @Activate
    public RokuDiscoveryService(final @Reference HttpClientFactory httpClientFactory) {
        super(SUPPORTED_THING_TYPES_UIDS, 30, true);
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public void startBackgroundDiscovery() {
        stopBackgroundDiscovery();
        scheduledFuture = scheduler.scheduleWithFixedDelay(this::doNetworkScan, 0, BACKGROUND_SCAN_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    @Override
    public void stopBackgroundDiscovery() {
        ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        this.scheduledFuture = null;
    }

    @Override
    public void startScan() {
        doNetworkScan();
    }

    /**
     * Enumerate all network interfaces, send the discovery broadcast and process responses.
     *
     */
    private synchronized void doNetworkScan() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface ni = nets.nextElement();
                try (DatagramSocket socket = sendDiscoveryBroacast(ni)) {
                    if (socket != null) {
                        scanResposesForKeywords(socket);
                    }
                }
            }
        } catch (IOException e) {
            logger.debug("Error discovering devices", e);
        }
    }

    /**
     * Broadcasts a SSDP discovery message into the network to find provided services.
     *
     * @return The Socket where answers to the discovery broadcast arrive
     */
    private @Nullable DatagramSocket sendDiscoveryBroacast(NetworkInterface ni) {
        try {
            InetAddress m = InetAddress.getByName("239.255.255.250");
            final int port = 1900;

            if (!ni.isUp() || !ni.supportsMulticast()) {
                return null;
            }

            Enumeration<InetAddress> addrs = ni.getInetAddresses();
            InetAddress a = null;
            while (addrs.hasMoreElements()) {
                a = addrs.nextElement();
                if (a instanceof Inet4Address) {
                    break;
                } else {
                    a = null;
                }
            }
            if (a == null) {
                logger.debug("No ipv4 address on {}", ni.getName());
                return null;
            }

            // Create the discovery message packet
            byte[] requestMessage = ROKU_DISCOVERY_MESSAGE.getBytes(StandardCharsets.UTF_8);
            DatagramPacket datagramPacket = new DatagramPacket(requestMessage, requestMessage.length, m, port);

            // Create socket and send the discovery message
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(3000);
            socket.send(datagramPacket);
            return socket;
        } catch (IOException e) {
            logger.debug("sendDiscoveryBroacast() got IOException: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Scans all messages that arrive on the socket and process those that come from a Roku.
     *
     * @param socket The socket where answers to the discovery broadcast arrive
     */
    private void scanResposesForKeywords(DatagramSocket socket) {
        byte[] receiveData = new byte[1024];
        do {
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            try {
                socket.receive(packet);
            } catch (SocketTimeoutException e) {
                return;
            } catch (IOException e) {
                logger.debug("Got exception while trying to receive UPnP packets: {}", e.getMessage());
                return;
            }
            String response = new String(packet.getData(), StandardCharsets.UTF_8);
            if (response.contains(ROKU_SSDP_MATCH)) {
                parseResponseCreateThing(response);
            }
        } while (true);
    }

    /**
     * Process the response from the Roku into a DiscoveryResult.
     *
     */
    private void parseResponseCreateThing(String response) {
        DiscoveryResult result;

        String label = "Roku";
        String uuid = null;
        String host = null;
        int port = -1;

        try (Scanner scanner = new Scanner(response)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] pair = line.split(":", 2);
                if (pair.length != 2) {
                    continue;
                }

                String key = pair[0].toLowerCase();
                String value = pair[1].trim();
                logger.debug("key: {} value: {}.", key, value);
                switch (key) {
                    case "location":
                        host = value;
                        Matcher matchIp = IP_HOST_PATTERN.matcher(value);
                        if (matchIp.find()) {
                            host = matchIp.group(1);
                            port = Integer.parseInt(matchIp.group(2));
                        }
                        break;
                    case "usn":
                        Matcher matchUid = USN_PATTERN.matcher(value);
                        if (matchUid.find()) {
                            uuid = matchUid.group(2);
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        if (host == null || port == -1 || uuid == null) {
            logger.debug("Bad Format from Roku, received data was: {}", response);
            return;
        } else {
            logger.debug("Found Roku, uuid: {} host: {}", uuid, host);
        }

        uuid = uuid.replace(":", "").toLowerCase();

        ThingUID thingUid = new ThingUID(THING_TYPE_ROKU_PLAYER, uuid);

        // Try to query the device using discovered host and port to get extended device info
        try {
            RokuCommunicator communicator = new RokuCommunicator(httpClient, host, port);
            DeviceInfo device = communicator.getDeviceInfo();
            label = device.getModelName() + " " + device.getModelNumber();
            if (device.isTv()) {
                thingUid = new ThingUID(THING_TYPE_ROKU_TV, uuid);
            }
        } catch (RokuHttpException e) {
            logger.debug("Unable to retrieve Roku device-info. Exception: {}", e.getMessage(), e);
        }

        result = DiscoveryResultBuilder.create(thingUid).withLabel(label).withRepresentationProperty(PROPERTY_UUID)
                .withProperty(PROPERTY_UUID, uuid).withProperty(PROPERTY_HOST_NAME, host)
                .withProperty(PROPERTY_PORT, port).build();
        this.thingDiscovered(result);
    }
}
