/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.auth;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Indicates an error in the OAuth2 authorization process.
 *
 * @author Roland Edelhoff - Initial contribution
 */
@NonNullByDefault
public class OAuthException extends RuntimeException {
    private static final long serialVersionUID = -1863609233382694104L;

    public OAuthException(final String message) {
        super(message);
    }

    public OAuthException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
