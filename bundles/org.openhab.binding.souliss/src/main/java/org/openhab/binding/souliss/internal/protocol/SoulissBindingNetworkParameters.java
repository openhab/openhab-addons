/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.souliss.internal.protocol;

import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.souliss.internal.discovery.SoulissDiscoverJob.DiscoverResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;

/**
 * This class contain parameter of Souliss Network.
 * Those are loaded at startup from SoulissBinding.updated(), from file openhab.cfg
 * and used by SoulissBinding.execute(), SoulissCommGate.send(), UDPServerThread, decodeDBStructRequest.decodeMacaco
 *
 * @author Tonino Fazio
 * @since 1.7.0
 */
public class SoulissBindingNetworkParameters {

    public static Byte defaultNodeIndex = (byte) 130;
    public static Byte defaultUserIndex = (byte) 70;
    public static int presetTime = 1000;
    public static int SEND_DELAY = presetTime;
    public static int SEND_MIN_DELAY = presetTime;
    public static long SECURE_SEND_TIMEOUT_TO_REQUEUE = presetTime;
    public static long SECURE_SEND_TIMEOUT_TO_REMOVE_PACKET = presetTime;

    private static ConcurrentHashMap<Byte, Thing> hashTableGateways = new ConcurrentHashMap<Byte, Thing>();
    private static ConcurrentHashMap<String, Thing> hashTableTopics = new ConcurrentHashMap<String, Thing>();

    private static DatagramSocket datagramSocket;
    public static DiscoverResult discoverResult;

    public static DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public static void closeDatagramSocket() {
        datagramSocket.close();
        datagramSocket = null;
    }

    public static void setDatagramSocket(DatagramSocket datagramSocket) {
        SoulissBindingNetworkParameters.datagramSocket = datagramSocket;
    }

    public static void addGateway(byte lastByteGatewayIP, Thing thing) {
        hashTableGateways.put(lastByteGatewayIP, thing);
    }

    public static void addTopics(String sUID, Thing thing) {
        hashTableTopics.put(sUID, thing);
    }

    public static ConcurrentHashMap<Byte, Thing> getHashTableGateways() {
        return hashTableGateways;
    }

    public static ConcurrentHashMap<String, Thing> getHashTableTopics() {
        return hashTableTopics;
    }

    public static Thing getTopic(String sUID) {
        return hashTableTopics.get(sUID);
    }

    public static Bridge getGateway(byte lastByteGatewayIP) {
        return (Bridge) hashTableGateways.get(lastByteGatewayIP);
    }

    public static void removeGateway(byte lastByteGatewayIP) {
        hashTableGateways.remove(lastByteGatewayIP);
    }

    public static void removeTopic(String sUID) {
        hashTableTopics.remove(sUID);
    }
}
