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
 * A single TCP item code whose payload is an ordered sequence of {@link Parser} slots
 * ({@link Measurand}s and {@link Skip}s) parsed sequentially.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class MeasurandGroup implements Parser {
    private final Parser[] slots;

    MeasurandGroup(Parser... slots) {
        this.slots = slots;
    }

    @Override
    public int extractMeasuredValues(byte[] data, int offset, @Nullable Integer channel,
            @Nullable ParserCustomizationType customizationType, List<MeasuredValue> result,
            DebugDetails debugDetails) {
        int subOffset = 0;
        for (Parser slot : slots) {
            subOffset += slot.extractMeasuredValues(data, offset + subOffset, channel, customizationType, result,
                    debugDetails);
        }
        return subOffset;
    }
}
