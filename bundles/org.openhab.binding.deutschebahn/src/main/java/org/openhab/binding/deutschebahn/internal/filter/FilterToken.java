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
package org.openhab.binding.deutschebahn.internal.filter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A token representing a part of a filter expression.
 * 
 * @author Sönke Küper - Initial contribution.
 */
@NonNullByDefault
public abstract class FilterToken {

    private final int position;

    /**
     * Creates a new {@link FilterToken}.
     */
    public FilterToken(int position) {
        this.position = position;
    }

    /**
     * Returns the start position of the token.
     */
    public final int getPosition() {
        return position;
    }

    /**
     * Accept for {@link FilterTokenVisitor}.
     */
    public abstract <R> R accept(FilterTokenVisitor<R> visitor) throws FilterParserException;
}
