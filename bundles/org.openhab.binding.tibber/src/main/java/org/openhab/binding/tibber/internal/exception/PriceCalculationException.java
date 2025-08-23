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
 * The {@link PriceCalculationException} exception is thrown if price calculations cannot be performed e.g.
 * stepping over boundaries.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PriceCalculationException extends Exception {
    private static final long serialVersionUID = 4330974498657720965L;

    public PriceCalculationException(String reason) {
        super(reason);
    }
}
