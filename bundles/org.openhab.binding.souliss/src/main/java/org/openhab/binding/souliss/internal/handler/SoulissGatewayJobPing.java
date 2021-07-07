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
public class SoulissGatewayJobPing implements Runnable {

    private final CommonCommands soulissCommands = new CommonCommands();

    @Nullable
    private SoulissGatewayHandler gwHandler;

    public SoulissGatewayJobPing(Bridge bridge) {
        var bridgeHandler = bridge.getHandler();
        if (bridgeHandler != null) {
            gwHandler = (SoulissGatewayHandler) bridgeHandler;
        }
    }

    @Override
    public void run() {
        @Nullable
        SoulissGatewayHandler localGwHandler = this.gwHandler;
        if (localGwHandler != null) {
            sendPing(localGwHandler);

            localGwHandler.pingSent();
        }
    }

    private void sendPing(SoulissGatewayHandler soulissGwHandler) {
        // sending ping packet

        if (soulissGwHandler.gwConfig.gatewayLanAddress.length() > 0) {
            soulissCommands.sendPing(soulissGwHandler.gwConfig);
            // ping packet sent
        }
    }
}
