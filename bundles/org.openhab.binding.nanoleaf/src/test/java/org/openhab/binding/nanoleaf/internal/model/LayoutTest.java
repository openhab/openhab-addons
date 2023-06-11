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
package org.openhab.binding.nanoleaf.internal.model;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

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
public class LayoutTest {

    @Nullable
    private Layout lo1;

    @Nullable
    private Layout lo2; // Different from l1

    @Nullable
    private Layout lo3; // Same as l1

    @BeforeEach
    public void setUp() {
        PositionDatum pd1 = new PositionDatum(100, 200, 270, 123, 12);
        PositionDatum pd2 = new PositionDatum(100, 220, 240, 123, 2);
        PositionDatum pd3 = new PositionDatum(100, 200, 270, 123, 12);

        lo1 = new Layout(Arrays.asList(pd1, pd3));
        lo2 = new Layout(Arrays.asList(pd1, pd2));
        lo3 = new Layout(Arrays.asList(pd1, pd3));
    }

    @Test
    public void testHashCode() {
        Layout l1 = lo1;
        Layout l2 = lo2;
        Layout l3 = lo3;
        if (l1 != null && l2 != null && l3 != null) {
            assertThat(l1.hashCode(), is(equalTo(l3.hashCode())));
            assertThat(l2.hashCode(), is(not(equalTo(l3.hashCode()))));
        } else {
            assertThat("Should be initialized", false);
        }
    }

    @Test
    public void testEquals() {
        assertThat(lo1, is(equalTo(lo3)));
        assertThat(lo2, is(not(equalTo(lo3))));
        assertThat(lo3, is(not(equalTo(null))));
    }
}
