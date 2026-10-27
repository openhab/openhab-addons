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
package org.openhab.binding.viessmann.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.viessmann.internal.dto.features.FeatureProperties;
import org.openhab.binding.viessmann.internal.handler.DeviceHandler.AdditionalProperty;

import com.google.gson.Gson;

/**
 * Tests for {@link DeviceHandler}.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DeviceHandlerTest {

    private static final String DEVICE_SERIAL_PROPERTIES = """
            {
              "value": {
                "type": "string",
                "value": "7956286403665124"
              },
              "vin": {
                "type": "VinEntry",
                "value": {
                  "uid": null,
                  "profile": null
                }
              },
              "materialId": {
                "type": "MaterialIdEntry",
                "value": {
                  "uid": null,
                  "profile": null
                }
              },
              "measurements": {
                "type": "MeasurementEntries",
                "value": [1, 2]
              }
            }
            """;

    @Test
    public void testUnsupportedSerialPropertiesAreSkipped() {
        FeatureProperties properties = Objects
                .requireNonNull(new Gson().fromJson(DEVICE_SERIAL_PROPERTIES, FeatureProperties.class));

        AdditionalProperty vin = DeviceHandler.resolveAdditionalProperty(properties, "vin");
        AdditionalProperty materialId = DeviceHandler.resolveAdditionalProperty(properties, "materialId");
        AdditionalProperty measurements = DeviceHandler.resolveAdditionalProperty(properties, "measurements");

        assertNull(vin);
        assertNull(materialId);

        assertNotNull(measurements);
        assertEquals("MeasurementEntries", measurements.type());
        assertEquals("[1,2]", measurements.value());
    }

    private static final String STATUS_MESSAGE_PROPERTIES = """
            {
              "errorEntries": {
                "type": "array",
                "value": [
                  { "errorCode": "S.125", "busAddress": "1", "priority": "status", "audiences": ["IS-SUPPLIER"] },
                  { "errorCode": "S.131", "busAddress": "1", "priority": "status", "audiences": [] }
                ]
              }
            }
            """;

    @Test
    public void testStatusMessageCodesAreJoined() {
        FeatureProperties properties = Objects
                .requireNonNull(new Gson().fromJson(STATUS_MESSAGE_PROPERTIES, FeatureProperties.class));

        assertTrue(properties.getUsedEntries().contains("errorEntries"));
        assertEquals("S.125,S.131", DeviceHandler.joinErrorCodes(properties.errorEntries));
    }

    @Test
    public void testNoStatusMessagesGiveEmptyCodeList() {
        assertEquals("", DeviceHandler.joinErrorCodes(null));
    }
}
