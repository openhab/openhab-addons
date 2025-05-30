/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ConnectionException} is used for errors in the connection to the amazon server
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class ConnectionException extends Exception {

    private static final long serialVersionUID = 1L;

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
