/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.keypadconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;

/**
 * Tests for {@link KeypadConfigHw}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class KeypadConfigHwTest {
    private final KeypadConfigHw config = new KeypadConfigHw();

    @Test
    public void genericModelHasButtonsAndLedsOneToTwentyFour() {
        List<KeypadComponent> components = config.getComponents(KeypadConfigHw.GENERIC);

        assertEquals(48, components.size());
        for (int n = 1; n <= 24; n++) {
            int button = n;
            assertTrue(components.stream().anyMatch(c -> c.type() == ComponentType.BUTTON && c.id() == button
                    && c.channel().equals("button" + button)));
            assertTrue(components.stream().anyMatch(c -> c.type() == ComponentType.LED
                    && c.id() == KeypadConfigHw.ledId(button) && c.channel().equals("led" + button)));
        }
    }

    @Test
    public void buttonAndLedIdsDoNotOverlap() {
        assertTrue(config.isButton(1));
        assertTrue(config.isButton(24));
        assertFalse(config.isButton(0));
        assertFalse(config.isButton(25));
        assertTrue(config.isLed(KeypadConfigHw.ledId(1)));
        assertTrue(config.isLed(KeypadConfigHw.ledId(24)));
        assertFalse(config.isLed(24));
        assertFalse(config.isLed(KeypadConfigHw.ledId(25)));
    }

    @Test
    public void unknownModelHasNoComponents() {
        assertTrue(config.getComponents("NOSUCHMODEL").isEmpty());
    }
}
