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
 * A {@link Parser} that skips a fixed number of bytes without extracting any value.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
class Skip implements Parser {
    private final int skip;

    public Skip(int skip) {
        this.skip = skip;
    }

    @Override
    public int extractMeasuredValues(byte[] data, int offset, @Nullable Integer channel,
            @Nullable ParserCustomizationType customizationType, List<MeasuredValue> result,
            DebugDetails debugDetails) {
        debugDetails.addDebugDetails(offset, skip, "skipped");
        return skip;
    }
}
