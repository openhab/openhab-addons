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
package org.openhab.binding.siemenshvac.internal.metadata;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests matching language options in gateway HTML without nested repetition.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class SiemensHvacMetadataRegistryImplTest {

    @ParameterizedTest
    @ValueSource(strings = { "", "\n", "\r\n" })
    void capturesOnlyTheLanguageSelectOptions(String newline) {
        String options = newline + "<option value=\"1\" selected>English</option>" + newline
                + "<option value=\"2\">Deutsch</option>" + newline;
        String html = "<select name=\"language\" class=\"selector\">" + options
                + "</select><select name=\"other\"><option>Other</option></select>";
        Matcher matcher = SiemensHvacMetadataRegistryImpl.LANGUAGE_SELECT_PATTERN.matcher(html);

        assertTrue(matcher.find());
        assertEquals(options, matcher.group(1));
        assertFalse(matcher.find());
    }

    @Test
    void rejectsMissingClosingSelectAfterManyNewlines() throws Exception {
        String html = "<select name=\"language\">" + "\n".repeat(20000);

        assertFalse(bounded(() -> SiemensHvacMetadataRegistryImpl.LANGUAGE_SELECT_PATTERN.matcher(html).find()));
    }

    private static <T> T bounded(Callable<T> operation) throws Exception {
        FutureTask<T> task = new FutureTask<>(operation);
        Thread.ofVirtual().start(task);
        try {
            return task.get(10, TimeUnit.SECONDS);
        } finally {
            task.cancel(true);
        }
    }
}
