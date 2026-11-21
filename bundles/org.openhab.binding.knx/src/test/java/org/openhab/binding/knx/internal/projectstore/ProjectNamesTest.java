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
package org.openhab.binding.knx.internal.projectstore;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Checks how a text from the project is made safe for one console line.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ProjectNamesTest {

    private static String of(int... chars) {
        StringBuilder text = new StringBuilder();
        for (int c : chars) {
            text.append((char) c);
        }
        return text.toString();
    }

    @Test
    void controlCharactersBecomeOneSpace() {
        assertEquals("a b c d", ProjectNames.clean("a\tb\nc\r\nd"));
        assertEquals("red [31m text", ProjectNames.clean("red " + of(0x1b) + "[31m text"));
        assertEquals("a b", ProjectNames.clean("a" + of(0x00, 0x7f, 0x85, 0x9b) + "b"));
    }

    @Test
    void lineAndParagraphSeparatorsBecomeASpace() {
        assertEquals("one two three", ProjectNames.clean("one" + of(0x2028) + "two" + of(0x2029) + "three"));
    }

    @Test
    void formatCharactersBecomeASpace() {
        assertEquals("abc evil", ProjectNames.clean("abc" + of(0x202e) + "evil"));
        assertEquals("zero width", ProjectNames.clean("zero" + of(0x200b) + "width"));
        assertEquals("a b", ProjectNames.clean("a" + of(0xfeff) + "b"));
    }

    @Test
    void runsOfSpacesBecomeOneAndTheEndsAreTrimmed() {
        assertEquals("Made-up light", ProjectNames.clean("  Made-up \t\n  light  "));
        assertEquals("", ProjectNames.clean(" \t\n "));
        assertEquals("", ProjectNames.clean(""));
    }

    @Test
    void aGivenLengthReplacesTheCutOf60() {
        String hundred = "0123456789".repeat(10);

        assertEquals(hundred, ProjectNames.clean(hundred, 100));
        assertEquals("0123456789", ProjectNames.clean(hundred, 10));
        assertEquals("a b", ProjectNames.clean("a" + of(0x1b) + "b", 100));
    }

    @Test
    void aLongTextIsCutTo60Characters() {
        String hundred = "0123456789".repeat(10);

        assertEquals(hundred.substring(0, 60), ProjectNames.clean(hundred));
        assertEquals("x".repeat(60), ProjectNames.clean("x".repeat(60)));
    }

    @Test
    void theCutDoesNotSplitASurrogatePair() {
        String smiley = of(0xd83d, 0xde00);
        String text = "x".repeat(59) + smiley + "tail";

        String cleaned = ProjectNames.clean(text);

        assertEquals("x".repeat(59), cleaned);
        assertEquals("x".repeat(58) + smiley, ProjectNames.clean("x".repeat(58) + smiley + "tail"));
    }

    @Test
    void otherTextStaysAsItIs() {
        String text = "Küche – Licht " + of(0xd83d, 0xde00) + " 21/7";
        assertEquals(text, ProjectNames.clean(text));
    }
}
