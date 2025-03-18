/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.grundfosalpha.internal.protocol;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.util.HexUtils;

/**
 * Tests for {@link MessageType}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class MessageTypeTest {
    @Test
    void requestFlowHead() {
        String expected = "27 07 E7 F8 0A 03 5D 01 21 52 1F";
        assertThat(HexUtils.bytesToHex(MessageType.FlowHead.request(), " "), is(expected));
    }

    @Test
    void requestPower() {
        String expected = "27 07 E7 F8 0A 03 57 00 45 8A CD";
        assertThat(HexUtils.bytesToHex(MessageType.Power.request(), " "), is(expected));
    }
}
