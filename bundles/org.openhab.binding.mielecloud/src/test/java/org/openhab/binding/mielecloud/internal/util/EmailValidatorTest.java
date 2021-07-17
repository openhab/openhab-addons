/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class EmailValidatorTest {
    @ParameterizedTest
    @ValueSource(strings = { "example@openhab.org", "itsme@test24.com", "my-account@t-online.de", "Some@dDRESs.edu",
            "min@Length.com" })
    void validEmailAddress(String emailAddress) {
        // when:
        var valid = EmailValidator.isValid(emailAddress);

        // then:
        assertTrue(valid);
    }

    @ParameterizedTest
    @ValueSource(strings = { "examp!e@###.org", "to@o.short.com" })
    void invalidEmailAddress(String emailAddress) {
        // when:
        var valid = EmailValidator.isValid(emailAddress);

        // then:
        assertFalse(valid);
    }
}
