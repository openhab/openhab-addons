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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This is the starting state of the {@link Connection} {@link StateMachine}.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class ConnectionStateInit extends AbstractConnectionState {
    public ConnectionStateInit(StateContext context, ScheduledExecutorService scheduler) {
        super(context, scheduler);
    }

    @Override
    public void startWorking() {
        nextState(ConnectionStateConnecting.class);
    }

    @Override
    public void onPckMessageReceived(String data) {
        // nothing
    }
}
