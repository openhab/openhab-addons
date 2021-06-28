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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;

/**
 * This class contain parameter of Souliss Network.
 * Those are loaded at startup from SoulissBinding.updated(), from file openhab.cfg
 * and used by SoulissBinding.execute(), SoulissCommGate.send(), UDPServerThread, decodeDBStructRequest.decodeMacaco
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public final class NetworkParameters {

    public static final Byte DEFAULT_NODE_INDEX = (byte) 130; // dummy for discover - unused
    public static final Byte DEFAULT_USER_INDEX = (byte) 75; // dummy for discover - unused

    private static ConcurrentMap<Byte, Thing> hashTableGateways = new ConcurrentHashMap<>();
    private static ConcurrentMap<String, Thing> hashTableTopics = new ConcurrentHashMap<>();

    // public static void addGateway(byte lastByteGatewayIP, Thing thing) {
    // hashTableGateways.put(lastByteGatewayIP, thing);
    // }

    // public static void addTopics(String sUID, Thing thing) {
    // hashTableTopics.put(sUID, thing);
    // }

    public static ConcurrentMap<Byte, Thing> getHashTableGateways() {
        return hashTableGateways;
    }

    public static ConcurrentMap<String, Thing> getHashTableTopics() {
        return hashTableTopics;
    }

    @Nullable
    public static Thing getTopic(String sUID) {
        return hashTableTopics.get(sUID);
    }

    @Nullable
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
