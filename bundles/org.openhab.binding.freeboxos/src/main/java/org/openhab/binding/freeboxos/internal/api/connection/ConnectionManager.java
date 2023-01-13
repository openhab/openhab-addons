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
package org.openhab.binding.freeboxos.internal.api.connection;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.CONNECTION_PATH;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.connection.ConnectionResponses.StatusResponse;
import org.openhab.binding.freeboxos.internal.rest.ConfigurableRest;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;

/**
 * The {@link ConnectionManager} is the Java class used to handle api requests related to connection
 *
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ConnectionManager extends ConfigurableRest<ConnectionStatus, StatusResponse> {

    public ConnectionManager(FreeboxOsSession session) {
        super(session, StatusResponse.class, CONNECTION_PATH, null);
    }
}
