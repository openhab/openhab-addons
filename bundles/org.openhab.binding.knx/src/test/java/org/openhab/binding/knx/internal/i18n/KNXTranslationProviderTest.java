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
class KNXTranslationProviderTest {
    static final String UNKNOWN = "unknown text";
    static final String UNKNOWN_PATTERN = "unknown text {0}";
    static final String UNKNOWN_FIVE = "unknown text 5";
    static final String UNKNOWN_NULL = "unknown text null";
    static final String KNX_BINDING_KEY = "binding.knx.name";
    static final String KNX_BINDING_VALUE = "KNX Binding";
    static final String CONN_TYPE_PATTERN_KEY = "error.knx-unknown-ip-connection-type";
    static final String CONN_TYPE_PATTERN_VALUE = "Unknown IP connection type: {0}.";
    static final String CONN_TYPE_FIVE_VALUE = "Unknown IP connection type: 5.";
    static final String CONN_TYPE_NULL_VALUE = "Unknown IP connection type: null.";

    @Test
    void testGetBeforeInit() {
        // initial state, should not crash and preferably return original strings (w. pattern substitution)
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
    void testSetProvider() {
        // initial state, should not crash
        KNXTranslationProvider.I18N.setProvider(null, null);
        assertNotNull(KNXTranslationProvider.I18N.get(UNKNOWN));

        // use mockup classes with known dictionary
        KNXTranslationProvider.I18N.setProvider(new MockedLocaleProvider(), new MockedTranslationProvider());
        assertEquals(KNX_BINDING_VALUE, KNXTranslationProvider.I18N.get(KNX_BINDING_KEY));
        assertEquals(CONN_TYPE_FIVE_VALUE, KNXTranslationProvider.I18N.get(CONN_TYPE_PATTERN_KEY, 5));
        assertEquals(UNKNOWN, KNXTranslationProvider.I18N.get(UNKNOWN));
        assertEquals(UNKNOWN, KNXTranslationProvider.I18N.get(UNKNOWN, 5));
        assertEquals(UNKNOWN_NULL, KNXTranslationProvider.I18N.get(UNKNOWN_PATTERN, null, null));
        assertEquals(UNKNOWN_FIVE, KNXTranslationProvider.I18N.get(UNKNOWN_PATTERN, 5));
        // KNXTranslationProvider.I18N.get(..., null) would cause a compiler warning,
        // but using a null object of a defined type, it is possible to invoke with null value
        String s = null;
        assertEquals(UNKNOWN, KNXTranslationProvider.I18N.get(UNKNOWN, s));
        assertEquals(UNKNOWN_NULL, KNXTranslationProvider.I18N.get(UNKNOWN_PATTERN, s));

        assertEquals(CONN_TYPE_NULL_VALUE, KNXTranslationProvider.I18N.get(CONN_TYPE_PATTERN_KEY, s));
        assertEquals(CONN_TYPE_PATTERN_VALUE, KNXTranslationProvider.I18N.get(CONN_TYPE_PATTERN_KEY));

        // no locale, should work as fallback to default locale
        KNXTranslationProvider.I18N.setProvider(null, new MockedTranslationProvider());
        assertEquals(KNXTranslationProvider.I18N.get(KNX_BINDING_KEY), KNX_BINDING_VALUE);

        // no translations, should return initial string
        KNXTranslationProvider.I18N.setProvider(new MockedLocaleProvider(), null);
        assertEquals(KNX_BINDING_KEY, KNXTranslationProvider.I18N.get(KNX_BINDING_KEY));

        // initial state, dictionary should be gone
        KNXTranslationProvider.I18N.setProvider(null, null);
        assertEquals(KNX_BINDING_KEY, KNXTranslationProvider.I18N.get(KNX_BINDING_KEY));
    }

    @Test
    void testGetLocalizedException() {
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
