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
 * Abstraction for all operators.
 * 
 * @author Sönke Küper - initial contribution.
 */
@NonNullByDefault
public abstract class OperatorToken extends FilterToken {

    /**
     * Creates an new {@link OperatorToken}.
     */
    public OperatorToken(int position) {
        super(position);
    }
}
