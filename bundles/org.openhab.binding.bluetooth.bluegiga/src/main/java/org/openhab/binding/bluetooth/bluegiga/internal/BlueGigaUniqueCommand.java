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
package org.openhab.binding.bluetooth.bluegiga.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Container class to hold {@link BlueGigaCommand} and transaction id.
 *
 * @author Pauli Anttila - Initial contribution
 *
 */
@NonNullByDefault
public class BlueGigaUniqueCommand {
    private BlueGigaCommand msg;
    private int transactionId;

    BlueGigaUniqueCommand(BlueGigaCommand message, int transactionId) {
        this.msg = message;
        this.transactionId = transactionId;
    }

    int getTransactionId() {
        return transactionId;
    }

    BlueGigaCommand getMessage() {
        return msg;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BlueGigaUniqueCommand [transactionId=");
        builder.append(transactionId);
        builder.append(", BlueGigaCommand=");
        builder.append(msg);
        builder.append(']');
        return builder.toString();
    }
}
