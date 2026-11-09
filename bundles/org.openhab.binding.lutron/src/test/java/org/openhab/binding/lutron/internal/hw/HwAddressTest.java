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
package org.openhab.binding.lutron.internal.hw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link HwAddress}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class HwAddressTest {
    @Test
    public void allDocumentedFormatsNormalizeToTheProcessorForm() {
        for (String address : List.of("[01:01:03:02:04]", "1:1:3:2:4", "1.1.3.2.4", "1/1/3/2/4", "1\\1\\3\\2\\4",
                "1-1-3-2-4", "[proc 1 : link 1 : mi 3 : module 2 : zone 4]")) {
            assertEquals("[01:01:03:02:04]", HwAddress.normalize(address), address);
        }
    }

    @Test
    public void fewerFieldsAreNormalizedToo() {
        assertEquals("[01:08:01:54]", HwAddress.normalize("1:8:1:54"));
        assertEquals("[01:05:25]", HwAddress.normalize("[1:5:25]"));
    }

    @Test
    @SuppressWarnings("PMD.SetDefaultLocale") // The regression only shows under a default locale with non-ASCII digits
    public void normalizedAddressUsesAsciiDigitsInEveryLocale() {
        Locale defaultLocale = Locale.getDefault();
        try {
            // Arabic (Egypt) formats numbers with Arabic-Indic digits by default
            Locale.setDefault(Locale.forLanguageTag("ar-EG"));
            assertEquals("[01:01:03:02:04]", HwAddress.normalize("1:1:3:2:4"));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    public void inputThatIsNotAnAddressIsReturnedUnchanged() {
        for (String text : List.of("", "abc", "[01:02]", "1:2:3:4:5:6", "1:2:3:99999")) {
            assertEquals(text, HwAddress.normalize(text));
        }
    }

    @Test
    public void onlyFourFieldAddressesOnConfigurableLinksAboveEightAreShades() {
        assertTrue(HwAddress.isQedShade("[01:04:02:12]"));
        assertTrue(HwAddress.isQedShade("1.6.1.96"));
        assertFalse(HwAddress.isQedShade("[01:04:02:08]"));
        assertFalse(HwAddress.isQedShade("[01:08:01:54]"));
        assertFalse(HwAddress.isQedShade("[01:01:04:01:01]"));
        assertFalse(HwAddress.isQedShade("[01:05:25]"));
    }
}
