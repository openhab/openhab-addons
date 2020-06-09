/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.common.LcnAddr;

/**
 * This state is active when the connection failed. A grace period is enforced to prevent fast cycling through the
 * states.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class ConnectionStateGracePeriodBeforeReconnect extends AbstractConnectionState {
    private static final int RECONNECT_GRACE_PERIOD_SEC = 5;

    public ConnectionStateGracePeriodBeforeReconnect(StateContext context, ScheduledExecutorService scheduler) {
        super(context, scheduler);
    }

    @Override
    public void startWorking() {
        closeSocketChannel();

        addTimer(scheduler.schedule(() -> nextState(ConnectionStateConnecting.class), RECONNECT_GRACE_PERIOD_SEC,
                TimeUnit.SECONDS));
    }

    @Override
    public void queue(LcnAddr addr, boolean wantsAck, byte[] data) {
        connection.queueOffline(addr, wantsAck, data);
    }

    @Override
    public void onPckMessageReceived(String data) {
        // nothing
    }
}
