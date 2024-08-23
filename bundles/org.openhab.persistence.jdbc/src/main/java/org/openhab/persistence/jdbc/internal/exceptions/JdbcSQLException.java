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
package org.openhab.persistence.jdbc.internal.exceptions;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.knowm.yank.exceptions.YankSQLException;

/**
 * This exception wraps a {@link YankSQLException}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class JdbcSQLException extends JdbcException {

    private static final long serialVersionUID = 4562191548585905000L;

    public JdbcSQLException(YankSQLException sqlException) {
        super(Objects.requireNonNull(sqlException.getMessage()));
    }
}
