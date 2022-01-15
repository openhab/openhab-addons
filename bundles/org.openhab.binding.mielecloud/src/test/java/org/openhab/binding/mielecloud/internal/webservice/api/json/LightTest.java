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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class LightTest {
    @Test
    public void testFromNullId() {
        // when:
        Light light = Light.fromId(null);

        // then:
        assertEquals(Light.UNKNOWN, light);
    }

    @Test
    public void testFromNotSupportedId() {
        // when:
        Light light = Light.fromId(0);

        // then:
        assertEquals(Light.NOT_SUPPORTED, light);
    }

    @Test
    public void testFromNotSupportedAlternativeId() {
        // when:
        Light light = Light.fromId(255);

        // then:
        assertEquals(Light.NOT_SUPPORTED, light);
    }

    @Test
    public void testFromEnabledId() {
        // when:
        Light light = Light.fromId(1);

        // then:
        assertEquals(Light.ENABLE, light);
    }

    @Test
    public void testFromDisabledId() {
        // when:
        Light light = Light.fromId(2);

        // then:
        assertEquals(Light.DISABLE, light);
    }
}
