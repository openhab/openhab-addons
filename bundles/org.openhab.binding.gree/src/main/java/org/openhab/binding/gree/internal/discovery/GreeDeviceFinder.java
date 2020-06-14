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
package org.openhab.binding.gree.internal.discovery;

import static org.openhab.binding.gree.internal.GreeBindingConstants.*;

import java.io.IOException;
import java.io.StringReader;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

/**
 * The GreeDeviceFinder provides functionality for searching for GREE Airconditioners on the network and keeping a list
 * of found devices.
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 */
@NonNullByDefault
public class GreeDeviceFinder {
    private final Logger logger = LoggerFactory.getLogger(GreeDeviceFinder.class);
    private static final Gson gson = (new GsonBuilder()).create();

    protected final InetAddress ipAddress;
    protected Map<String, GreeAirDevice> deviceTable = new HashMap<>();

    public GreeDeviceFinder() {
        ipAddress = InetAddress.getLoopbackAddress(); // dummy
    }

    public GreeDeviceFinder(String broadcastAddress) throws GreeException {
        try {
            ipAddress = InetAddress.getByName(broadcastAddress);
        } catch (UnknownHostException e) {
            throw new GreeException("Unknown host or invalid IP address", e);
        }
    }

    public void scan(DatagramSocket clientSocket, boolean scanNetwork) throws GreeException {
        try {
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];

            // Send the Scan message
            GreeScanRequestDTO scanGson = new GreeScanRequestDTO();
            scanGson.t = GREE_CMDT_SCAN;
            String scanReq = gson.toJson(scanGson);
            sendData = scanReq.getBytes(StandardCharsets.UTF_8);
            logger.trace("Sending scan packet to {}", ipAddress.getHostAddress());
            clientSocket.setSoTimeout(DISCOVERY_TIMEOUT_MS);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, DISCOVERY_TIMEOUT_MS);
            clientSocket.send(sendPacket);

            // Loop for respnses from devices until we get a timeout.
            int retries = MAX_SCAN_CYCLES;
            while ((retries > 0)) {
                // Receive a response
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    clientSocket.receive(receivePacket);
                    InetAddress remoteAddress = receivePacket.getAddress();
                    int remotePort = receivePacket.getPort();

                    // Read the response
                    // String modifiedSentence = new String(receivePacket.getData(), StandardCharsets.UTF_8);
                    // GreeScanResponseDTO scanResponseGson = gson.fromJson(modifiedSentence,
                    // GreeScanResponseDTO.class);
                    String modifiedSentence = new String(receivePacket.getData(), StandardCharsets.UTF_8);
                    StringReader stringReader = new StringReader(modifiedSentence);
                    GreeScanResponseDTO scanResponseGson = gson.fromJson(new JsonReader(stringReader),
                            GreeScanResponseDTO.class);

                    // If there was no pack, ignore the response
                    if (scanResponseGson.pack == null) {
                        logger.debug("Invalid packet format, ignore");
                        continue;
                    }

                    // Decrypt message - a a GreeException is thrown when something went wrong
                    scanResponseGson.decryptedPack = GreeCryptoUtil
                            .decryptPack(GreeCryptoUtil.getAESGeneralKeyByteArray(), scanResponseGson.pack);
                    String decryptedMsg = GreeCryptoUtil.decryptPack(GreeCryptoUtil.getAESGeneralKeyByteArray(),
                            scanResponseGson.pack);

                    logger.debug("Response received from address {}: {}", remoteAddress.getHostAddress(), decryptedMsg);

                    // Create the JSON to hold the response values
                    scanResponseGson.packJson = gson.fromJson(decryptedMsg, GreeScanReponsePackDTO.class);

                    // Now make sure the device is reported as a Gree device
                    if (scanResponseGson.packJson.brand.equalsIgnoreCase("gree")) {
                        // Create a new GreeDevice
                        logger.debug("Discovered device at {}:{}", remoteAddress.getHostAddress(), remotePort);
                        GreeAirDevice newDevice = new GreeAirDevice();
                        newDevice.setAddress(remoteAddress);
                        newDevice.setPort(remotePort);
                        newDevice.setScanResponseGson(scanResponseGson);
                        addDevice(newDevice);
                    } else {
                        logger.debug("Unit discovered, but brand is not GREE");
                    }

                    if (!scanNetwork) {
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    break;
                } catch (IOException e) {
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

    public void addDevice(GreeAirDevice newDevice) {
        deviceTable.put(newDevice.getId(), newDevice);
    }

    public GreeAirDevice getDevice(String id) {
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
