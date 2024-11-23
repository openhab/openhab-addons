/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal.connection;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.common.LcnAddr;
import org.openhab.binding.lcn.internal.common.PckGenerator;

/**
 * This state is active when the connection to the LCN bus has been established successfully and data can be sent and
 * retrieved.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class ConnectionStateConnected extends AbstractConnectionState {
    private static final int PING_INTERVAL_SEC = 60;
    private int pingCounter;

    public ConnectionStateConnected(ConnectionStateMachine context) {
        super(context);
    }

    @Override
    public void startWorking() {
        // send periodic keep-alives to keep the connection open
        addTimer(getScheduler().scheduleWithFixedDelay(
                () -> connection.queueDirectlyPlainText(PckGenerator.ping(++pingCounter)), PING_INTERVAL_SEC,
                PING_INTERVAL_SEC, TimeUnit.SECONDS));

        // run ModInfo.update() for every LCN module
        addTimer(getScheduler().scheduleWithFixedDelay(connection::updateModInfos, 0, 1, TimeUnit.SECONDS));

        connection.sendOfflineQueue();
    }

    @Override
    public void queue(LcnAddr addr, boolean wantsAck, byte[] data) {
        connection.queueDirectly(addr, wantsAck, data);
    }

    @Override
    public void onPckMessageReceived(String data) {
        parseLcnBusDiconnectMessage(data);
    }
}
