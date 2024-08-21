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
 * Visitor for {@link FilterToken}.
 * 
 * @author Sönke Küper - Initial Contribution.
 *
 * @param <R> Return type.
 */
@NonNullByDefault
public interface FilterTokenVisitor<R> {

    /**
     * Handles {@link ChannelNameEquals}.
     */
    R handle(ChannelNameEquals equals) throws FilterParserException;

    /**
     * Handles {@link OrOperator}.
     */
    R handle(OrOperator operator) throws FilterParserException;

    /**
     * Handles {@link AndOperator}.
     */
    R handle(AndOperator operator) throws FilterParserException;

    /**
     * Handles {@link BracketOpenToken}.
     */
    R handle(BracketOpenToken token) throws FilterParserException;

    /**
     * Handles {@link BracketCloseToken}.
     */
    R handle(BracketCloseToken token) throws FilterParserException;
}
