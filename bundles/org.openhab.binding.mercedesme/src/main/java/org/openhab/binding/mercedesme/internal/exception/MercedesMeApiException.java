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
 * The {@link MercedesMeApiException} is thrown if an exception occurs during API calls
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MercedesMeApiException extends Exception {

    private static final long serialVersionUID = 8841726242900964396L;

    public MercedesMeApiException(String message) {
        super(message);
    }
}
