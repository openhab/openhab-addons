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
package org.openhab.binding.souliss.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SoulissCommonCommands;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.BridgeHandler;

/**
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */

@NonNullByDefault
public class SoulissGatewayJobPing implements Runnable {

    private String ipAddressOnLAN = "";
    private byte userIndex;
    private byte nodeIndex;
    private int pingRefreshInterval;

    private final SoulissCommonCommands soulissCommands = new SoulissCommonCommands();

    @Nullable
    private SoulissGatewayHandler gwHandler;

    @SuppressWarnings("null")
    public SoulissGatewayJobPing(Bridge bridge) {
        BridgeHandler bridgeHandler = bridge.getHandler();
        if (bridgeHandler != null) {
            gwHandler = (SoulissGatewayHandler) bridgeHandler;
            this.ipAddressOnLAN = gwHandler.ipAddressOnLAN;
            userIndex = gwHandler.userIndex;
            nodeIndex = gwHandler.nodeIndex;
            setPingRefreshInterval(gwHandler.pingRefreshInterval);
        }
    }

    @Override
    public void run() {
        sendPing();
        if (gwHandler != null) {
            gwHandler.pingSent();
        }
    }

    private void sendPing() {
        // sending ping packet
        if (ipAddressOnLAN.length() > 0) {
            soulissCommands.sendPing(SoulissBindingNetworkParameters.getDatagramSocket(), ipAddressOnLAN, nodeIndex,
                    userIndex, (byte) 0, (byte) 0);
            // ping packet sent
        }
    }

    public int getPingRefreshInterval() {
        return pingRefreshInterval;
    }

    public void setPingRefreshInterval(int pingRefreshInterval) {
        this.pingRefreshInterval = pingRefreshInterval;
    }
}
