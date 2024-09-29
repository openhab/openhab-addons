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
import org.openhab.binding.lcn.internal.common.PckGenerator;

/**
 * Sets the dimming mode range (0-50 or 0-200) in the LCN-PCK for this connection, as configured by the user.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class ConnectionStateSendDimMode extends AbstractConnectionState {
    public ConnectionStateSendDimMode(ConnectionStateMachine context) {
        super(context);
    }

    @Override
    public void startWorking() {
        connection.queueDirectlyPlainText(PckGenerator.setOperationMode(connection.getSettings().getDimMode(),
                connection.getSettings().getStatusMode()));

        nextState(ConnectionStateSegmentScan::new);
    }

    @Override
    public void onPckMessageReceived(String data) {
        // nothing
    }
}
