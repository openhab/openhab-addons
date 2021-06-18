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
import org.openhab.binding.souliss.internal.protocol.CommonCommands;
import org.openhab.core.thing.Bridge;

/**
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */

@NonNullByDefault
public class SoulissGatewayJobHealthy extends Thread {

    private String ipAddressOnLAN = "";
    private byte userIndex = 0;
    private byte nodeIndex = 0;
    private int healthRefreshInterval = 0;

    private final CommonCommands soulissCommands = new CommonCommands();

    @Nullable
    private SoulissGatewayHandler gwHandler;

    @SuppressWarnings("null")
    public SoulissGatewayJobHealthy(Bridge bridge) {
        this.gwHandler = (SoulissGatewayHandler) bridge.getHandler();
        if (gwHandler != null) {
            this.ipAddressOnLAN = gwHandler.ipAddressOnLAN;
            this.userIndex = gwHandler.userIndex;
            this.nodeIndex = gwHandler.nodeIndex;
            this.sethealthRefreshInterval(gwHandler.healthRefreshInterval);
        }
    }

    @Override
    public void run() {
        sendHealthyRequest();
    }

    @SuppressWarnings("null")
    private void sendHealthyRequest() {
        // sending healthy packet
        if (ipAddressOnLAN.length() > 0 && this.gwHandler != null) {
            soulissCommands.sendHealthyRequestFrame(ipAddressOnLAN, nodeIndex, userIndex, this.gwHandler.getNodes());
            // healthy packet sent
        }
    }

    public int gethealthRefreshInterval() {
        return healthRefreshInterval;
    }

    public void sethealthRefreshInterval(int healthRefreshInterval) {
        this.healthRefreshInterval = healthRefreshInterval;
    }
}
