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
package org.openhab.binding.mielecloud.internal.webservice.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This {@link RuntimeException} is thrown if an error occurred due to authorization failure.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class AuthorizationFailedException extends RuntimeException {
    private static final long serialVersionUID = 963609531804668970L;

    public AuthorizationFailedException(final String message) {
        super(message);
    }
}
