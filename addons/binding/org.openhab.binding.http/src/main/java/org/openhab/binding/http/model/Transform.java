/*
 * Copyright (c) 2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class describing a parsed transformation function.
 *
 * @author Brian J. Tarricone
 */
@NonNullByDefault
public class Transform {
    private static final Pattern EXTRACT_FUNCTION_PATTERN = Pattern.compile("(.*?)\\((.*)\\)");

    static Transform parse(final String s) {
        final Matcher matcher = EXTRACT_FUNCTION_PATTERN.matcher(s);
        if (!matcher.matches() || !matcher.find()) {
            throw new IllegalArgumentException("Supplied string (" + s + ") is not a valid transformation funcion");
        } else {
            return new Transform(matcher.group(1), matcher.group(2));
        }
    }

    private final String function;
    private final String pattern;

    private Transform(final String function, final String pattern) {
        this.function = function;
        this.pattern = pattern;
    }

    public String getFunction() {
        return function;
    }

    public String getPattern() {
        return pattern;
    }
}
