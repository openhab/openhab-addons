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

import org.openhab.core.thing.Bridge;

/**
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
public class SoulissGatewayJobSubscription extends Thread {

    private int subscriptionRefreshInterval;

    private SoulissGatewayHandler gw;

    public SoulissGatewayJobSubscription(Bridge bridge) {
        gw = (SoulissGatewayHandler) bridge.getHandler();
        setSubscriptionRefreshInterval(gw.subscriptionRefreshInterval);
    }

    @Override
    public void run() {
        sendSubscription();
    }

    public int getSubscriptionRefreshInterval() {
        return subscriptionRefreshInterval;
    }

    public void setSubscriptionRefreshInterval(int subscriptionRefreshInterval) {
        this.subscriptionRefreshInterval = subscriptionRefreshInterval;
    }

    private void sendSubscription() {
        gw.sendSubscription();
    }
}
