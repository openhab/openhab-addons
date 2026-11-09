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
    public void modelsHaveTheButtonsAndLedsOfTheirNumberingDiagram() {
        List<Integer> fiveAndMaster = List.of(1, 2, 3, 4, 5, 23, 24);
        List<Integer> five = List.of(1, 2, 3, 4, 5);
        for (String model : List.of("HWI-KP5", "HWI-KP5-DN", "HWI-KP5-DW", "HWI-4SE-M", "HWI-4SE-IR", "HWI-5S-M")) {
            assertModel(model, fiveAndMaster, five);
        }
        assertModel("HWI-KP10", List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 23, 24), List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        assertModel("HWI-KP15", List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 23, 24),
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15));
        for (String model : List.of("HWI-8SE-M", "HWI-8SE-IR")) {
            assertModel(model, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 23, 24), List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
        }
        assertModel("HWI-KP-LB6", List.of(1, 3, 5, 6, 8, 10), List.of(1, 3, 5, 6, 8, 10));
        assertModel("HWI-KP-LB9", List.of(1, 3, 5, 6, 8, 10, 11, 13, 15), List.of(1, 3, 5, 6, 8, 10, 11, 13, 15));
        assertModel("HWI-2B", List.of(23, 24), List.of(23, 24));
        assertModel("HWI-2SE", List.of(3, 4), List.of(3, 4));
        for (String model : List.of("HWI-5S-IR", "HWI-5S-NM")) {
            assertModel(model, five, five);
        }
    }

    private void assertModel(String model, List<Integer> buttons, List<Integer> leds) {
        List<KeypadComponent> components = config.getComponents(model);
        assertEquals(buttons.stream().map(n -> "button" + n).toList(), components.stream()
                .filter(c -> c.type() == ComponentType.BUTTON).map(KeypadComponent::channel).toList(), model);
        assertEquals(leds.stream().map(n -> "led" + n).toList(),
                components.stream().filter(c -> c.type() == ComponentType.LED).map(KeypadComponent::channel).toList(),
                model);
        assertTrue(
                components.stream().allMatch(c -> c.type() == ComponentType.BUTTON
                        ? c.id() == KeypadConfigHw.buttonId(Integer.parseInt(c.channel().substring("button".length())))
                        : c.id() == KeypadConfigHw.ledId(Integer.parseInt(c.channel().substring("led".length())))),
                model);
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
