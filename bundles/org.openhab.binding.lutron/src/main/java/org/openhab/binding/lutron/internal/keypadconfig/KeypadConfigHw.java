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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;

/**
 * Keypad configuration definition for legacy HomeWorks keypads, which are controlled with the L232 command set.
 *
 * L232 numbers the buttons of every keypad 1 to 24, and gives each button the LED with the same number. The button
 * numbers of the models come from the Keypad Button Numbering diagrams in the HomeWorks Illumination 1.65 help. A
 * model has an LED for each button that its diagram shows with one. The European-style diagrams show no LEDs,
 * although these keypads have status LEDs, so those models have an LED for each button except the raise/lower pair.
 * seeTouch keypads are not in those diagrams; their models use the numbering reported by a processor.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public final class KeypadConfigHw extends KeypadConfig {
    /** A keypad with all 24 buttons and LEDs that L232 can address. */
    public static final String GENERIC = "GENERIC";

    public static final int MAX_BUTTONS = 24;

    private static final int LED_ID_OFFSET = 100;

    private static final List<Integer> BUTTONS_23_24 = List.of(23, 24);
    private static final List<Integer> LARGE_BUTTONS_6 = List.of(1, 3, 5, 6, 8, 10);
    private static final List<Integer> LARGE_BUTTONS_9 = List.of(1, 3, 5, 6, 8, 10, 11, 13, 15);

    private record Component(int id, String channel, String description,
            ComponentType type) implements KeypadComponent {
    }

    public static String buttonChannel(int button) {
        return "button" + button;
    }

    public static String ledChannel(int led) {
        return "led" + led;
    }

    /** Returns the component id of a button, which is its L232 button number. */
    public static int buttonId(int button) {
        return button;
    }

    /** Returns the component id of the LED belonging to a button. */
    public static int ledId(int button) {
        return LED_ID_OFFSET + button;
    }

    public KeypadConfigHw() {
        List<Integer> all = range(1, MAX_BUTTONS);
        modelData.put(GENERIC, components(all, all));

        // Slim button keypads and Architrave keypads: scene buttons with LEDs, and master buttons 23 and 24
        for (String model : List.of("HWI-KP5", "HWI-KP5-DN", "HWI-KP5-DW")) {
            modelData.put(model, components(plus(range(1, 5), BUTTONS_23_24), range(1, 5)));
        }
        modelData.put("HWI-KP10", components(plus(range(1, 10), BUTTONS_23_24), range(1, 10)));
        modelData.put("HWI-KP15", components(plus(range(1, 15), BUTTONS_23_24), range(1, 15)));

        // Large button keypads
        modelData.put("HWI-KP-LB6", components(LARGE_BUTTONS_6, LARGE_BUTTONS_6));
        modelData.put("HWI-KP-LB9", components(LARGE_BUTTONS_9, LARGE_BUTTONS_9));

        // 2-button keypad, with an LED on each button
        modelData.put("HWI-2B", components(BUTTONS_23_24, BUTTONS_23_24));

        // European-style keypads: scene buttons, an off button and a raise/lower pair
        modelData.put("HWI-2SE", components(range(3, 4), range(3, 4)));
        for (String model : List.of("HWI-4SE-M", "HWI-4SE-IR")) {
            modelData.put(model, components(plus(range(1, 5), BUTTONS_23_24), range(1, 5)));
        }
        for (String model : List.of("HWI-8SE-M", "HWI-8SE-IR")) {
            modelData.put(model, components(plus(range(1, 9), BUTTONS_23_24), range(1, 9)));
        }

        // 5-scene keypads, with a raise/lower pair (M), an IR receiver (IR) or neither (NM)
        modelData.put("HWI-5S-M", components(plus(range(1, 5), BUTTONS_23_24), range(1, 5)));
        for (String model : List.of("HWI-5S-IR", "HWI-5S-NM")) {
            modelData.put(model, components(range(1, 5), range(1, 5)));
        }

        // seeTouch keypads report buttons by position, with 23 for lower and 24 for raise. The 4-scene keypad has
        // scene buttons 1 to 4 and button 6, each with an LED, and the lower/raise pair.
        List<Integer> seeTouch4Scene = List.of(1, 2, 3, 4, 6);
        modelData.put("ST-4S", components(plus(seeTouch4Scene, BUTTONS_23_24), seeTouch4Scene));
    }

    private static List<Integer> range(int first, int last) {
        return IntStream.rangeClosed(first, last).boxed().toList();
    }

    private static List<Integer> plus(List<Integer> buttons, List<Integer> more) {
        List<Integer> combined = new ArrayList<>(buttons);
        combined.addAll(more);
        return combined;
    }

    private static List<KeypadComponent> components(List<Integer> buttons, List<Integer> leds) {
        List<KeypadComponent> buttonList = new ArrayList<>();
        for (int n : buttons) {
            buttonList.add(new Component(buttonId(n), buttonChannel(n), "Button " + n, ComponentType.BUTTON));
        }
        List<KeypadComponent> ledList = new ArrayList<>();
        for (int n : leds) {
            ledList.add(new Component(ledId(n), ledChannel(n), "LED " + n, ComponentType.LED));
        }
        return combinedList(buttonList, ledList);
    }

    @Override
    public boolean isButton(int id) {
        return id >= 1 && id <= MAX_BUTTONS;
    }

    @Override
    public boolean isLed(int id) {
        return id > LED_ID_OFFSET && id <= LED_ID_OFFSET + MAX_BUTTONS;
    }

    @Override
    public boolean isCCI(int id) {
        return false;
    }
}
