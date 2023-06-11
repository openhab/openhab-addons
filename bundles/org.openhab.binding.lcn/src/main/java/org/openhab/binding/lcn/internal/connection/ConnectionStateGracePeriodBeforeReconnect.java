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

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This state is active when the connection failed. A grace period is enforced to prevent fast cycling through the
 * states.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class ConnectionStateGracePeriodBeforeReconnect extends AbstractConnectionState {
    private static final int RECONNECT_GRACE_PERIOD_SEC = 5;

    public ConnectionStateGracePeriodBeforeReconnect(ConnectionStateMachine context) {
        super(context);
    }

    @Override
    public void startWorking() {
        closeSocketChannel();

        addTimer(getScheduler().schedule(() -> nextState(ConnectionStateConnecting::new), RECONNECT_GRACE_PERIOD_SEC,
                TimeUnit.SECONDS));
    }

    @Override
    public void onPckMessageReceived(String data) {
        // nothing
    }

    @Override
    public void handleConnectionFailed(@Nullable Throwable e) {
        // nothing
    }
}
