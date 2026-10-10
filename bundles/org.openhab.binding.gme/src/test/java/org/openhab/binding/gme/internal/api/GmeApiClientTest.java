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
package org.openhab.binding.gme.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.gme.internal.model.GmeGranularity;
import org.openhab.binding.gme.internal.model.GmePriceEntry;

@NonNullByDefault
class GmeApiClientTest {

    @Test
    void decodesZipAndFiltersPunEntries() throws IOException {
        String json = """
                [
                  {
                    "FlowDate":"20260910",
                    "Hour":"1",
                    "Market":"MGP",
                    "Zone":"PUN",
                    "Price":"198.020000",
                    "Period":"1"
                  },
                  {
                    "FlowDate":"20260910",
                    "Hour":"1",
                    "Market":"MGP",
                    "Zone":"NORD",
                    "Price":"190.000000",
                    "Period":"1"
                  },
                  {
                    "FlowDate":"20260910",
                    "Hour":"1",
                    "Market":"MI-A1",
                    "Zone":"PUN",
                    "Price":"180.000000",
                    "Period":"1"
                  }
                ]
                """;

        String contentResponse = zipAndEncode(json);

        List<GmePriceEntry> entries = GmeApiClient.parseMarketPriceContentResponse(contentResponse, GmeGranularity.PT15)
                .stream().filter(price -> "PUN".equals(price.zone())).toList();

        assertEquals(1, entries.size());
        assertEquals("MGP", entries.getFirst().market());
        assertEquals("PUN", entries.getFirst().zone());
        assertEquals(1, entries.getFirst().hour());
        assertEquals(GmeGranularity.PT15, entries.getFirst().granularity());
    }

    private static String zipAndEncode(String json) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            zip.putNextEntry(new ZipEntry("data.json"));
            zip.write(json.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        return Base64.getEncoder().encodeToString(output.toByteArray());
    }
}
