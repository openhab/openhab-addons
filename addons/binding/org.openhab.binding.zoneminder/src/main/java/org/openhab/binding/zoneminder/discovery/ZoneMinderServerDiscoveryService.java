/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.zoneminder.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.handler.ZoneMinderServerBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ZoneMinderServerDiscoveryService.
 *
 * @author Martin S. Eskildsen
 */
public class ZoneMinderServerDiscoveryService extends AbstractDiscoveryService {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(ZoneMinderServerDiscoveryService.class);

    /**
     * Instantiates a new zone minder server discovery service.
     *
     * @throws IllegalArgumentException the illegal argument exception
     */
    public ZoneMinderServerDiscoveryService() throws IllegalArgumentException {
        super(ZoneMinderServerBridgeHandler.SUPPORTED_THING_TYPES, 10);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.config.discovery.AbstractDiscoveryService#getSupportedThingTypes()
     */
    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return ZoneMinderServerBridgeHandler.SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startBackgroundDiscovery() {
        // Background discovery disabled - doesn't give much sense
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.config.discovery.AbstractDiscoveryService#startScan()
     */
    @Override
    protected void startScan() {
        // discoverZoneMinderServer();
    }

    /**
     * Method for ZoneMinder Server Discovery.
     */
    protected synchronized void discoverZoneMinderServer() {
        logger.info("[DISCOVERY] - Starting ZoneMinder Bridge Discovery.");
        String ipAddress = "";
        SubnetUtils subnetUtils = null;
        SubnetInfo subnetInfo = null;
        long lowIP = 0;
        long highIP = 0;

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
            subnetUtils = new SubnetUtils(localHost.getHostAddress() + "/"
                    + networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength());
            subnetInfo = subnetUtils.getInfo();
            lowIP = convertIPToNumber(subnetInfo.getLowAddress());
            highIP = convertIPToNumber(subnetInfo.getHighAddress());

        } catch (IllegalArgumentException e) {
            logger.error("[DISCOVERY] -discoverZoneMinderServer(): Illegal Argument Exception - {}", e.toString());
            return;
        } catch (Exception e) {
            logger.error("[DISCOVERY] - discoverZoneMinderServer(): Error - Unable to get Subnet Information! {}",
                    e.toString());
            return;
        }

        logger.debug("   Local IP Address: {} - {}", subnetInfo.getAddress(),
                convertIPToNumber(subnetInfo.getAddress()));
        logger.debug("   Subnet:           {} - {}", subnetInfo.getNetworkAddress(),
                convertIPToNumber(subnetInfo.getNetworkAddress()));
        logger.debug("   Network Prefix:   {}", subnetInfo.getCidrSignature().split("/")[1]);
        logger.debug("   Network Mask:     {}", subnetInfo.getNetmask());
        logger.debug("   Low IP:           {}", convertNumberToIP(lowIP));
        logger.debug("   High IP:          {}", convertNumberToIP(highIP));

        for (long ip = lowIP; ip <= highIP; ip++) {
            try (Socket socket = new Socket()) {
                ipAddress = convertNumberToIP(ip);

                logger.debug("[DISCOVERY] - Discoververing ZoneMinder Server at IPAddress '{}'", ipAddress);
                discoverZoneMinderServerHttp(ipAddress);

            } catch (IllegalArgumentException e) {
                logger.error("[DISCOVERY] - discoverZoneMinderServer(): Illegal Argument Exception - {}", e.toString());

            } catch (IOException e) {
                logger.error("[DISCOVERY] - discoverZoneMinderServer(): IO Exception! [{}] - {}", ipAddress,
                        e.toString());
            }
        }
    }

    /**
     * Convert an IP address to a number.
     *
     * @param ipAddress the ip address
     * @return the long
     */
    private long convertIPToNumber(String ipAddress) {

        String octets[] = ipAddress.split("\\.");

        if (octets.length != 4) {
            throw new IllegalArgumentException("Invalid IP address: " + ipAddress);
        }

        long ip = 0;

        for (int i = 3; i >= 0; i--) {
            long octet = Long.parseLong(octets[3 - i]);

            if (octet != (octet & 0xff)) {
                throw new IllegalArgumentException("Invalid IP address: " + ipAddress);
            }

            ip |= octet << (i * 8);
        }

        return ip;
    }

    /**
     * Convert a number to an IP address.
     *
     * @param ip the ip
     * @return the string
     */
    private String convertNumberToIP(long ip) {
        StringBuilder ipAddress = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {

            ipAddress.insert(0, Long.toString(ip & 0xff));

            if (i < 3) {
                ipAddress.insert(0, '.');
            }

            ip = ip >> 8;
        }

        return ipAddress.toString();
    }

    /**
     * Looks for devices that respond back with the proper title tags.
     *
     * @param ipAddress the ip address
     */
    private void discoverZoneMinderServerHttp(String ipAddress) {
        String response = "";

        try {
            response = getHttpDocumentAsString("http://" + ipAddress + "/zm", 1000);
        } catch (Exception e) {

        }

        if (response == null) {
            return;
        }

        if (response.contains("ZoneMinder login") || response.contains(">ZoneMinder</a> Console -")) {
            logger.info("[DISCOVERY] - Discovered ZoneMinder Server at '{}'", ipAddress);

            ThingUID uid = new ThingUID(ZoneMinderConstants.THING_TYPE_BRIDGE_ZONEMINDER_SERVER,
                    ZoneMinderConstants.BRIDGE_ZONEMINDER_SERVER);
            Map<String, Object> properties = new HashMap<>(0);
            properties.put(ZoneMinderConstants.PARAM_HOSTNAME, ipAddress);
            properties.put(ZoneMinderConstants.PARAM_PORT, new Integer(80));
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    // .withLabel(BuildMonitorLabel(monitor.getId(), monitor.getName()))
                    .withLabel(ZoneMinderConstants.ZONEMINDER_SERVER_NAME).build();
            thingDiscovered(result);
        }

    }

    /**
     * Performs a get request.
     *
     * @param url to get
     * @param timeout the timeout
     * @return the string response or null
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected String getHttpDocumentAsString(String url, int timeout) throws IOException {
        StringBuffer response = new StringBuffer();

        URL _url = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) _url.openConnection();

        // Set Connection timeout
        conn.setConnectTimeout(timeout);

        // default is GET
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            conn.disconnect();
        } else {
            String message = "";
            switch (responseCode) {
                case 404:
                    break;
                default:
                    message = String.format(
                            "An error occured while communicating with ZoneMinder Server: URL='%s', ResponseCode='%d', ResponseMessage='%s'",
                            _url.toString(), responseCode, conn.getResponseMessage());
            }
            logger.error(message);
        }

        return response.toString();

    }

}
