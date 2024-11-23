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
package org.openhab.binding.argoclima.internal.utils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@code StringUtils} class provides {@code String} manipulation utilities using standard Java facilities
 *
 * @implNote The interface is modeled on {@link org.apache.commons.lang3.StringUtils} interface (which is not used as it
 *           seems frowned upon - ex. due to no support for null-safe annotations)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public final class StringUtils {
    /**
     * Strips any leading and trailing characters that match the input list from the {@code String}.
     * Similar to {@link String#trim()} but strips any characters, not only whitespaces.
     *
     * @implNote API-compatible with {@link org.apache.commons.lang3.StringUtils#strip(String, String)} (except for
     *           {@code NonNull} annotation)
     * @implNote Not a performance-optimized implementation (trying at one would be reinventing the wheel). Uses regular
     *           expressions internally.
     *
     * @param str the String to remove characters from
     * @param stripChars the characters to remove (not null, to strip whitespaces, use {@link String#trim()})
     * @return the stripped String, {@code null} if null String input
     */
    public static String strip(String str, final String stripChars) {
        if (str.isEmpty()) {
            return str;
        }

        var rxCaptureRange = "[" + Pattern.quote(stripChars) + "]";
        var stripCharsCaptureRegex = Objects.requireNonNull(MessageFormat.format("^{0}+|{0}+$", rxCaptureRange));
        return str.replaceAll(stripCharsCaptureRegex, "");
    }

    /**
     * Splits the provided text by provided separator. Adjacent separators are treated as one.
     * Similar to {@link String#split(String)} but patter is not a regex and removes adjacent separators.
     *
     * @implNote API-compatible with {@link org.apache.commons.lang3.StringUtils#splitByWholeSeparator(String, String)}
     *           (except for {@code NonNull} annotation and different return type
     *
     * @param str the String to split
     * @param separator String containing the String to be used as a delimiter
     * @return an list of split Strings
     */
    public static List<String> splitByWholeSeparator(final String str, final String separator) {
        var multiSeparatorPattern = "(" + Pattern.quote(separator) + ")+";
        var stripBeginAndEndPatterns = Objects
                .requireNonNull(MessageFormat.format("^{0}+|{0}+$", multiSeparatorPattern));

        var withoutLeadingAndTrailingSeparators = str.replaceAll(stripBeginAndEndPatterns, "");
        if (withoutLeadingAndTrailingSeparators.isEmpty()) {
            return List.of();
        }

        return Arrays.asList(withoutLeadingAndTrailingSeparators.split(multiSeparatorPattern));
    }
}
