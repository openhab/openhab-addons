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
 * What a TCP item code resolves to: a {@link Parser} (a single {@link Measurand}, a
 * {@link MeasurandGroup}, or a {@link Skip}) plus the channel for multi-channel measurands.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class CodeBinding {
    private final Parser parser;
    private final @Nullable Integer channel;
    private final String debugName;

    CodeBinding(Parser parser, @Nullable Integer channel, String debugName) {
        this.parser = parser;
        this.channel = channel;
        this.debugName = debugName;
    }

    public int extractMeasuredValues(byte[] data, int offset, @Nullable ParserCustomizationType customizationType,
            List<MeasuredValue> result, DebugDetails debugDetails) {
        return parser.extractMeasuredValues(data, offset, channel, customizationType, result, debugDetails);
    }

    public String getDebugString() {
        return debugName + (channel == null ? "" : " channel " + channel);
    }
}
