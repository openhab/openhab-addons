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
import org.openhab.binding.lcn.internal.common.LcnDefs;

/**
 * This state sends the username during the authentication with the LCN-PCK gateway.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class ConnectionStateSendUsername extends AbstractConnectionStateSendCredentials {
    public ConnectionStateSendUsername(ConnectionStateMachine context) {
        super(context);
    }

    @Override
    public void startWorking() {
        startTimeoutTimer();
    }

    @Override
    public void onPckMessageReceived(String data) {
        if (data.equals(LcnDefs.AUTH_USERNAME)) {
            connection.queueDirectlyPlainText(connection.getSettings().getUsername());
            nextState(ConnectionStateSendPassword::new);
        }
    }
}
