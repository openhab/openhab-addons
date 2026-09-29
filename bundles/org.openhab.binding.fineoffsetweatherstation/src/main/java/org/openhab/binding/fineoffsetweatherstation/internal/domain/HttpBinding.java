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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;

/**
 * Binds an HTTP {@code get_livedata_info} entry to a measurand's parser. The optional {@code alternate} is used
 * when the reported unit does not fit the primary parser's canonical dimension - this lets the lux illumination
 * and the W/m² solar-radiation channel share the item code {@code 0x15}.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public final class HttpBinding {
    private final Measurand parser;
    private @Nullable Measurand alternate;

    HttpBinding(Measurand parser, @Nullable Measurand alternate) {
        this.parser = parser;
        this.alternate = alternate;
    }

    void setAlternate(Measurand alternate) {
        this.alternate = alternate;
    }

    public @Nullable MeasuredValue parse(String val, @Nullable String unit, @Nullable Integer channel,
            @Nullable ParserCustomizationType customizationType) {
        MeasuredValue value = parser.parseHttp(val, unit, channel, customizationType);
        @Nullable
        Measurand alternate = this.alternate;
        if (value == null && alternate != null) {
            value = alternate.parseHttp(val, unit, channel, customizationType);
        }
        return value;
    }
}
