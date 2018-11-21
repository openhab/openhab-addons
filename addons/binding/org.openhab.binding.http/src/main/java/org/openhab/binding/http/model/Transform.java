package org.openhab.binding.http.model;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Transform {
    private static final Pattern EXTRACT_FUNCTION_PATTERN = Pattern.compile("(.*?)\\((.*)\\)");

    public static Transform parse(final String s) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transform transform = (Transform) o;
        return getFunction().equals(transform.getFunction()) &&
                getPattern().equals(transform.getPattern());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFunction(), getPattern());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Transform.class.getSimpleName() + "[", "]")
                .add("function='" + function + "'")
                .add("pattern='" + pattern + "'")
                .toString();
    }
}


