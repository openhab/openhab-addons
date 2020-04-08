/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.radiothermostat.internal.discovery;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.radiothermostat.internal.RadioThermostatBindingConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link RadioThermostatDiscoveryService} is responsible for discovery of
 * RadioThermostats on the local network
 * 
 * @author William Welliver - Initial contribution
 * @author Dan Cunningham - Refactoring and Improvements
 * @author Bill Forsyth - Modified for the RadioThermostat's peculiar discovery mode
 * 
 */

@Component(service = DiscoveryService.class, configurationPid = "discovery.radiothermostat")
public class RadioThermostatDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(RadioThermostatDiscoveryService.class);
    private static final String RADIOTHERMOSTAT_DISCOVERY_MESSAGE = "TYPE: WM-DISCOVER\r\nVERSION: 1.0\r\n\r\nservices:com.marvell.wm.system*\r\n\r\n";

    private static final String SSDP_MATCH = "WM-NOTIFY";
    private static final int BACKGROUND_SCAN_INTERVAL_SECONDS = 300;

    private ScheduledFuture<?> scheduledFuture = null;

    public RadioThermostatDiscoveryService() {
        super(RadioThermostatBindingConstants.SUPPORTED_THING_TYPES_UIDS, 30, true);
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting Background Scan");
        stopBackgroundDiscovery();
        scheduledFuture = scheduler.scheduleAtFixedRate(this::doRunRun, 0, BACKGROUND_SCAN_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Interactive Scan");
        doRunRun();
    }

    protected synchronized void doRunRun() {
        logger.trace("Sending SSDP discover.");
        for (int i = 0; i < 1; i++) {
            try {
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                while (nets.hasMoreElements()) {
                    NetworkInterface ni = nets.nextElement();
                    if (ni.isUp() && ni.supportsMulticast() && !ni.getName().matches("lo")) {
                        sendDiscoveryBroacast(ni);
                    }
                }
            } catch (IOException e) {
                logger.debug("Error discoverying devices", e);
            }
        }
    }

    /**
     * Broadcasts a SSDP discovery message into the network to find provided
     * services.
     *
     * @return The Socket the answers will arrive at.
     * @throws UnknownHostException
     * @throws IOException
     * @throws SocketException
     * @throws UnsupportedEncodingException
     */
    private void sendDiscoveryBroacast(NetworkInterface ni)
            throws UnknownHostException, SocketException, UnsupportedEncodingException {
        InetAddress m = InetAddress.getByName("239.255.255.250");
        final int port = 1900;
        logger.trace("Sending discovery broadcast");
        // logger.trace("Considering {}", ni.getName());
        try {
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
                logger.trace("no ipv4 address on {}", ni.getName());
                return;
            }

            // for whatever reason, the radio thermostat responses will not be seen
            // if we bind this socket to a particular address.
            // this seems to be okay on linux systems, but osx apparently prefers ipv6, so this
            // prevents responses from being received unless the ipv4 stack is given preference.
            MulticastSocket socket = new MulticastSocket(null);
            socket.setSoTimeout(5000);
            socket.setReuseAddress(true);
            // socket.setBroadcast(true);
            socket.setNetworkInterface(ni);
            socket.joinGroup(m);
            logger.trace("Joined UPnP Multicast group on Interface: {}", ni.getName());
            byte[] requestMessage = RADIOTHERMOSTAT_DISCOVERY_MESSAGE.getBytes("UTF-8");
            DatagramPacket datagramPacket = new DatagramPacket(requestMessage, requestMessage.length, m, port);
            socket.send(datagramPacket);
            try {
                // Try to ensure that joinGroup has taken effect. Without this delay, the query
                // packet ends up going out before the group join.
                Thread.sleep(1000);

                socket.send(datagramPacket);

                byte[] buf = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                try {
                    while (!Thread.interrupted()) {
                        socket.receive(packet);
                        String response = new String(packet.getData());
                        logger.trace("Response: {} ", response);
                        if (response.contains(SSDP_MATCH)) {
                            logger.trace("Match: {} ", response);
                            parseResponse(response);
                        }
                    }
                    logger.info("Bridge device scan interrupted");
                } catch (SocketTimeoutException e) {
                    logger.trace(
                            "Timed out waiting for multicast response. Presumably all devices have already responded.");
                }
            } finally {
                socket.leaveGroup(m);
                socket.close();
            }
        } catch (IOException | InterruptedException e) {
            logger.trace("got exception: {}", e.getMessage());
        }
        return;
    }

    /**
     * Scans all messages that arrive on the socket and scans them for the
     * search keywords. The search is not case sensitive.
     *
     * @param socket
     *            The socket where the answers arrive.
     * @param keywords
     *            The keywords to be searched for.
     * @return
     * @throws IOException
     */

    protected void parseResponse(String response) {
        DiscoveryResult result;

        String name = "unknownName";
        String uuid = "unknownThermostat";
        String ip = null;
        String url = null;

        Scanner scanner = new Scanner(response);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] pair = line.split(":", 2);
            if (pair.length != 2) {
                continue;
            }
            String key = pair[0].toLowerCase();
            String value = pair[1].trim();
            logger.trace("key: {} value: {}.", key, value);
            switch (key) {
                case "location":
                    try {
                        url = value;
                        ip = new URL(value).getHost();
                    } catch (MalformedURLException e) {
                        logger.debug("Malfored URL {}", e.getMessage());
                    }
                    break;
                default:
                    break;
            }
        }
        scanner.close();

        logger.trace("Found thermostat, ip: {} ", ip);

        if (ip == null) {
            logger.trace("Bad Format from thermostat");
            return;
        }

        JsonObject content;
        String sysinfo;

        try {
            sysinfo = HttpUtil.executeUrl("GET", url, 10000);
            content = new JsonParser().parse(sysinfo).getAsJsonObject();
            uuid = content.get("uuid").getAsString();
        } catch (Exception e) {
            logger.debug("Cannot get system info from thermostat {} {}", ip, e.getMessage());
            sysinfo = null;
        }

        try {
            String nameinfo = HttpUtil.executeUrl("GET", url + "name", 10000);
            content = new JsonParser().parse(nameinfo).getAsJsonObject();
            name = content.get("name").getAsString();
        } catch (Exception e) {
            logger.debug("Cannot get name from thermostat {} {}", ip, e.getMessage());
        }

        logger.trace("Discovery returned: {} uuid {} name {}", sysinfo, uuid, name);

        ThingUID thingUid = new ThingUID(RadioThermostatBindingConstants.THING_TYPE_RTHERM, uuid);

        logger.trace("Got discovered device.");

        String label = String.format("RadioThermostat (%s)", name);
        result = DiscoveryResultBuilder.create(thingUid).withLabel(label).withRepresentationProperty(uuid)
                // .withProperty(RadioThermostatBindingConstants.PROPERTY_UUID, uuid)
                .withProperty(RadioThermostatBindingConstants.PROPERTY_IP, ip).build();
        logger.trace("New RadioThermostat discovered with ID=<{}>", uuid);
        this.thingDiscovered(result);
    }

}
