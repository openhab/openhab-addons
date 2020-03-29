/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.dwdpollenflug.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DWDPollenflugBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDDataException extends Exception {

    private static final long serialVersionUID = 1L;

    public DWDDataException(String message) {
        super(message);
    }

    public DWDDataException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
