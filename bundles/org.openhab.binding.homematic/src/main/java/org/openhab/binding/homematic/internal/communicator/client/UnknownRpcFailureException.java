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
package org.openhab.binding.homematic.internal.communicator.client;

import java.io.IOException;

/**
 * Exception if the RPC call returns a unknown -1 Failure.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public class UnknownRpcFailureException extends IOException {
    private static final long serialVersionUID = -5695414238422364040L;

    public UnknownRpcFailureException(String message) {
        super(message);
    }
}
