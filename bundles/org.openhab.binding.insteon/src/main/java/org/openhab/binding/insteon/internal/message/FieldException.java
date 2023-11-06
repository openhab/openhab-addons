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
package org.openhab.binding.insteon.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception to be thrown if there is an error processing a field, for
 * example type mismatch, out of bounds etc.
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class FieldException extends Exception {
    private static final long serialVersionUID = -4749311173073727318L;

    public FieldException() {
        super();
    }

    public FieldException(String m) {
        super(m);
    }

    public FieldException(String m, Throwable cause) {
        super(m, cause);
    }

    public FieldException(Throwable cause) {
        super(cause);
    }
}
