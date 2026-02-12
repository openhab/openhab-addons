package org.openhab.binding.restify.internal.servlet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public sealed interface Authorization {
    public record Basic(String username) implements Authorization {
    }

    public record Bearer(String token) implements Authorization {
    }
}
