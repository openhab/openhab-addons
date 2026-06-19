/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;

/**
 * Parser for a single measurand's binary payload.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
interface Parser {
    /**
     * Parses this parser's slice of a TCP item payload, appending any decoded values to {@code result}.
     *
     * @param data the full response buffer
     * @param offset the index in {@code data} where this parser's payload starts
     * @param channel the 1-based sensor channel for multi-channel measurands, or {@code null} for single-channel ones
     * @param customizationType the active protocol-specific reading variant, or {@code null} for the default
     * @param result collects the decoded {@link MeasuredValue}s
     * @param debugDetails accumulates a human-readable description of the parsed bytes
     * @return the number of bytes consumed starting at {@code offset}
     */
    int extractMeasuredValues(byte[] data, int offset, @Nullable Integer channel,
            @Nullable ParserCustomizationType customizationType, List<MeasuredValue> result, DebugDetails debugDetails);
}
