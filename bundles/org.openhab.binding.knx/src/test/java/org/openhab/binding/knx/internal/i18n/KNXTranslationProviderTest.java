/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.i18n;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.link.KNXLinkClosedException;

/**
 *
 * @author Holger Friedrich - initial contribution
 *
 */
@NonNullByDefault
public class KNXTranslationProviderTest {
    static final String UNKNOWN = "unknown text";
    static final String UNKNOWN_PATTERN = "unknown text {0}";
    static final String UNKNOWN_FIVE = "unknown text 5";
    static final String UNKNOWN_NULL = "unknown text null";
    static final String KNOWN_DAY_KEY = "dynamic-channel.automation.weekdays";
    static final String KNOWN_DAY_VALUE = "Weekdays";
    static final String KNOWN_DAY_PATTERN_KEY = "dynamic-channel.automation.after-sunset";
    static final String KNOWN_DAY_PATTERN_VALUE = "{0} after sunset";
    static final String KNOWN_DAY_FIVE_VALUE = "5 after sunset";
    static final String KNOWN_DAY_ORIG_VALUE = "null after sunset";
    static final String KNOWN_DAY_NULL_VALUE = "null after sunset";

    @Test
    public void testGetBeforeInit() {
        // NonNull, compilation error
        // assertNull(KNXTranslationProvider.I18N.get(null));

        // initial state, should not crash and preferrably return original strings (w. pattern substitution)
        assertEquals(UNKNOWN, KNXTranslationProvider.I18N.get(UNKNOWN));
        assertEquals(UNKNOWN, KNXTranslationProvider.I18N.get(UNKNOWN, 5));
        assertEquals(UNKNOWN_NULL, KNXTranslationProvider.I18N.get(UNKNOWN_PATTERN, null, null));
        assertEquals(UNKNOWN_FIVE, KNXTranslationProvider.I18N.get(UNKNOWN_PATTERN, 5));
        // KNXTranslationProvider.I18N.get(..., null) would cause a compiler warning,
        // but using a null object of a defined type, it is possible to invoke with null value
        String s = null;
        assertEquals(UNKNOWN, KNXTranslationProvider.I18N.get(UNKNOWN, s));
        assertEquals(UNKNOWN_NULL, KNXTranslationProvider.I18N.get(UNKNOWN_PATTERN, s));
    }

    @Test
    public void testSetProvider() {
        // initial state, should not crash
        KNXTranslationProvider.I18N.setProvider(null, null);
        assertNotNull(KNXTranslationProvider.I18N.get(UNKNOWN));

        // use mockup classes with known dictionary
        KNXTranslationProvider.I18N.setProvider(new MockedLocaleProvider(), new MockedTranslationProvider());
        assertEquals(KNOWN_DAY_VALUE, KNXTranslationProvider.I18N.get(KNOWN_DAY_KEY));
        assertEquals(KNOWN_DAY_FIVE_VALUE, KNXTranslationProvider.I18N.get(KNOWN_DAY_PATTERN_KEY, 5));
        assertEquals(UNKNOWN, KNXTranslationProvider.I18N.get(UNKNOWN));
        assertEquals(UNKNOWN, KNXTranslationProvider.I18N.get(UNKNOWN, 5));
        assertEquals(UNKNOWN_NULL, KNXTranslationProvider.I18N.get(UNKNOWN_PATTERN, null, null));
        assertEquals(UNKNOWN_FIVE, KNXTranslationProvider.I18N.get(UNKNOWN_PATTERN, 5));
        // KNXTranslationProvider.I18N.get(..., null) would cause a compiler warning,
        // but using a null object of a defined type, it is possible to invoke with null value
        String s = null;
        assertEquals(UNKNOWN, KNXTranslationProvider.I18N.get(UNKNOWN, s));
        assertEquals(UNKNOWN_NULL, KNXTranslationProvider.I18N.get(UNKNOWN_PATTERN, s));

        assertEquals(KNOWN_DAY_NULL_VALUE, KNXTranslationProvider.I18N.get(KNOWN_DAY_PATTERN_KEY, s));
        assertEquals(KNOWN_DAY_PATTERN_VALUE, KNXTranslationProvider.I18N.get(KNOWN_DAY_PATTERN_KEY));

        // no locale, should work as fallback to default locale
        KNXTranslationProvider.I18N.setProvider(null, new MockedTranslationProvider());
        assertEquals(KNXTranslationProvider.I18N.get(KNOWN_DAY_KEY), KNOWN_DAY_VALUE);

        // no translations, should return initial string
        KNXTranslationProvider.I18N.setProvider(new MockedLocaleProvider(), null);
        assertEquals(KNOWN_DAY_KEY, KNXTranslationProvider.I18N.get(KNOWN_DAY_KEY));

        // initial state, dictionary should be gone
        KNXTranslationProvider.I18N.setProvider(null, null);
        assertEquals(KNOWN_DAY_KEY, KNXTranslationProvider.I18N.get(KNOWN_DAY_KEY));
    }

    @Test
    public void testGetLocalizedException() {
        // initial state, should not crash
        KNXTranslationProvider.I18N.setProvider(null, null);

        final Exception e = new KNXException("error 1");
        final Exception se = new KNXLinkClosedException("connection closed", e);
        assertNotNull(KNXTranslationProvider.I18N.getLocalizedException(e));
        assertNotNull(KNXTranslationProvider.I18N.getLocalizedException(se));
        assertEquals("KNXException, error 1", KNXTranslationProvider.I18N.getLocalizedException(e));

        // use mockup classes with known dictionary
        KNXTranslationProvider.I18N.setProvider(new MockedLocaleProvider(), new MockedTranslationProvider());
        // exception which is not part off the dictionary
        assertEquals("KNXLinkClosedException, connection closed",
                KNXTranslationProvider.I18N.getLocalizedException(se));
        // exception which can be translated
        assertEquals("Translated KNX Exception (KNXException, error 1)",
                KNXTranslationProvider.I18N.getLocalizedException(e));
    }
}
