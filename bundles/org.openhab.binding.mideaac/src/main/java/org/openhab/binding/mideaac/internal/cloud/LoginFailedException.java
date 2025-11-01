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

package org.openhab.binding.mideaac.internal.cloud;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LoginFailedException} is used to indicate login failures to Midea
 * cloud services.
 *
 * @author Bob Eckhoff - Initial contribution
 */

@NonNullByDefault
public class LoginFailedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LoginFailedException(String message) {
        super(message);
    }
}
