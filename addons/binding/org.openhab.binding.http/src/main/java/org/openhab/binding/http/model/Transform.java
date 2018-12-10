package org.openhab.binding.http.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


