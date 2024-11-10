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
package org.openhab.binding.energidataservice.internal.api.filter.dto;

import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.energidataservice.internal.api.ChargeTypeCode;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameter;
import org.openhab.binding.energidataservice.internal.api.filter.serialization.ChargeTypeCodeDeserializer;
import org.openhab.binding.energidataservice.internal.api.filter.serialization.DateQueryParameterDeserializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Record for deserializing YAML grid tariff definitions.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public record DatahubFilter(String gln,
        @JsonDeserialize(contentUsing = ChargeTypeCodeDeserializer.class) Set<ChargeTypeCode> chargeTypeCodes,
        Set<String> notes, @JsonDeserialize(using = DateQueryParameterDeserializer.class) DateQueryParameter start) {
    @Override
    public DateQueryParameter start() {
        return Objects.isNull(start) ? DateQueryParameter.EMPTY : start;
    }

    @Override
    public Set<String> notes() {
        return Objects.isNull(notes) ? Set.of() : notes;
    }

    @Override
    public String toString() {
        return "DatahubFilter{gln='" + gln + "', chargeTypeCodes=" + chargeTypeCodes + "', notes=" + notes + "', start="
                + start + '}';
    }
}
