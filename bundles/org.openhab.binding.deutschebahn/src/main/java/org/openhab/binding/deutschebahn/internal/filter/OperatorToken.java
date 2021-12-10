/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
 * Operator Tokens within filter expressions.
 * 
 * @author Sönke Küper - Initial contribution.
 */
@NonNullByDefault
public enum OperatorToken implements FilterToken {

    /**
     * And Token.
     */
    AND,

    /**
     * Or-Token.
     */
    OR,

    /**
     * Opening bracket.
     */
    BRACKET_OPEN,

    /**
     * Closing bracket.
     */
    BRACKET_CLOSE;

    @Override
    public <R> R accept(FilterTokenVisitor<R> visitor) throws FilterParserException {
        return visitor.handle(this);
    }
}
