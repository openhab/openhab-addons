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
package org.openhab.binding.insteon.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception to indicate various xml parsing errors.
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class ParsingException extends Exception {
    private static final long serialVersionUID = 3997461423241843949L;

    public ParsingException(String msg) {
        super(msg);
    }

    public ParsingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
