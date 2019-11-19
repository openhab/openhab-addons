/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.souliss.internal.discovery;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.souliss.handler.SoulissGatewayHandler;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingUDPServerJob;
import org.openhab.binding.souliss.internal.protocol.SoulissCommonCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissDiscoverJob implements Runnable {
    /**
     * Result callback interface.
     */
    public interface DiscoverResult {
        static boolean isGatewayDetected = false;
        Bridge bridge = null;

        void gatewayDetected(InetAddress addr, String id);

        void thingDetected_Typicals(byte lastByteGatewayIP, byte typical, byte node, byte slot);

        void thingDetected_ActionMessages(String sTopicNumber, String sTopicVariant);

        ThingUID getGatewayUID();
    };

    private DatagramSocket datagramSocket;
    SoulissBindingUDPServerJob UDP_Server_OnDefaultPort = null;
    ///// Debug
    private Logger logger = LoggerFactory.getLogger(SoulissDiscoverJob.class);

    private int resendCounter = 0;

    public SoulissDiscoverJob(DatagramSocket _datagramSocket, DiscoverResult discoverResult) throws SocketException {
        datagramSocket = _datagramSocket;
    }

    @Override
    public void run() {
        logger.debug("Sending discovery packet nr.{}", resendCounter);

        try {
            // ===============================================================================
            // ===============================================================================
            SoulissCommonCommands.sendBroadcastGatewayDiscover(datagramSocket);
            // ===============================================================================
            // ===============================================================================
            logger.debug("Sent discovery packet");

        } catch (Exception e) {
            logger.error("Sending a discovery packet failed. " + e.getLocalizedMessage());
        }

        ConcurrentHashMap<Byte, Thing> gwMaps = SoulissBindingNetworkParameters.getHashTableGateways();
        Collection<Thing> gwMapsCollection = gwMaps.values();
        for (Thing t : gwMapsCollection) {
            SoulissGatewayHandler gw = (SoulissGatewayHandler) t.getHandler();
            if (gw != null) {
                logger.debug("Sending request to gateway for souliss network", resendCounter);
                SoulissCommonCommands.sendDBStructFrame(SoulissBindingNetworkParameters.getDatagramSocket(),
                        gw.getGatewayIP(), gw.nodeIndex, gw.userIndex);
            } else {
                logger.debug("Gateway null - Skipped");
            }
        }
        resendCounter++;
    }
}
