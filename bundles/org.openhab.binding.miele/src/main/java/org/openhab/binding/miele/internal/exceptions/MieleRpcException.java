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
package org.openhab.binding.miele.internal.exceptions;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MieleRpcException} indicates failure to perform JSON-RPC call.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class MieleRpcException extends IOException {

    private static final long serialVersionUID = -8147063891196639054L;

    public MieleRpcException(String message) {
        super(message);
    }

    public MieleRpcException(String message, Throwable cause) {
        super(message, cause);
    }
}
