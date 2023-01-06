/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.lcn.internal.common.LcnDefs;

/**
 * Settings for a connection to LCN-PCHK.
 *
 * @author Tobias Jüttner - Initial Contribution
 */
@NonNullByDefault
public class ConnectionSettings {

    /** Unique identifier for this connection. */
    private final String id;

    /** The user name for authentication. */
    private final String username;

    /** The password for authentication. */
    private final String password;

    /** The TCP/IP address or IP of the connection. */
    private final String address;

    /** The TCP/IP port of the connection. */
    private final int port;

    /** The dimming mode to use. */
    private final LcnDefs.OutputPortDimMode dimMode;

    /** The status-messages mode to use. */
    private final LcnDefs.OutputPortStatusMode statusMode;

    /** Timeout for requests. */
    private final long timeoutMSec;

    /**
     * Constructor.
     *
     * @param id the connnection's unique identifier
     * @param address the connection's TCP/IP address or IP
     * @param port the connection's TCP/IP port
     * @param username the user name for authentication
     * @param password the password for authentication
     * @param dimMode the dimming mode
     * @param statusMode the status-messages mode
     * @param timeout the request timeout
     */
    public ConnectionSettings(String id, String address, int port, String username, String password,
            LcnDefs.OutputPortDimMode dimMode, LcnDefs.OutputPortStatusMode statusMode, int timeout) {
        this.id = id;
        this.address = address;
        this.port = port;
        this.username = username;
        this.password = password;
        this.dimMode = dimMode;
        this.statusMode = statusMode;
        this.timeoutMSec = timeout;
    }

    /**
     * Gets the unique identifier for the connection.
     *
     * @return the unique identifier
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the user name used for authentication.
     *
     * @return the user name
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Gets the password used for authentication.
     *
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Gets the TCP/IP address or IP of the connection.
     *
     * @return the address or IP
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Gets the TCP/IP port of the connection.
     *
     * @return the port
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Gets the dimming mode to use for the connection.
     *
     * @return the dimming mode
     */
    public LcnDefs.OutputPortDimMode getDimMode() {
        return this.dimMode;
    }

    /**
     * Gets the status-messages mode to use for the connection.
     *
     * @return the status-messages mode
     */
    public LcnDefs.OutputPortStatusMode getStatusMode() {
        return this.statusMode;
    }

    /**
     * Gets the request timeout.
     *
     * @return the timeout in milliseconds
     */
    public long getTimeout() {
        return this.timeoutMSec;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof ConnectionSettings)) {
            return false;
        }
        ConnectionSettings other = (ConnectionSettings) o;
        return this.id.equals(other.id) && this.address.equals(other.address) && this.port == other.port
                && this.username.equals(other.username) && this.password.equals(other.password)
                && this.dimMode == other.dimMode && this.statusMode == other.statusMode
                && this.timeoutMSec == other.timeoutMSec;
    }
}
