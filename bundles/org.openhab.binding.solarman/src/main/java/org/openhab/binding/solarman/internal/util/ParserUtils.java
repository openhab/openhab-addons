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
package org.openhab.binding.solarman.internal.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ParserUtils} contains different utility methods for parsing
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class ParserUtils {
    private static final Pattern REGISTER_PATTERN = Pattern.compile("\\s*(0x[\\da-fA-F]+|[\\d]+)\\s*");

    public static List<Integer> parseRegisters(String registers) {
        String[] tokens = registers.split(",");
        return Stream.of(tokens).map(REGISTER_PATTERN::matcher).filter(Matcher::find).map(matcher -> matcher.group(1))
                .map(ParserUtils::parseNumber).toList();
    }

    public static int parseNumber(String number) {
        return number.startsWith("0x") ? Integer.parseInt(number.substring(2), 16) : Integer.parseInt(number);
    }
}
