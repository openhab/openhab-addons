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
package org.openhab.persistence.jdbc.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for JDBC exceptions.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class JdbcException extends Exception {

    private static final long serialVersionUID = 1911437557128995424L;

    public JdbcException(String message) {
        super(message);
    }

    public JdbcException(String message, Throwable cause) {
        super(message, cause);
    }
}
