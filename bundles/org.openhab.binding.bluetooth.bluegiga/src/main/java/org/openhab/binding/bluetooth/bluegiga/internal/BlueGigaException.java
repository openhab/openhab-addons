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
package org.openhab.binding.bluetooth.bluegiga.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A runtime exception used in the internal code of this bundle.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@NonNullByDefault
public class BlueGigaException extends RuntimeException {

    private static final long serialVersionUID = 58882813509800169L;

    public BlueGigaException() {
        super();
    }

    public BlueGigaException(String message) {
        super(message);
    }

    public BlueGigaException(String message, Throwable cause) {
        super(message, cause);
    }
}
