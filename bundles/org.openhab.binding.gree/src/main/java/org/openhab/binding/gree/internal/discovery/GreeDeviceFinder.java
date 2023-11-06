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
package org.openhab.binding.gree.internal.discovery;

import static org.openhab.binding.gree.internal.GreeBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gree.internal.GreeCryptoUtil;
import org.openhab.binding.gree.internal.GreeException;
import org.openhab.binding.gree.internal.gson.GreeScanReponsePackDTO;
import org.openhab.binding.gree.internal.gson.GreeScanRequestDTO;
import org.openhab.binding.gree.internal.gson.GreeScanResponseDTO;
import org.openhab.binding.gree.internal.handler.GreeAirDevice;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The GreeDeviceFinder provides functionality for searching for GREE Airconditioners on the network and keeping a list
 * of found devices.
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 */
@NonNullByDefault
@Component(service = GreeDeviceFinder.class, configurationPid = "devicefinder.gree")
public class GreeDeviceFinder {
    private final Logger logger = LoggerFactory.getLogger(GreeDeviceFinder.class);
    private static final Gson GSON = (new GsonBuilder()).create();

    protected final InetAddress ipAddress = InetAddress.getLoopbackAddress();
    protected Map<String, GreeAirDevice> deviceTable = new HashMap<>();

    @Activate
    public GreeDeviceFinder() {
    }

    public void scan(DatagramSocket clientSocket, String broadcastAddress, boolean scanNetwork) throws GreeException {
        InetAddress ipAddress;
        try {
            ipAddress = InetAddress.getByName(broadcastAddress);
        } catch (UnknownHostException e) {
            throw new GreeException("Unknown host or invalid IP address", e);
        }
        try {
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];

            // Send the Scan message
            GreeScanRequestDTO scanGson = new GreeScanRequestDTO();
            scanGson.t = GREE_CMDT_SCAN;
            String scanReq = GSON.toJson(scanGson);
            sendData = scanReq.getBytes(StandardCharsets.UTF_8);
            logger.debug("Sending scan packet to {}", ipAddress.getHostAddress());
            clientSocket.setSoTimeout(DISCOVERY_TIMEOUT_MS);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, DISCOVERY_TIMEOUT_MS);
            clientSocket.send(sendPacket);

            // Loop for respnses from devices until we get a timeout.
            int retries = scanNetwork ? MAX_SCAN_CYCLES : 1;
            while ((retries > 0)) {
                // Receive a response
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    clientSocket.receive(receivePacket);
                    InetAddress remoteAddress = receivePacket.getAddress();
                    int remotePort = receivePacket.getPort();

                    // Read the response
                    GreeScanResponseDTO scanResponseGson = fromJson(receivePacket, GreeScanResponseDTO.class);

                    // If there was no pack, ignore the response
                    if (scanResponseGson.pack == null) {
                        logger.debug("Invalid packet format, ignore");
                        retries--;
                        continue;
                    }

                    // Decrypt message - a GreeException is thrown when something went wrong
                    String decryptedMsg = scanResponseGson.decryptedPack = GreeCryptoUtil
                            .decryptPack(GreeCryptoUtil.getAESGeneralKeyByteArray(), scanResponseGson.pack);
                    logger.debug("Response received from address {}: {}", remoteAddress.getHostAddress(), decryptedMsg);

                    // Create the JSON to hold the response values
                    scanResponseGson.packJson = GSON.fromJson(decryptedMsg, GreeScanReponsePackDTO.class);

                    // Now make sure the device is reported as a Gree device
                    if ("gree".equalsIgnoreCase(scanResponseGson.packJson.brand)) {
                        // Create a new GreeDevice
                        logger.debug("Discovered device at {}:{}", remoteAddress.getHostAddress(), remotePort);
                        GreeAirDevice newDevice = new GreeAirDevice(remoteAddress, remotePort, scanResponseGson);
                        addDevice(newDevice);
                    } else {
                        logger.debug("Unit discovered, but brand is not GREE");
                    }
                } catch (SocketTimeoutException e) {
                    return;
                } catch (IOException | JsonSyntaxException e) {
                    retries--;
                    if (retries == 0) {
                        throw new GreeException("Exception on device scan", e);
                    }
                }
            }
        } catch (IOException e) {
            throw new GreeException("I/O exception during device scan", e);
        }
    }

    private <T> T fromJson(DatagramPacket packet, Class<T> classOfT) {
        String json = new String(packet.getData(), StandardCharsets.UTF_8).replace("\\u0000", "").trim();
        return GSON.fromJson(json, classOfT);
    }

    public void addDevice(GreeAirDevice newDevice) {
        deviceTable.put(newDevice.getId(), newDevice);
    }

    public @Nullable GreeAirDevice getDevice(String id) {
        return deviceTable.get(id);
    }

    public Map<String, GreeAirDevice> getDevices() {
        return deviceTable;
    }

    public @Nullable GreeAirDevice getDeviceByIPAddress(String ipAddress) {
        for (GreeAirDevice currDevice : deviceTable.values()) {
            if (currDevice.getAddress().getHostAddress().equals(ipAddress)) {
                return currDevice;
            }
        }
        return null;
    }

    public int getScannedDeviceCount() {
        return deviceTable.size();
    }
}
