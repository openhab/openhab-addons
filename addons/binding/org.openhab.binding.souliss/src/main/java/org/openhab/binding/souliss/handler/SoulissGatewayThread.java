/**
 * Copyright (c) 2014-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class SoulissGatewayThread extends Thread {

    private Logger logger = LoggerFactory.getLogger(SoulissGatewayThread.class);
    private String _iPAddressOnLAN;
    private short _userIndex;
    private short _nodeIndex;
    private double millisTime1, millisTime2;
    private double millisTime3, millisTime4;
    private int _pingRefreshInterval;
    private int _subscriptionRefreshInterval;
    private int _afterThingDetection_subscriptionRefreshInterval;
    private int _healthRefreshInterval;
    private String _gwID;
    private SoulissGatewayHandler gw;
    int sCount = 10;

    public SoulissGatewayThread(Bridge bridge) {
        gw = (SoulissGatewayHandler) bridge.getHandler();
        _iPAddressOnLAN = gw.IPAddressOnLAN;
        _userIndex = gw.userIndex;
        _nodeIndex = gw.nodeIndex;
        // _nodes = gw.nodes;
        _pingRefreshInterval = gw.pingRefreshInterval;
        _subscriptionRefreshInterval = gw.subscriptionRefreshInterval;
        _afterThingDetection_subscriptionRefreshInterval = gw.afterThingDetection_subscriptionRefreshInterval;
        _healthRefreshInterval = gw.healthRefreshInterval;
        _gwID = gw.getThing().getUID().getAsString();

    }

    @Override
    public void run() {
        double actualmillis;
        while (true) {
            actualmillis = System.currentTimeMillis();
            // PING - refresh Interval in seconds
            if (actualmillis - millisTime1 >= _pingRefreshInterval * 1000) {
                sendPing();
                gw.pingSent();
                millisTime1 = System.currentTimeMillis();
            }

            // SUBSCRIPTION - Value in minutes
            if (actualmillis - millisTime2 >= _subscriptionRefreshInterval * 1000 * 60) {
                sendSubscription();
                millisTime2 = System.currentTimeMillis();
            }
            // SUBSCRIPTION after Thing detection - Value in millis
            if (gw.thereIsAThingDetection
                    && actualmillis - millisTime4 >= _afterThingDetection_subscriptionRefreshInterval) {
                sendSubscription();
                millisTime4 = System.currentTimeMillis();
                if (--sCount <= 0) {
                    gw.resetThereIsAThingDetection();
                    sCount = 10;
                }

            }

            // HEALT - Value in seconds
            if (actualmillis - millisTime3 >= _healthRefreshInterval * 1000) {
                sendHEALTHY_REQUEST();
                millisTime3 = System.currentTimeMillis();
            }

        }

    }

    private void sendHEALTHY_REQUEST() {
        logger.debug("Sending healthy packet");
        if (_iPAddressOnLAN.length() > 0) {
            SoulissCommonCommands.sendHEALTY_REQUESTframe(SoulissBindingNetworkParameters.getDatagramSocket(),
                    _iPAddressOnLAN, _nodeIndex, _userIndex, gw.getNodes());
            logger.debug("Sent healthy packet");
        }
    }

    private void sendPing() {
        logger.debug("Sending ping packet");
        if (_iPAddressOnLAN.length() > 0) {
            SoulissCommonCommands.sendPing(SoulissBindingNetworkParameters.getDatagramSocket(), _iPAddressOnLAN,
                    _nodeIndex, _userIndex, (byte) 0, (byte) 0);
            logger.debug("Sent ping packet");
        }
    }

    private void sendSubscription() {
        gw.sendSubscription();
    }
}