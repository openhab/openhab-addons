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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
@NonNullByDefault
public class SoulissBindingNetworkParameters {

    public static final Byte DEFAULT_NODE_INDEX = (byte) 130;
    public static final Byte DEFAULT_USER_INDEX = (byte) 70;
    public static final int PRESET_TIME = 1000;
    public static final int SEND_DELAY = PRESET_TIME;
    public static final int SEND_MIN_DELAY = PRESET_TIME;
    public static final long SECURE_SEND_TIMEOUT_TO_REQUEUE = PRESET_TIME;
    public static final long SECURE_SEND_TIMEOUT_TO_REMOVE_PACKET = PRESET_TIME;

    private static ConcurrentHashMap<Byte, Thing> hashTableGateways = new ConcurrentHashMap<Byte, Thing>();
    private static ConcurrentHashMap<String, Thing> hashTableTopics = new ConcurrentHashMap<String, Thing>();

    @Nullable
    private static DatagramSocket datagramSocket = null;
    @Nullable
    public static DiscoverResult discoverResult = null;

    @Nullable
    public static DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public static void closeDatagramSocket() {
        if (datagramSocket != null) {
            datagramSocket.close();
        }
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
