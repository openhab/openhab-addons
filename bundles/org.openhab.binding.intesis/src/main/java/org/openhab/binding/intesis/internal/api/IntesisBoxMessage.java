/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.intesis.internal.api;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class IntesisBoxMessage {
    public static final String ID = "ID";
    public static final String INFO = "INFO";
    public static final String SET = "SET";
    public static final String CHN = "CHN";
    public static final String GET = "GET";
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";
    public static final String CFG = "CFG";
    public static final String LIMITS = "LIMITS";
    public static final String DISCOVER = "DISCOVER";

    private static final Pattern REGEX = Pattern.compile("^([^,]+)(?:,(\\d+))?:([^,]+),([A-Z0-9.,\\[\\]]+)$");

    @SuppressWarnings("unused")
    private final String acNum;
    private final String command;
    private final String function;
    private final String value;

    private IntesisBoxMessage(String command, String acNum, String function, String value) {
        this.command = command;
        this.acNum = acNum;
        this.function = function;
        this.value = value;
    }

    public String getCommand() {
        return command;
    }

    public String getFunction() {
        return function;
    }

    public String getValue() {
        return value;
    }

    public List<String> getLimitsValue() {
        return Arrays.asList(value.substring(1, value.length() - 1).split(","));
    }

    public static @Nullable IntesisBoxMessage parse(String message) {
        Matcher m = REGEX.matcher(message);
        if (!m.find()) {
            return null;
        }

        return new IntesisBoxMessage(m.group(1), m.group(2), m.group(3), m.group(4));
    }
}
