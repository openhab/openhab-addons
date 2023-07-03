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
package org.openhab.binding.energidataservice.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link MissingPriceException} is thrown when there are no prices
 * available in the requested interval, e.g. when performing a calculation.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class MissingPriceException extends Exception {

    private static final long serialVersionUID = 1L;

    public MissingPriceException(String message) {
        super(message);
    }
}
