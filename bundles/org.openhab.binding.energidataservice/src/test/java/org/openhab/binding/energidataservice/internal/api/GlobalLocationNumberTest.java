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
package org.openhab.binding.energidataservice.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link GlobalLocationNumber}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class GlobalLocationNumberTest {

    @Test
    void isValid() {
        assertThat(GlobalLocationNumber.of("5790000682102").isValid(), is(true));
    }

    @Test
    void isInvalid() {
        assertThat(GlobalLocationNumber.of("5790000682103").isValid(), is(false));
    }

    @Test
    void emptyIsInvalid() {
        assertThat(GlobalLocationNumber.EMPTY.isValid(), is(false));
    }
}
