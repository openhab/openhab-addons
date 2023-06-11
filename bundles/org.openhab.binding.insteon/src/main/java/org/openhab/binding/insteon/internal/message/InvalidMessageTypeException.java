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
 * Exception to be thrown from Msg class
 *
 * @author Rob Nielsen - Initial contribution
 */
@NonNullByDefault
public class InvalidMessageTypeException extends Exception {
    private static final long serialVersionUID = -7582457696582413074L;

    public InvalidMessageTypeException(String message) {
        super(message);
    }
}
