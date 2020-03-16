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

import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.common.LcnAddr;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.NullScheduledFuture;

/**
 * This state waits for the status answer of the LCN-PCK gateway after connection establishment, rather the LCN bus is
 * connected.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class ConnectionStateWaitForLcnBusConnected extends AbstractConnectionState {
    private ScheduledFuture<?> legacyTimer = NullScheduledFuture.getInstance();

    public ConnectionStateWaitForLcnBusConnected(StateContext context, ScheduledExecutorService scheduler) {
        super(context, scheduler);
    }

    @Override
    public void startWorking() {
        // Legacy support for LCN-PCHK 2.2 and earlier:
        // There was no explicit "LCN connected" notification after successful authentication.
        // Only "LCN disconnected" would be reported immediately. That means "LCN connected" used to be the default.
        addTimer(legacyTimer = scheduler.schedule(() -> {
            connection.getCallback().onOnline();
            nextState(ConnectionStateSendDimMode.class);
        }, connection.getSettings().getTimeout(), TimeUnit.MILLISECONDS));
    }

    @Override
    public void queue(LcnAddr addr, boolean wantsAck, ByteBuffer data) {
        connection.queueOffline(addr, wantsAck, data);
    }

    @Override
    public void onPckMessageReceived(String data) {
        if (data.equals(LcnDefs.LCNCONNSTATE_DISCONNECTED)) {
            legacyTimer.cancel(true);
            connection.getCallback().onOffline("LCN bus not connected to LCN-PCHK/PKE");
        } else if (data.equals(LcnDefs.LCNCONNSTATE_CONNECTED)) {
            legacyTimer.cancel(true);
            connection.getCallback().onOnline();
            nextState(ConnectionStateSendDimMode.class);
        } else if (data.equals(LcnDefs.INSUFFICIENT_LICENSES)) {
            context.handleConnectionFailed(
                    new LcnException("LCN-PCHK/PKE has not enough licenses to handle this connection"));
        }
    }
}
