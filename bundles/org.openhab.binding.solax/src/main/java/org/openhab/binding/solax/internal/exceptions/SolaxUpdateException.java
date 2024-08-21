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
package org.openhab.binding.solax.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SolaxUpdateException} exception thrown to the abstract class from the sub-classes if something goes wrong
 * with the data update
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class SolaxUpdateException extends Exception {

    private static final long serialVersionUID = 1L;
    private @Nullable Object[] args;

    public SolaxUpdateException(String message, @Nullable Object... args) {
        super(message);
        this.args = args;
    }

    public @Nullable Object[] getArgs() {
        return args;
    }
}
