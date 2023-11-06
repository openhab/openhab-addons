/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice.language;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class CombiningLanguageProviderTest {
    private static final Optional<String> PRIORITIZED_LANGUAGE = Optional.of("de");
    private static final Optional<String> FALLBACK_LANGUAGE = Optional.of("en");

    private static final LanguageProvider PRIORITIZED_PROVIDER = new LanguageProvider() {
        @Override
        public Optional<String> getLanguage() {
            return PRIORITIZED_LANGUAGE;
        }
    };

    private static final LanguageProvider FALLBACK_PROVIDER = new LanguageProvider() {
        @Override
        public Optional<String> getLanguage() {
            return FALLBACK_LANGUAGE;
        }
    };

    private static final LanguageProvider NULL_PROVIDER = new LanguageProvider() {
        @Override
        public Optional<String> getLanguage() {
            return Optional.empty();
        }
    };

    @Test
    public void testPrioritizedLanguageProviderIsUsed() {
        // given:
        LanguageProvider provider = new CombiningLanguageProvider(PRIORITIZED_PROVIDER, FALLBACK_PROVIDER);

        // when:
        Optional<String> language = provider.getLanguage();

        // then:
        assertEquals(PRIORITIZED_LANGUAGE, language);
    }

    @Test
    public void testFallbackProviderIsUsedWhenPrioritizedProviderIsNull() {
        // given:
        LanguageProvider provider = new CombiningLanguageProvider(null, FALLBACK_PROVIDER);

        // when:
        Optional<String> language = provider.getLanguage();

        // then:
        assertEquals(FALLBACK_LANGUAGE, language);
    }

    @Test
    public void testFallbackProviderIsUsedWhenPrioritizedProviderProvidesNull() {
        // given:
        LanguageProvider provider = new CombiningLanguageProvider(NULL_PROVIDER, FALLBACK_PROVIDER);

        // when:
        Optional<String> language = provider.getLanguage();

        // then:
        assertEquals(FALLBACK_LANGUAGE, language);
    }

    @Test
    public void testProvidesNullWhenBothProvidersAreNull() {
        // given:
        LanguageProvider provider = new CombiningLanguageProvider(null, null);

        // when:
        Optional<String> language = provider.getLanguage();

        // then:
        assertFalse(language.isPresent());
    }
}
