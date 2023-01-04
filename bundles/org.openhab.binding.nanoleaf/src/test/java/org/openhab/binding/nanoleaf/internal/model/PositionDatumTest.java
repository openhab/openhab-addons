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
package org.openhab.binding.nanoleaf.internal.model;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for global orientation
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class PositionDatumTest {

    @Nullable
    private PositionDatum pd1;

    @Nullable
    private PositionDatum pd2; // different from pd1

    @Nullable
    private PositionDatum pd3; // same as pd1

    @BeforeEach
    public void setUp() {
        pd1 = new PositionDatum(100, 200, 270, 123, 12);
        pd2 = new PositionDatum(100, 220, 240, 123, 2);
        pd3 = new PositionDatum(100, 200, 270, 123, 12);
    }

    @Test
    public void testHashCode() {
        PositionDatum p1 = pd1;
        PositionDatum p2 = pd2;
        PositionDatum p3 = pd3;
        if (p1 != null && p2 != null && p3 != null) {
            assertThat(p1.hashCode(), is(equalTo(p3.hashCode())));
            assertThat(p2.hashCode(), is(not(equalTo(p3.hashCode()))));
        } else {
            assertThat("Should be initialized", false);
        }
    }

    @Test
    public void testEquals() {
        assertThat(pd1, is(equalTo(pd3)));
        assertThat(pd2, is(not(equalTo(pd3))));
        assertThat(pd3, is(not(equalTo(null))));
    }
}
