/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
    public abstract R handle(ChannelNameEquals equals) throws FilterParserException;

    /**
     * Handles {@link OrOperator}.
     */
    public abstract R handle(OrOperator operator) throws FilterParserException;

    /**
     * Handles {@link AndOperator}.
     */
    public abstract R handle(AndOperator operator) throws FilterParserException;

    /**
     * Handles {@link BracketOpenToken}.
     */
    public abstract R handle(BracketOpenToken token) throws FilterParserException;

    /**
     * Handles {@link BracketCloseToken}.
     */
    public abstract R handle(BracketCloseToken token) throws FilterParserException;
}
