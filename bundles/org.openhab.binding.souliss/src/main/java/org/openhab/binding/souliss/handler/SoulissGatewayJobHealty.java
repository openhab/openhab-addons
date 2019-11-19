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
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SoulissCommonCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissGatewayJobHealty extends Thread {

    private Logger logger = LoggerFactory.getLogger(SoulissGatewayJobHealty.class);
    private String _iPAddressOnLAN;
    private byte _userIndex;
    private byte _nodeIndex;
    private int _healthRefreshInterval;

    private SoulissGatewayHandler gw;

    public SoulissGatewayJobHealty(Bridge bridge) {
        gw = (SoulissGatewayHandler) bridge.getHandler();
        _iPAddressOnLAN = gw.getGatewayIP();
        _userIndex = gw.userIndex;
        _nodeIndex = gw.nodeIndex;
        set_healthRefreshInterval(gw.healthRefreshInterval);
    }

    @Override
    public void run() {
        sendHEALTHY_REQUEST();
    }

    private void sendHEALTHY_REQUEST() {
        logger.debug("Sending healthy packet");
        if (_iPAddressOnLAN.length() > 0) {
            SoulissCommonCommands.sendHEALTY_REQUESTframe(SoulissBindingNetworkParameters.getDatagramSocket(),
                    _iPAddressOnLAN, _nodeIndex, _userIndex, gw.getNodes());
            logger.debug("Sent healthy packet");
        }
    }

    public int get_healthRefreshInterval() {
        return _healthRefreshInterval;
    }

    public void set_healthRefreshInterval(int _healthRefreshInterval) {
        this._healthRefreshInterval = _healthRefreshInterval;
    }
}