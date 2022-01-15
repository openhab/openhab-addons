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
package org.openhab.binding.homeconnect.internal.client.exception;

import static java.lang.String.format;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * API communication exception - appliance offline
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public class ApplianceOfflineException extends Exception {

    private static final long serialVersionUID = 1L;

    public ApplianceOfflineException(int code, String message, String body) {
        super(format("Communication error - appliance offline! response code: %d, message: %s, body: %s (Tried at %s)",
                code, message, body, new Date()));
    }
}
