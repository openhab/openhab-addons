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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gree.internal.GreeCryptoUtil;
import org.openhab.binding.gree.internal.gson.GreeScanReponsePack4GsonDTO;
import org.openhab.binding.gree.internal.gson.GreeScanRequest4GsonDTO;
import org.openhab.binding.gree.internal.gson.GreeScanResponse4GsonDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

/**
 * The GreeDeviceFinder provides functionality for searching for
 * Gree Airconditioners on the network and keeping a list of
 * found devices.
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 */
@NonNullByDefault
public class GreeDeviceFinder {
    private final Logger logger = LoggerFactory.getLogger(GreeDeviceFinder.class);

    protected final InetAddress mIPAddress;
    protected HashMap<String, GreeAirDevice> mDevicesHashMap = new HashMap<>();

    public GreeDeviceFinder() {
        mIPAddress = InetAddress.getLoopbackAddress(); // dummy
    }

    public GreeDeviceFinder(String broadcastAddress) throws UnknownHostException {
        mIPAddress = InetAddress.getByName(broadcastAddress);
        logger.debug("Broadtcast address {} converted to {}", broadcastAddress, mIPAddress.getHostAddress());
    }

    public void scan(@Nullable DatagramSocket clientSocket) throws IOException, Exception {
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];

        // Send the Scan message
        // GreeProtocolUtils protocolUtils = new GreeProtocolUtils();
        // sendData = protocolUtils.CreateScanRequest();
        GreeScanRequest4GsonDTO scanGson = new GreeScanRequest4GsonDTO();
        scanGson.t = "scan";

        GsonBuilder gsonBuilder = new GsonBuilder();
        // gsonBuilder.setLenient();
        Gson gson = gsonBuilder.create();
        String scanReq = gson.toJson(scanGson);
        sendData = scanReq.getBytes();

        logger.trace("Sending scan packet to {}", mIPAddress.getHostAddress());

        clientSocket.setSoTimeout(DISCOVERY_TIMEOUT_MS);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, mIPAddress, DISCOVERY_TIMEOUT_MS);
        clientSocket.send(sendPacket);

        // Loop for respnses from devices until we get a timeout.
        boolean scanning = true;
        int retries = MAX_SCAN_CYCLES;
        while (scanning && (retries > 0)) {
            // Receive a response
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                clientSocket.receive(receivePacket);
                InetAddress remoteAddress = receivePacket.getAddress();
                int remotePort = receivePacket.getPort();

                // Read the response
                String modifiedSentence = new String(receivePacket.getData());
                StringReader stringReader = new StringReader(modifiedSentence);
                GreeScanResponse4GsonDTO scanResponseGson = gson.fromJson(new JsonReader(stringReader),
                        GreeScanResponse4GsonDTO.class);

                // If there was no pack, ignore the response
                if (scanResponseGson.pack == null) {
                    logger.debug("Invalid packet format, ignore");
                    continue;
                }

                scanResponseGson.decryptedPack = GreeCryptoUtil.decryptPack(GreeCryptoUtil.GetAESGeneralKeyByteArray(),
                        scanResponseGson.pack);
                String decryptedMsg = GreeCryptoUtil.decryptPack(GreeCryptoUtil.GetAESGeneralKeyByteArray(),
                        scanResponseGson.pack);

                // If something was wrong with the decryption, ignore the response
                if (decryptedMsg == null) {
                    logger.debug("Decryption failed, ignore response");
                    continue;
                }
                logger.debug("Response received from address {}: {}", remoteAddress.getHostAddress(), decryptedMsg);

                // Create the JSON to hold the response values
                stringReader = new StringReader(decryptedMsg);
                scanResponseGson.packJson = gson.fromJson(new JsonReader(stringReader),
                        GreeScanReponsePack4GsonDTO.class);

                // Now make sure the device is reported as a Gree device
                if (scanResponseGson.packJson.brand.equalsIgnoreCase("gree")) {
                    // Create a new GreeDevice
                    logger.debug("Discovered device at {}:{}", remoteAddress, remotePort);
                    GreeAirDevice newDevice = new GreeAirDevice();
                    newDevice.setAddress(remoteAddress);
                    newDevice.setPort(remotePort);
                    newDevice.setScanResponseGson(scanResponseGson);
                    addDevice(newDevice);
                } else {
                    logger.debug("Unit discovered, but brand is not GREE");
                }
            } catch (SocketTimeoutException e) {
                // We've received a timeout so lets quit searching for devices
                scanning = false;
                logger.debug("Discovery timed out");
                break;
            } catch (Exception e) {
                logger.debug("Exception on device scan", e);
                retries--;
            }
        }
    }

    public void addDevice(GreeAirDevice newDevice) {
        mDevicesHashMap.put(newDevice.getId(), newDevice);
    }

    public GreeAirDevice getDevice(String id) {
        return mDevicesHashMap.get(id);
    }

    public HashMap<String, GreeAirDevice> getDevices() {
        return mDevicesHashMap;
    }

    public @Nullable GreeAirDevice getDeviceByIPAddress(String ipAddress) {
        GreeAirDevice returnDevice = null;

        Set<String> keySet = mDevicesHashMap.keySet();
        Iterator<String> iter = keySet.iterator();
        while (returnDevice == null && iter.hasNext()) {
            Object thiskey = iter.next();
            if (mDevicesHashMap.containsKey(thiskey)) {
                GreeAirDevice currDevice = mDevicesHashMap.get(thiskey);
                if (currDevice.getAddress().getHostAddress().equals(ipAddress)) {
                    returnDevice = currDevice;
                }
            }
        }

        return returnDevice;
    }

    public Integer getScannedDeviceCount() {
        return new Integer(mDevicesHashMap.size());
    }
}
