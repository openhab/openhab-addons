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
package org.openhab.binding.mielecloud.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class LocaleValidatorTest {
    @Test
    public void enIsAValidLanguage() {
        // given:
        String language = "en";

        // when:
        boolean valid = LocaleValidator.isValidLanguage(language);

        // then:
        assertTrue(valid);
    }

    @Test
    public void deIsAValidLanguage() {
        // given:
        String language = "de";

        // when:
        boolean valid = LocaleValidator.isValidLanguage(language);

        // then:
        assertTrue(valid);
    }

    @Test
    public void aFullLocaleIsNotAValidLanguage() {
        // given:
        String language = "en_us";

        // when:
        boolean valid = LocaleValidator.isValidLanguage(language);

        // then:
        assertFalse(valid);
    }

    @Test
    public void textIsNotAValidLanguage() {
        // given:
        String language = "Hello World!";

        // when:
        boolean valid = LocaleValidator.isValidLanguage(language);

        // then:
        assertFalse(valid);
    }
}
