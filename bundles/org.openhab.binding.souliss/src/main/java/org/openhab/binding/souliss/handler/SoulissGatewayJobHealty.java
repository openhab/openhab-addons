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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SoulissCommonCommands;
import org.openhab.core.thing.Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */

@NonNullByDefault
public class SoulissGatewayJobHealty extends Thread {

    private Logger logger = LoggerFactory.getLogger(SoulissGatewayJobHealty.class);
    private String ipAddressOnLAN;
    private byte userIndex;
    private byte nodeIndex;
    private int healthRefreshInterval;

    private final SoulissCommonCommands soulissCommands = new SoulissCommonCommands();

    @Nullable
    private SoulissGatewayHandler gw;

    public SoulissGatewayJobHealty(Bridge bridge) {
        gw = (SoulissGatewayHandler) bridge.getHandler();
        ipAddressOnLAN = gw.ipAddressOnLAN;
        userIndex = gw.userIndex;
        nodeIndex = gw.nodeIndex;
        sethealthRefreshInterval(gw.healthRefreshInterval);
    }

    @Override
    public void run() {
        sendHealthyRequest();
    }

    private void sendHealthyRequest() {
        logger.debug("Sending healthy packet");
        if (ipAddressOnLAN.length() > 0) {
            soulissCommands.sendHealthyRequestFrame(SoulissBindingNetworkParameters.getDatagramSocket(), ipAddressOnLAN,
                    nodeIndex, userIndex, gw.getNodes());
            logger.debug("Sent healthy packet");
        }
    }

    public int gethealthRefreshInterval() {
        return healthRefreshInterval;
    }

    public void sethealthRefreshInterval(int healthRefreshInterval) {
        this.healthRefreshInterval = healthRefreshInterval;
    }
}
