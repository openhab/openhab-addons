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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energidataservice.internal.api.ChargeTypeCode;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * The {@link ChargeTypeCodeDeserializer} converts a charge type code string
 * into a {@link ChargeTypeCode} object.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ChargeTypeCodeDeserializer extends StdDeserializer<ChargeTypeCode> {
    public ChargeTypeCodeDeserializer() {
        super(ChargeTypeCode.class);
    }

    @Override
    public ChargeTypeCode deserialize(@Nullable JsonParser p, @Nullable DeserializationContext ctxt) {
        if (p == null) {
            throw new IllegalArgumentException("JsonParser is null");
        }
        return ChargeTypeCode.of(p.getString());
    }
}
