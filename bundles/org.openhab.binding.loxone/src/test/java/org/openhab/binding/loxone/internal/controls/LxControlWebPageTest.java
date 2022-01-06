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
package org.openhab.binding.loxone.internal.controls;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.StringType;

/**
 * Test class for (@link LxControlWebPage}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlWebPageTest extends LxControlTest {
    @BeforeEach
    public void setup() {
        setupControl("132d2791-00f8-d532-ffff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Webpage 1");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlWebPage.class, 1, 0, 2, 2, 0);
    }

    @Test
    public void testChannels() {
        testChannel("String", " / URL", null, null, null, null, true, null);
        testChannel("String", " / URL HD", null, null, null, null, true, null);
    }

    @Test
    public void testChannelStates() {
        testChannelState(" / URL", new StringType("http://low.res"));
        testChannelState(" / URL HD", new StringType("http://hi.res"));
    }
}
