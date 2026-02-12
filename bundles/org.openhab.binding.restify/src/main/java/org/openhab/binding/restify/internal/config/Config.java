package org.openhab.binding.restify.internal.config;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public record Config(int version, Map<String, String> usernamePasswords) implements Serializable {
    public static final int LATEST_VERSION = 1;
    public static final Config EMPTY = new Config(LATEST_VERSION, Map.of());
}
