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
package org.openhab.binding.knx.internal.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.projectstore.LogCapture;
import org.openhab.binding.knx.internal.rest.dto.ErrorDTO;

/**
 * Checks the one error body: what a call returns, how a failure of this add-on and any other exception are answered.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxRestCallTest {
    private static final String MADE_UP_PATH = "/made/up/userdata/knx/project-knx_ip_x.json";

    @Test
    void anAnswerIs200WithTheObject() {
        Object answer = List.of("x");
        KnxRestCall.Answer result = KnxRestCall.run("test", () -> answer);
        assertEquals(200, result.status());
        assertSame(answer, result.entity());
    }

    @Test
    void aCallWithoutBodyIs204() {
        KnxRestCall.Answer result = KnxRestCall.runWithoutBody("test", () -> {
        });
        assertEquals(204, result.status());
        assertNull(result.entity());
    }

    @Test
    void aFailureOfTheAddOnIsItsStatusAndErrorBody() {
        KnxRestCall.Answer result = KnxRestCall.run("test", () -> {
            throw new KnxRestException(KnxRestError.NO_PROJECT);
        });
        assertEquals(404, result.status());
        ErrorDTO error = (ErrorDTO) result.entity();
        assertNotNull(error);
        assertEquals("no-project", error.code);
        assertEquals(KnxRestError.NO_PROJECT.text(), error.text);
        assertEquals("{\"code\":\"no-project\",\"text\":\"" + KnxRestError.NO_PROJECT.text() + "\"}",
                RestFixtures.GSON.toJson(error));

        KnxRestCall.Answer noBody = KnxRestCall.runWithoutBody("test", () -> {
            throw KnxRestException.badInput("limit", "not a number from 0 on");
        });
        assertEquals(400, noBody.status());
        ErrorDTO badInput = (ErrorDTO) noBody.entity();
        assertNotNull(badInput);
        assertEquals("bad-input", badInput.code);
        assertEquals("limit: not a number from 0 on", badInput.text);
    }

    @Test
    void anyOtherExceptionIsInternalWithOneWarningOfItsClassOnly() {
        try (LogCapture log = new LogCapture()) {
            KnxRestCall.Answer result = KnxRestCall.run("bridges", () -> {
                throw new IllegalStateException("cannot open " + MADE_UP_PATH);
            });
            assertEquals(500, result.status());
            ErrorDTO error = (ErrorDTO) result.entity();
            assertNotNull(error);
            assertEquals("internal", error.code);
            assertEquals(KnxRestError.INTERNAL.text(), error.text);
            assertFalse(RestFixtures.GSON.toJson(error).contains("/made/up"));

            KnxRestCall.Answer noBody = KnxRestCall.runWithoutBody("clear", () -> {
                throw new IllegalArgumentException(MADE_UP_PATH);
            });
            assertEquals(500, noBody.status());

            List<String> warnings = log.warnings();
            assertEquals(2, warnings.size(), warnings.toString());
            assertTrue(warnings.get(0).contains("java.lang.IllegalStateException"), warnings.toString());
            for (String line : log.all()) {
                assertFalse(line.contains("/made/up"), "a log line holds the exception's message: " + line);
            }
        }
    }

    @Test
    void theTextIsCutTo200Characters() {
        KnxRestException e = KnxRestException.badInput("dst", "x".repeat(500));
        assertEquals(200, e.text().length());
        assertEquals("bad-input", e.getMessage());
    }

    @Test
    void everyCodeHasAFixedTextWithoutPathOrExceptionAndCodesAreUnique() {
        Set<String> codes = new HashSet<>();
        for (KnxRestError error : KnxRestError.values()) {
            assertTrue(codes.add(error.code()), "twice: " + error.code());
            assertTrue(error.code().matches("[a-z]+(-[a-z]+)*"), error.code());
            String text = error.text();
            assertFalse(text.isBlank());
            assertTrue(text.length() <= KnxRestException.MAX_TEXT_LENGTH);
            assertFalse(text.toLowerCase(Locale.ROOT).contains("exception"), text);
            assertFalse(text.contains("/") || text.contains("\\"), text);
            assertTrue(error.status() >= 400 && error.status() < 600, error.code());
            KnxRestCall.Answer answer = KnxRestCall.failed(new KnxRestException(error));
            assertEquals(error.status(), answer.status());
        }
    }
}
