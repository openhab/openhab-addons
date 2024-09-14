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

package org.openhab.binding.sunsynk.internal.api.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SunSynkSendCommandException} represents a binding specific {@link Exception}
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class SunSynkSendCommandException extends Exception {

    private static final long serialVersionUID = 5L;

    public SunSynkSendCommandException(String message) {
        super(message);
    }

    public SunSynkSendCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public SunSynkSendCommandException(Throwable cause) {
        super(cause);
    }
}
