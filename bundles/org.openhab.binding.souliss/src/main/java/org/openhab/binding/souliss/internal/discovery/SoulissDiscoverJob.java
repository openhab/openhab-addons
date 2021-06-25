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
package org.openhab.binding.souliss.internal.discovery;

import java.net.InetAddress;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.handler.SoulissGatewayHandler;
import org.openhab.binding.souliss.internal.protocol.CommonCommands;
import org.openhab.binding.souliss.internal.protocol.NetworkParameters;
import org.openhab.binding.souliss.internal.protocol.UDPListenDiscoverRunnable;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public class SoulissDiscoverJob implements Runnable {
    private CommonCommands soulissCommands = new CommonCommands();

    /**
     * Result callback interface.
     */
    public interface DiscoverResult {
        static boolean IS_GATEWAY_DETECTED = false;

        void gatewayDetected(InetAddress addr, String id);

        void thingDetectedTypicals(byte lastByteGatewayIP, byte typical, byte node, byte slot);

        void thingDetectedActionMessages(String sTopicNumber, String sTopicVariant);
    }

    @Nullable
    UDPListenDiscoverRunnable udpServerOnDefaultPort = null;
    ///// Debug
    private final Logger logger = LoggerFactory.getLogger(SoulissDiscoverJob.class);

    private int resendCounter = 0;

    public SoulissDiscoverJob() {
    }

    @Override
    public void run() {
        ConcurrentMap<Byte, Thing> gwMaps = NetworkParameters.getHashTableGateways();
        Collection<Thing> gwMapsCollection = gwMaps.values();
        for (Thing t : gwMapsCollection) {
            SoulissGatewayHandler gw = (SoulissGatewayHandler) t.getHandler();
            if (gw != null) {
                logger.debug("Sending request to gateway for souliss network - Counter={}", resendCounter);
                soulissCommands.sendDBStructFrame(gw.gwConfig.gatewayIpAddress, (byte) gw.gwConfig.nodeIndex,
                        (byte) gw.gwConfig.userIndex);
            } else {
                logger.debug("Gateway null - Skipped");
            }
        }
        resendCounter++;
    }
}
