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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This state is entered when the LCN-PCK gateway sent a message, that the connection to the LCN bus was lost. This can
 * happen if the user plugs the USB cable to the PC coupler.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class ConnectionStateWaitForLcnBusConnectedAfterDisconnected extends ConnectionStateWaitForLcnBusConnected {
    public ConnectionStateWaitForLcnBusConnectedAfterDisconnected(ConnectionStateMachine context) {
        super(context);
    }

    @Override
    public void startWorking() {
        // nothing, don't start legacy timer
    }
}
