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
package org.openhab.binding.mielecloud.internal.webservice.language;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Locale;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.i18n.LocaleProvider;

/**
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class OpenHabLanguageProviderTest {
    @Test
    public void whenTheLocaleIsSetToEnglishThenTheLanguageCodeIsEn() {
        // given:
        LocaleProvider localeProvider = mock(LocaleProvider.class);
        when(localeProvider.getLocale()).thenReturn(Locale.ENGLISH);

        LanguageProvider languageProvider = new OpenHabLanguageProvider(localeProvider);

        // when:
        Optional<String> language = languageProvider.getLanguage();

        // then:
        assertEquals(Optional.of("en"), language);
    }

    @Test
    public void whenTheLocaleIsSetToGermanThenTheLanguageCodeIsDe() {
        // given:
        LocaleProvider localeProvider = mock(LocaleProvider.class);
        when(localeProvider.getLocale()).thenReturn(Locale.GERMAN);

        LanguageProvider languageProvider = new OpenHabLanguageProvider(localeProvider);

        // when:
        Optional<String> language = languageProvider.getLanguage();

        // then:
        assertEquals(Optional.of("de"), language);
    }

    @Test
    public void whenTheLocaleIsSetToGermanyThenTheLanguageCodeIsDe() {
        // given:
        LocaleProvider localeProvider = mock(LocaleProvider.class);
        when(localeProvider.getLocale()).thenReturn(Locale.GERMANY);

        LanguageProvider languageProvider = new OpenHabLanguageProvider(localeProvider);

        // when:
        Optional<String> language = languageProvider.getLanguage();

        // then:
        assertEquals(Optional.of("de"), language);
    }
}
