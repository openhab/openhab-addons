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
import org.openhab.binding.lcn.internal.common.LcnException;

/**
 * Base class for sending username or password.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public abstract class AbstractConnectionStateSendCredentials extends AbstractConnectionState {
    private static final int AUTH_TIMEOUT_SEC = 10;

    public AbstractConnectionStateSendCredentials(ConnectionStateMachine context) {
        super(context);
    }

    @Override
    public void startWorking() {
        addTimer(getScheduler().schedule(() -> nextState(ConnectionStateConnecting::new), AUTH_TIMEOUT_SEC,
                TimeUnit.SECONDS));
    }

    /**
     * Starts a timeout when the PCK gateway does not answer to the credentials.
     */
    protected void startTimeoutTimer() {
        addTimer(getScheduler().schedule(
                () -> handleConnectionFailed(
                        new LcnException("Network timeout in state " + getClass().getSimpleName())),
                connection.getSettings().getTimeout(), TimeUnit.MILLISECONDS));
    }
}
