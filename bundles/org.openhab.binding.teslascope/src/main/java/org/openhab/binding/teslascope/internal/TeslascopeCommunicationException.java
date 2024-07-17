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
package org.openhab.binding.teslascope.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception for when an unexpected response is received from the Teslascope API.
 *
 * @author Paul Smedley - Initial contribution
 *
 */
@NonNullByDefault
public class TeslascopeCommunicationException extends Exception {
    private static final long serialVersionUID = 529232811860854017L;

    public TeslascopeCommunicationException(String message) {
        super(message);
    }

    public TeslascopeCommunicationException(Throwable ex) {
        super(ex);
    }

    public TeslascopeCommunicationException(@Nullable String message, Throwable cause) {
        super(message, cause);
    }
}
