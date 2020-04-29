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

public class GreeDeviceFinder {
    private final Logger logger = LoggerFactory.getLogger(GreeDeviceFinder.class);

    protected final InetAddress mIPAddress;
    protected HashMap<String, GreeAirDevice> mDevicesHashMap = new HashMap<>();

    public GreeDeviceFinder() {
        mIPAddress = InetAddress.getLoopbackAddress(); // dummy
    }

    public GreeDeviceFinder(String broadcastAddress) throws UnknownHostException {
        mIPAddress = InetAddress.getByName(broadcastAddress);
    }

    public void Scan(DatagramSocket clientSocket) throws IOException, Exception {
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

        logger.trace("Sending scan packet to {}", mIPAddress);

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, mIPAddress, 7000);
        clientSocket.send(sendPacket);

        // Loop for respnses from devices until we get a timeout.
        boolean timeoutRecieved = false;
        while (!timeoutRecieved) {
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
                    continue;
                }

                scanResponseGson.decryptedPack = GreeCryptoUtil.decryptPack(GreeCryptoUtil.GetAESGeneralKeyByteArray(),
                        scanResponseGson.pack);
                String decryptedMsg = GreeCryptoUtil.decryptPack(GreeCryptoUtil.GetAESGeneralKeyByteArray(),
                        scanResponseGson.pack);

                // If something was wrong with the decryption, ignore the response
                if (decryptedMsg == null) {
                    continue;
                }
                logger.trace("Response received from address {}: {}", remoteAddress, decryptedMsg);

                // Create the JSON to hold the response values
                stringReader = new StringReader(decryptedMsg);
                scanResponseGson.packJson = gson.fromJson(new JsonReader(stringReader),
                        GreeScanReponsePack4GsonDTO.class);

                // Now make sure the device is reported as a Gree device
                if (scanResponseGson.packJson.brand.equals("gree")) {
                    // Create a new GreeDevice
                    GreeAirDevice newDevice = new GreeAirDevice();
                    newDevice.setAddress(remoteAddress);
                    newDevice.setPort(remotePort);
                    newDevice.setScanResponseGson(scanResponseGson);

                    addDevice(newDevice);
                }
            } catch (SocketTimeoutException e) {
                // We've received a timeout so lets quit searching for devices
                timeoutRecieved = true;
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

    public GreeAirDevice getDeviceByIPAddress(String ipAddress) {
        GreeAirDevice returnDevice = null;

        Set<String> keySet = mDevicesHashMap.keySet();
        Iterator<String> iter = keySet.iterator();
        while (returnDevice == null && iter.hasNext()) {
            Object thiskey = iter.next();
            GreeAirDevice currDevice = mDevicesHashMap.get(thiskey);
            if (currDevice != null && currDevice.getAddress().getHostAddress().equals(ipAddress)) {
                returnDevice = currDevice;
            }
        }

        return returnDevice;
    }

    public Integer getScannedDeviceCount() {
        return new Integer(mDevicesHashMap.size());
    }
}
