/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnException;

/**
 * This state waits for the status answer of the LCN-PCK gateway after connection establishment, rather the LCN bus is
 * connected.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class ConnectionStateWaitForLcnBusConnected extends AbstractConnectionState {
    private @Nullable ScheduledFuture<?> legacyTimer;

    public ConnectionStateWaitForLcnBusConnected(ConnectionStateMachine context) {
        super(context);
    }

    @Override
    public void startWorking() {
        // Legacy support for LCN-PCHK 2.2 and earlier:
        // There was no explicit "LCN connected" notification after successful authentication.
        // Only "LCN disconnected" would be reported immediately. That means "LCN connected" used to be the default.
        ScheduledFuture<?> localLegacyTimer = legacyTimer = getScheduler().schedule(() -> {
            connection.getCallback().onOnline();
            nextState(ConnectionStateSendDimMode::new);
        }, connection.getSettings().getTimeout(), TimeUnit.MILLISECONDS);
        addTimer(localLegacyTimer);
    }

    @Override
    public void onPckMessageReceived(String data) {
        switch (data) {
            case LcnDefs.LCNCONNSTATE_DISCONNECTED:
                cancelLegacyTimer();
                connection.getCallback().onOffline("LCN-PCHK/VISU not connected to LCN data wire");
                break;
            case LcnDefs.LCNCONNSTATE_CONNECTED:
                cancelLegacyTimer();
                connection.getCallback().onOnline();
                nextState(ConnectionStateSendDimMode::new);
                break;
            case LcnDefs.INSUFFICIENT_LICENSES:
                cancelLegacyTimer();
                handleConnectionFailed(
                        new LcnException("LCN-PCHK/VISU has not enough licenses to handle this connection"));
                break;
        }
    }

    private void cancelLegacyTimer() {
        ScheduledFuture<?> localLegacyTimer = legacyTimer;
        if (localLegacyTimer != null) {
            localLegacyTimer.cancel(true);
        }
    }
}
