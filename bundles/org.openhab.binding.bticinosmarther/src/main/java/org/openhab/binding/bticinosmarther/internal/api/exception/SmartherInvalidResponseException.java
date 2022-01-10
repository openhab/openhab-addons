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
package org.openhab.binding.bticinosmarther.internal.api.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Signals that an "invalid response" messaging issue with API gateway has occurred.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherInvalidResponseException extends SmartherGatewayException {

    private static final long serialVersionUID = 3166922285185480855L;

    /**
     * Constructs a {@code SmartherInvalidResponseException} with the specified detail message.
     *
     * @param message
     *            the error message returned from the API gateway
     */
    public SmartherInvalidResponseException(@Nullable String message) {
        super(message);
    }
}
