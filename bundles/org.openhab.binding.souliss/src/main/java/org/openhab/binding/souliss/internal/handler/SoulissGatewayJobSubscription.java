/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.core.thing.Bridge;

/**
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */

@NonNullByDefault
public class SoulissGatewayJobSubscription implements Runnable {

    private @Nullable SoulissGatewayHandler gwHandler;

    public SoulissGatewayJobSubscription(Bridge bridge) {
        this.gwHandler = (SoulissGatewayHandler) bridge.getHandler();
    }

    @Override
    public void run() {
        sendSubscription();
    }

    private void sendSubscription() {
        SoulissGatewayHandler localGwHandler = this.gwHandler;
        if (localGwHandler != null) {
            localGwHandler.sendSubscription();
        }
    }
}
