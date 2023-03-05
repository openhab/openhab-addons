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
public class GlobalOrientationTest {

    @Nullable
    GlobalOrientation go1;

    @Nullable
    GlobalOrientation go2; // Different from go1

    @Nullable
    GlobalOrientation go3; // Same as go1

    @BeforeEach
    public void setUp() {
        go1 = new GlobalOrientation(0, 360, 180);
        go2 = new GlobalOrientation(0, 360, 267);
        go3 = new GlobalOrientation(0, 360, 180);
    }

    @Test
    public void testHashCode() {
        GlobalOrientation g1 = go1;
        GlobalOrientation g2 = go2;
        GlobalOrientation g3 = go3;
        if (g1 != null && g2 != null && g3 != null) {
            assertThat(g1.hashCode(), is(equalTo(g3.hashCode())));
            assertThat(g2.hashCode(), is(not(equalTo(g3.hashCode()))));
        } else {
            assertThat("Should be initialized", false);
        }
    }

    @Test
    public void testEquals() {
        assertThat(go1, is(equalTo(go3)));
        assertThat(go2, is(not(equalTo(go3))));
        assertThat(go3, is(not(equalTo(null))));
    }
}
