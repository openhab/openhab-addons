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
package org.openhab.binding.enturno.internal.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link EnturCommunicationException} is a communication exception for the connections to Entur API.
 *
 * @author Michal Kloc - Initial contribution
 */
@NonNullByDefault
public class EnturCommunicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EnturCommunicationException(@Nullable String message) {
        super(message);
    }

    public EnturCommunicationException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
