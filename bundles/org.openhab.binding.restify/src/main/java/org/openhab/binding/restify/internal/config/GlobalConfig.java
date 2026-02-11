package org.openhab.binding.restify.internal.config;

import java.io.Serializable;
import java.util.Map;

public record GlobalConfig(String version, Map<String, String> usernamePasswords) implements Serializable {
    public static final GlobalConfig EMPTY = new GlobalConfig("v1", Map.of());
}
