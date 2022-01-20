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
package org.openhab.binding.hdpowerview.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HubInvalidResponseException} is a custom exception for the HD PowerView Hub
 * which is thrown when a response does not adhere to a defined contract.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class HubInvalidResponseException extends HubProcessingException {

    private static final long serialVersionUID = -2293572741003905474L;

    public HubInvalidResponseException(String message) {
        super(message);
    }

    public HubInvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
