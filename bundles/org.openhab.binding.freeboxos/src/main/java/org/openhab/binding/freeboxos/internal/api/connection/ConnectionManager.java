/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;
import org.openhab.binding.freeboxos.internal.handler.ApiHandler;

/**
 * The {@link ConnectionManager} is the Java class used to handle api requests
 * related to connection
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ConnectionManager extends RestManager {

    public ConnectionManager(ApiHandler apiHandler) {
        super(apiHandler);
    }

    public ConnectionStatus getStatus() throws FreeboxException {
        return apiHandler.get("connection/", ConnectionStatusResponse.class, true);
    }

    // Response classes and validity evaluations
    private static class ConnectionStatusResponse extends Response<ConnectionStatus> {
    }
}
