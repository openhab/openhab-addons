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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class ErrorMessageTest {

    @Test
    public void testErrorMessageCanBeCreated() {
        // given:
        String json = "{\"message\": \"Unauthorized\"}";

        // when:
        ErrorMessage errorMessage = ErrorMessage.fromJson(json);

        // then:
        assertEquals("Unauthorized", errorMessage.getMessage().get());
    }

    @Test
    public void testErrorMessageCreationThrowsMieleSyntaxExceptionWhenJsonIsInvalid() {
        // given:
        String json = "\"message\": \"Unauthorized}";

        // when:
        assertThrows(MieleSyntaxException.class, () -> {
            ErrorMessage.fromJson(json);
        });
    }
}
