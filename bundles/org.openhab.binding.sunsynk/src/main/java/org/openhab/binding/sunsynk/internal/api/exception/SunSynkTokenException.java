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
 * The {@link SunSynkTokenException} represents a binding specific {@link Exception}
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class SunSynkTokenException extends Exception {

    private static final long serialVersionUID = 6L;

    public SunSynkTokenException(String message) {
        super(message);
    }

    public SunSynkTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public SunSynkTokenException(Throwable cause) {
        super(cause);
    }
}
