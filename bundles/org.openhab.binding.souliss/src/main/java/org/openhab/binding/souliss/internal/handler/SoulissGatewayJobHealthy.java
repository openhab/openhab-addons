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
public class SoulissGatewayJobHealthy implements Runnable {

    private final CommonCommands soulissCommands = new CommonCommands();

    @Nullable
    private SoulissGatewayHandler gwHandler;

    public SoulissGatewayJobHealthy(Bridge bridge) {
        this.gwHandler = (SoulissGatewayHandler) bridge.getHandler();
    }

    @Override
    public void run() {
        sendHealthyRequest();
    }

    private void sendHealthyRequest() {
        if (this.gwHandler != null) {
            // sending healthy packet
            if (this.gwHandler.gwConfig.gatewayLanAddress.length() > 0 && this.gwHandler != null) {
                soulissCommands.sendHealthyRequestFrame(this.gwHandler.gwConfig.gatewayLanAddress,
                        (byte) this.gwHandler.gwConfig.nodeIndex, (byte) this.gwHandler.gwConfig.userIndex,
                        this.gwHandler.getNodes());
                // healthy packet sent
            }
        }
    }
}
