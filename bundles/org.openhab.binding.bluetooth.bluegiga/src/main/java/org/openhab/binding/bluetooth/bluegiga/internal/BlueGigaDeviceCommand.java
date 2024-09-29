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
package org.openhab.binding.bluetooth.bluegiga.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Abstract base class for all device commands.
 *
 * @author Pauli Anttila - Initial contribution
 *
 */
@NonNullByDefault
public abstract class BlueGigaDeviceCommand extends BlueGigaCommand {

    /**
     * Connection handle.
     * <p>
     * BlueGiga API type is <i>uint8</i> - Java type is {@link int}
     */
    protected int connection;

    /**
     * Set connection handle.
     *
     * @param connection the connection to set as {@link int}
     */
    public final void setConnection(int connection) {
        this.connection = connection;
    }

    /**
     * Connection handle.
     * <p>
     * BlueGiga API type is <i>uint8</i> - Java type is {@link int}
     *
     * @return the current connection as {@link int}
     */
    public final int getConnection() {
        return connection;
    }
}
