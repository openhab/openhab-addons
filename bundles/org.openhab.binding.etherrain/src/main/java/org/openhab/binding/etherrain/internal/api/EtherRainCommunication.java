/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.etherrain.internal.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.etherrain.internal.EtherRainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EtherRainCommunication} handles communication with EtherRain
 * controllers so that the API is all in one place
 *
 * @author Joe Inkenbrandt - Initial contribution
 */

@NonNullByDefault
public class EtherRainCommunication {

    private static final String BROADCAST_DISCOVERY_MESSAGE = "eviro_id_request1";
    private static final int BROADCAST_DISCOVERY_PORT = 8089;

    private static final String ETHERRAIN_USERNAME = "admin";

    private static final int HTTP_TIMEOUT = 3;

    private static final int BROADCAST_TIMEOUT = 80;

    private static final String RESPONSE_STATUS_PATTERN = "^\\s*(un|ma|ac|os|cs|rz|ri|rn):\\s*([a-zA-Z0-9\\.]*)(\\s*<br>)?";
    private static final String BROADCAST_RESPONSE_DISCOVER_PATTERN = "eviro t=(\\S*) n=(\\S*) p=(\\S*) a=(\\S*)";

    private static final Pattern broadcastResponseDiscoverPattern = Pattern
            .compile(BROADCAST_RESPONSE_DISCOVER_PATTERN);
    private static final Pattern responseStatusPattern = Pattern.compile(RESPONSE_STATUS_PATTERN);

    private final Logger logger = LoggerFactory.getLogger(EtherRainCommunication.class);
    private final HttpClient httpClient;

    private final String address;
    private final int port;
    private final String password;

    public EtherRainCommunication(String address, int port, String password, HttpClient httpClient) {
        this.address = address;
        this.port = port;
        this.password = password;
        this.httpClient = httpClient;
    }

    public static List<EtherRainUdpResponse> autoDiscover() {
        return updBroadcast();
    }

    public EtherRainStatusResponse commandStatus() throws EtherRainException, IOException {
        commandLogin();

        List<String> responseList = sendGet("result.cgi?xs");

        if (responseList.isEmpty()) {
            throw new EtherRainException("Empty Response");
        }

        EtherRainStatusResponse response = new EtherRainStatusResponse();

        for (String line : responseList) {

            Matcher m = responseStatusPattern.matcher(line);

            if (m.matches()) {
                String command = m.replaceAll("$1");
                String status = m.replaceAll("$2");

                switch (command) {
                    case "un":
                        response.setUniqueName(status);
                        break;
                    case "ma":
                        response.setMacAddress(status);
                        break;
                    case "ac":
                        response.setServiceAccount(status);
                        break;
                    case "os":
                        response.setOperatingStatus(EtherRainOperatingStatus.valueOf(status.toUpperCase()));
                        break;
                    case "cs":
                        response.setLastCommandStatus(EtherRainCommandStatus.valueOf(status.toUpperCase()));
                        break;
                    case "rz":
                        response.setLastCommandResult(EtherRainCommandResult.valueOf(status.toUpperCase()));
                        break;
                    case "ri":
                        response.setLastActiveValue(Integer.parseInt(status));
                        break;
                    case "rn":
                        response.setRainSensor(Integer.parseInt(status) == 1);
                        break;
                    default:
                        logger.debug("Unknown response: {}", command);
                }
            }
        }

        return response;
    }

    public synchronized boolean commandIrrigate(int delay, int zone1, int zone2, int zone3, int zone4, int zone5,
            int zone6, int zone7, int zone8) {
        try {
            sendGet("result.cgi?xi=" + delay + ":" + zone1 + ":" + zone2 + ":" + zone3 + ":" + zone4 + ":" + zone5 + ":"
                    + zone6 + ":" + zone7 + ":" + zone8);
        } catch (IOException e) {
            logger.warn("Could not send irrigate command to etherrain: {}", e.getMessage());
            return false;
        }

        return true;
    }

    public synchronized boolean commandClear() {
        try {
            sendGet("/result.cgi?xr");
        } catch (IOException e) {
            logger.warn("Could not send clear command to etherrain: {}", e.getMessage());
            return false;
        }

        return true;
    }

    public synchronized boolean commandLogin() throws EtherRainException {
        try {
            sendGet("/ergetcfg.cgi?lu=" + ETHERRAIN_USERNAME + "&lp=" + password);
        } catch (IOException e) {
            logger.warn("Could not send clear command to etherrain: {}", e.getMessage());
            return false;
        }

        return true;
    }

    public synchronized boolean commandLogout() {
        try {
            sendGet("/ergetcfg.cgi?m=o");
        } catch (IOException e) {
            logger.warn("Could not send logout command to etherrain: {}", e.getMessage());
            return false;
        }

        return true;
    }

    private synchronized List<String> sendGet(String command) throws IOException {
        String url = "http://" + address + ":" + port + "/" + command;

        ContentResponse response;

        try {
            response = httpClient.newRequest(url).timeout(HTTP_TIMEOUT, TimeUnit.SECONDS).send();
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                logger.warn("Etherrain return status other than HTTP_OK : {}", response.getStatus());
                return Collections.emptyList();
            }
        } catch (TimeoutException | ExecutionException e) {
            logger.warn("Could not connect to Etherrain with exception: {}", e.getMessage());
            return Collections.emptyList();
        } catch (InterruptedException e) {
            logger.warn("Connect to Etherrain interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }

        return new BufferedReader(new StringReader(response.getContentAsString())).lines().collect(Collectors.toList());
    }

    private static List<EtherRainUdpResponse> updBroadcast() {
        List<EtherRainUdpResponse> rList = new LinkedList<>();

        // Find the server using UDP broadcast

        try (DatagramSocket c = new DatagramSocket()) {
            c.setSoTimeout(BROADCAST_TIMEOUT);
            c.setBroadcast(true);

            byte[] sendData = BROADCAST_DISCOVERY_MESSAGE.getBytes("UTF-8");

            // Try the 255.255.255.255 first
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                    InetAddress.getByName("255.255.255.255"), BROADCAST_DISCOVERY_PORT);
            c.send(sendPacket);

            while (true) {
                // Wait for a response
                byte[] recvBuf = new byte[15000];
                DatagramPacket receivePacket;
                try {
                    receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                    c.receive(receivePacket);
                } catch (SocketTimeoutException e) {
                    return rList;
                }

                // Check if the message is correct
                String message = new String(receivePacket.getData(), "UTF-8").trim();

                if (message.length() == 0) {
                    return rList;
                }

                String addressBC = receivePacket.getAddress().getHostAddress();

                Matcher matcher = broadcastResponseDiscoverPattern.matcher(message);
                String deviceTypeBC = matcher.replaceAll("$1");
                String unqiueNameBC = matcher.replaceAll("$2");
                int portBC = Integer.parseInt(matcher.replaceAll("$3"));

                // NOTE: Version 3.77 of API states that Additional Parameters (a=) are not used
                // on EtherRain
                rList.add(new EtherRainUdpResponse(deviceTypeBC, addressBC, portBC, unqiueNameBC, ""));
            }
        } catch (IOException ex) {
            return rList;
        }
    }
}
