/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tibber.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CalculationParameterException} exception is thrown if ThingsActions parameters contains errors
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class CalculationParameterException extends Exception {
    private static final long serialVersionUID = -1841031906330289887L;

    public CalculationParameterException(String reason) {
        super(reason);
    }
}
