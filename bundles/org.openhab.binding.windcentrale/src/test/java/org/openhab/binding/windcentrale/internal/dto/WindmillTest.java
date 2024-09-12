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
package org.openhab.binding.windcentrale.internal.dto;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link Windmill} enum.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class WindmillTest {

    @Test
    public void fromName() {
        assertThat(Windmill.fromName("Unknown Windmill"), nullValue());
        assertThat(Windmill.fromName("De Grote Geert"), is(Windmill.DE_GROTE_GEERT));

        for (Windmill windmill : Windmill.values()) {
            assertThat(Windmill.fromName(windmill.getName()), is(windmill));
        }
    }

    @Test
    public void fromProjectCode() {
        assertThat(Windmill.fromProjectCode("WND-UNKNOWN"), nullValue());
        assertThat(Windmill.fromProjectCode("WND-GG"), is(Windmill.DE_GROTE_GEERT));

        for (Windmill windmill : Windmill.values()) {
            assertThat(Windmill.fromProjectCode(windmill.getProjectCode()), is(windmill));
        }
    }

    @Test
    public void namesAreUnique() {
        int count = (int) Arrays.stream(Windmill.values()) //
                .map(Windmill::getName) //
                .distinct() //
                .count();

        assertThat(count, is(Windmill.values().length));
    }

    @Test
    public void projectCodesAreUnique() {
        int count = (int) Arrays.stream(Windmill.values()) //
                .map(Windmill::getProjectCode) //
                .distinct() //
                .count();

        assertThat(count, is(Windmill.values().length));
    }
}
