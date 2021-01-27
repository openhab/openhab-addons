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
package org.openhab.binding.souliss.handler;

import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SoulissCommonCommands;
import org.openhab.core.thing.Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
public class SoulissGatewayJobPing extends Thread {

    private Logger logger = LoggerFactory.getLogger(SoulissGatewayJobPing.class);
    private String ipAddressOnLAN;
    private byte userIndex;
    private byte nodeIndex;
    private int pingRefreshInterval;

    private SoulissGatewayHandler gw;

    public SoulissGatewayJobPing(Bridge bridge) {
        gw = (SoulissGatewayHandler) bridge.getHandler();
        ipAddressOnLAN = gw.ipAddressOnLAN;
        userIndex = gw.userIndex;
        nodeIndex = gw.nodeIndex;
        setPingRefreshInterval(gw.pingRefreshInterval);
    }

    @Override
    public void run() {
        sendPing();
        gw.pingSent();
    }

    private void sendPing() {
        logger.debug("Sending ping packet");
        if (ipAddressOnLAN.length() > 0) {
            SoulissCommonCommands.sendPing(SoulissBindingNetworkParameters.getDatagramSocket(), ipAddressOnLAN,
                    nodeIndex, userIndex, (byte) 0, (byte) 0);
            logger.debug("Sent ping packet");
        }
    }

    public int getPingRefreshInterval() {
        return pingRefreshInterval;
    }

    public void setPingRefreshInterval(int pingRefreshInterval) {
        this.pingRefreshInterval = pingRefreshInterval;
    }
}
