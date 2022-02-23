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
package org.openhab.binding.freeboxos.internal.api.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.connection.ConnectionStatus.ConnectionStatusResponse;
import org.openhab.binding.freeboxos.internal.api.rest.ConfigurableRest;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;

/**
 * The {@link ConnectionManager} is the Java class used to handle api requests
 * related to connection
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ConnectionManager extends ConfigurableRest<ConnectionStatus, ConnectionStatusResponse> {
    private static final String CONNECTION_PATH = "connection";

    public ConnectionManager(FreeboxOsSession session) {
        super(session, ConnectionStatusResponse.class, CONNECTION_PATH, null);
    }
}
