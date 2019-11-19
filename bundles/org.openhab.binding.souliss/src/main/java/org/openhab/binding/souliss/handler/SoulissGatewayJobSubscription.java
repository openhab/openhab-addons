/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.core.thing.Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissGatewayJobSubscription extends Thread {

    private Logger logger = LoggerFactory.getLogger(SoulissGatewayJobSubscription.class);
    private String _iPAddressOnLAN;
    private byte _userIndex;
    private byte _nodeIndex;
    private int _subscriptionRefreshInterval;

    private SoulissGatewayHandler gw;

    public SoulissGatewayJobSubscription(Bridge bridge) {
        gw = (SoulissGatewayHandler) bridge.getHandler();
        _iPAddressOnLAN = gw.getGatewayIP();
        _userIndex = gw.userIndex;
        _nodeIndex = gw.nodeIndex;
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