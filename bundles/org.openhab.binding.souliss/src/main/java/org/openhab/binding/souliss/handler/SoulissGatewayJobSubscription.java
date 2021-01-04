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
 */
public class SoulissGatewayJobSubscription extends Thread {

    private int _subscriptionRefreshInterval;

    private SoulissGatewayHandler gw;

    public SoulissGatewayJobSubscription(Bridge bridge) {
        gw = (SoulissGatewayHandler) bridge.getHandler();
        set_subscriptionRefreshInterval(gw.subscriptionRefreshInterval);
    }

    @Override
    public void run() {
        sendSubscription();
    }

    public int get_subscriptionRefreshInterval() {
        return _subscriptionRefreshInterval;
    }

    public void set_subscriptionRefreshInterval(int _subscriptionRefreshInterval) {
        this._subscriptionRefreshInterval = _subscriptionRefreshInterval;
    }

    private void sendSubscription() {
        gw.sendSubscription();
    }
}
