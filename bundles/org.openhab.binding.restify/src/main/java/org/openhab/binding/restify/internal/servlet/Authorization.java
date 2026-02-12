package org.openhab.binding.restify.internal.servlet;

public sealed interface Authorization {
    public record Basic(String username) implements Authorization {
    }

    public record Bearer(String token) implements Authorization {
    }
}
