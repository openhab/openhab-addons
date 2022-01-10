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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lcn.internal.common.LcnAddr;

/**
 * This state is entered when the connection shall be shut-down finally. This happens when Thing.dispose() is called.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class ConnectionStateShutdown extends AbstractConnectionState {
    public ConnectionStateShutdown(ConnectionStateMachine context) {
        super(context);
    }

    @Override
    public void startWorking() {
        closeSocketChannel();

        // end state
    }

    @Override
    public void queue(LcnAddr addr, boolean wantsAck, byte[] data) {
        // nothing
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
