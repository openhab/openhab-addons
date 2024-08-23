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
package org.openhab.binding.hue.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown when API is returning an unexpected empty response.
 *
 * @author Laurent Garnier - Initial contribution
 */
@SuppressWarnings("serial")
@NonNullByDefault
public class EmptyResponseException extends ApiException {
    public EmptyResponseException() {
    }

    public EmptyResponseException(String message) {
        super(message);
    }
}
