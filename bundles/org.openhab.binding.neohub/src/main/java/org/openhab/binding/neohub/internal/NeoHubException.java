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
package org.openhab.binding.neohub.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NeoHubException} is a custom checked exception for NeoHubs. It is thrown when the NeoHub encounters an
 * error that is NOT I/O related. i.e. if the binding is able to connect to the NeoHub but the NeoHub returns unexpected
 * results.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class NeoHubException extends Exception {

    private static final long serialVersionUID = -7358712540781217363L;

    public NeoHubException(String message) {
        super(message);
    }

    public NeoHubException(String message, Throwable cause) {
        super(message, cause);
    }
}
