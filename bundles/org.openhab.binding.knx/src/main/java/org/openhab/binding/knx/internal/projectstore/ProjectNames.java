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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Makes a text from an ETS project safe to show on one line.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class ProjectNames {

    static final int MAX_LENGTH = 60;

    private ProjectNames() {
    }

    /**
     * {@return the text with control, line separator, paragraph separator and format characters (such as a bidi
     * override) as spaces, runs of spaces as one, the ends trimmed, and at most {@value #MAX_LENGTH} characters,
     * never half of a surrogate pair}
     */
    public static String clean(String text) {
        return clean(text, MAX_LENGTH);
    }

    /**
     * {@return the text as {@link #clean(String)} makes it, but cut at {@code maxLength} characters instead of
     * {@value #MAX_LENGTH}}
     */
    public static String clean(String text, int maxLength) {
        StringBuilder cleaned = new StringBuilder(Math.min(text.length(), 2 * maxLength));
        boolean space = true;
        int i = 0;
        while (i < text.length() && cleaned.length() <= maxLength) {
            int c = text.codePointAt(i);
            i += Character.charCount(c);
            if (c == ' ' || unsafe(c)) {
                if (!space) {
                    cleaned.append(' ');
                    space = true;
                }
            } else {
                cleaned.appendCodePoint(c);
                space = false;
            }
        }
        int end = Math.min(cleaned.length(), maxLength);
        if (end < cleaned.length() && Character.isLowSurrogate(cleaned.charAt(end))
                && Character.isHighSurrogate(cleaned.charAt(end - 1))) {
            end--;
        }
        while (end > 0 && cleaned.charAt(end - 1) == ' ') {
            end--;
        }
        return cleaned.substring(0, end);
    }

    private static boolean unsafe(int c) {
        int type = Character.getType(c);
        return Character.isISOControl(c) || type == Character.LINE_SEPARATOR || type == Character.PARAGRAPH_SEPARATOR
                || type == Character.FORMAT;
    }
}
