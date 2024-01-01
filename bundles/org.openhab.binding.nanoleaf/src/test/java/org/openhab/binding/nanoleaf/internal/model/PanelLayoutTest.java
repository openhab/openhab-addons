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
public class PanelLayoutTest {

    @Nullable
    private PanelLayout pl1;

    @Nullable
    private PanelLayout pl2; // Different from pl1

    @Nullable
    private PanelLayout pl3; // Equal to pl1

    @BeforeEach
    public void setUp() {
        PositionDatum pd1 = new PositionDatum(100, 200, 270, 123, 12);
        PositionDatum pd2 = new PositionDatum(100, 220, 240, 123, 2);
        PositionDatum pd3 = new PositionDatum(100, 200, 270, 123, 12);

        Layout l1 = new Layout(Arrays.asList(pd1, pd3));
        Layout l2 = new Layout(Arrays.asList(pd1, pd2));
        Layout l3 = new Layout(Arrays.asList(pd1, pd3));

        GlobalOrientation go1 = new GlobalOrientation(0, 360, 180);

        pl1 = new PanelLayout(go1, l1);
        pl2 = new PanelLayout(go1, l2);
        pl3 = new PanelLayout(go1, l3);
    }

    @Test
    public void testHashCode() {
        PanelLayout p1 = pl1;
        PanelLayout p2 = pl2;
        PanelLayout p3 = pl3;
        if (p1 != null && p2 != null && p3 != null) {
            assertThat(p1.hashCode(), is(equalTo(p3.hashCode())));
            assertThat(p2.hashCode(), is(not(equalTo(p3.hashCode()))));
        } else {
            assertThat("Should be initialized", false);
        }
    }

    @Test
    public void testEquals() {
        assertThat(pl1, is(equalTo(pl3)));
        assertThat(pl2, is(not(equalTo(pl3))));
        assertThat(pl3, is(not(equalTo(null))));
    }
}
