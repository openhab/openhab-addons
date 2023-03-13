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
package org.openhab.binding.ring.internal.errors;

/**
 * AuthenticationException will be thrown if an invalid username or
 * password is used to get access to the Ring account.
 *
 * @author Wim Vissers - Initial contribution
 */

public class AuthenticationException extends Exception {

    private static final long serialVersionUID = -2630294607218363771L;

    public AuthenticationException(String message) {
        super(message);
    }
}
