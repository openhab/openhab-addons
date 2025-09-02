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
package org.openhab.binding.mercedesme.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MercedesMeAuthException} is thrown if an exception occurs during authorization
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MercedesMeAuthException extends Exception {

    private static final long serialVersionUID = -5739643283190864467L;

    public MercedesMeAuthException(String message) {
        super(message);
    }
}
