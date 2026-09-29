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
package org.openhab.binding.tuya.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceSchema;

import com.google.gson.Gson;

/**
 * The {@link SchemaDpTest} verifies the conversion of cloud schema descriptions to {@link SchemaDp}.
 *
 * @author Carlo Dischler - Initial contribution
 */
@NonNullByDefault
public class SchemaDpTest {
    private final Gson gson = new Gson();

    @ParameterizedTest
    @CsvSource({ "Bitmap,bitmap", "Boolean,bool", "Enum,enum", "Integer,value", "String,string", "Json,string" })
    public void remoteTypeIsMappedToLocalType(String remoteType, String localType) {
        DeviceSchema.Description description = new DeviceSchema.Description();
        description.code = "fault";
        description.dp_id = 22;
        description.type = remoteType;
        description.values = "{\"label\":[\"E1\",\"E2\"],\"maxlen\":4}";

        SchemaDp schemaDp = SchemaDp.fromRemoteSchema(gson, description, Boolean.TRUE);

        assertEquals(localType, schemaDp.type);
        assertEquals(22, schemaDp.id);
        assertEquals("fault", schemaDp.code);
        assertEquals(Boolean.TRUE, schemaDp.readOnly);
    }

    @Test
    public void unknownRemoteTypeFallsBackToRaw() {
        DeviceSchema.Description description = new DeviceSchema.Description();
        description.code = "unknown";
        description.dp_id = 1;
        description.type = "Raw";

        SchemaDp schemaDp = SchemaDp.fromRemoteSchema(gson, description, Boolean.FALSE);

        assertEquals("raw", schemaDp.type);
    }
}
