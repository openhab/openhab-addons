package org.openhab.binding.restify.internal;

public sealed interface Authorization {
    public record Basic(String username, String password) implements Authorization {
    }

    public record Bearer(String token) implements Authorization {
    }
}
