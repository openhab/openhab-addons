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
package org.openhab.binding.deutschebahn.internal.filter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception showing problems during parsing a filter expression.
 * 
 * @author Sönke Küper - initial contribution.
 */
@NonNullByDefault
public final class FilterParserException extends Exception {

    private static final long serialVersionUID = 3104578924298682889L;

    /**
     * Creates a new {@link FilterParserException}.
     */
    public FilterParserException(String message) {
        super(message);
    }
}
