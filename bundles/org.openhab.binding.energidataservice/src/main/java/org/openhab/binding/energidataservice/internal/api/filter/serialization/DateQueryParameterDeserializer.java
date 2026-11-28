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
package org.openhab.binding.energidataservice.internal.api.filter.serialization;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameter;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameterType;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * The {@link DateQueryParameterDeserializer} converts a string representation
 * of either a {@link LocalDate} or {@link DateQueryParameterType} into a
 * {@link DateQueryParameter}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DateQueryParameterDeserializer extends StdDeserializer<DateQueryParameter> {
    public DateQueryParameterDeserializer() {
        super(DateQueryParameter.class);
    }

    @Override
    public DateQueryParameter deserialize(@Nullable JsonParser p, @Nullable DeserializationContext ctxt) {
        if (p == null) {
            throw new IllegalArgumentException("JsonParser is null");
        }
        String value = p.getString();
        try {
            LocalDate date = LocalDate.parse(value);
            return DateQueryParameter.of(date);
        } catch (DateTimeParseException e) {
            return DateQueryParameter.of(DateQueryParameterType.of(value));
        }
    }
}
