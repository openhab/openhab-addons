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
package org.openhab.binding.knx.internal.project;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A datapoint type as the project names it: {@code DPST-main-sub} or only {@code DPT-main}.
 *
 * @param main the main number
 * @param sub the sub number, or {@code null} when the project gives the main type only
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxDatapointType(int main, @Nullable Integer sub) {

    private static final Pattern ID = Pattern.compile("DPST-(\\d{1,5})-(\\d{1,5})|DPT-(\\d{1,5})");
    // the forms the binding's channel text accepts: 1 to 3 digits, then 3 to 5 digits of sub number
    private static final Pattern TEXT = Pattern.compile("(\\d{1,3})(?:\\.(\\d{3,5}))?");

    /**
     * Parses a {@code DatapointType} attribute, which may hold several ids separated by white space. Ids of
     * another form are skipped. Each type made counts as a record of the read.
     */
    static List<KnxDatapointType> parseList(@Nullable String value, ReadBudget budget) throws KnxProjectException {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        List<KnxDatapointType> types = new ArrayList<>();
        Matcher tokens = ProjectIds.tokens(value);
        while (tokens.find()) {
            Matcher matcher = ID.matcher(tokens.group());
            if (!matcher.matches()) {
                continue;
            }
            budget.countRecord();
            String main = matcher.group(3);
            if (main != null) {
                types.add(new KnxDatapointType(Integer.parseInt(main), null));
            } else {
                types.add(new KnxDatapointType(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
            }
        }
        return List.copyOf(types);
    }

    /**
     * Parses the usual text form, {@code 9.001} or {@code 9}; {@code null} for anything else.
     */
    public static @Nullable KnxDatapointType parse(@Nullable String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = TEXT.matcher(text.trim());
        if (!matcher.matches()) {
            return null;
        }
        String sub = matcher.group(2);
        return new KnxDatapointType(Integer.parseInt(matcher.group(1)), sub == null ? null : Integer.parseInt(sub));
    }

    /** The usual text form, {@code 9.001}, or {@code 9} without a sub number. */
    @Override
    public String toString() {
        Integer sub = this.sub;
        return sub == null ? Integer.toString(main) : main + "." + padSub(sub);
    }

    /** The sub number with at least three digits; built by hand, as a formatter would use the locale's digits. */
    static String padSub(int sub) {
        String digits = Integer.toString(sub);
        return digits.length() >= 3 ? digits : "0".repeat(3 - digits.length()) + digits;
    }
}
