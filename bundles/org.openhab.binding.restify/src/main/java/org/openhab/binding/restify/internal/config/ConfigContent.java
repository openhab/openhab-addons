package org.openhab.binding.restify.internal.config;

import java.util.List;
import java.util.Optional;

public record ConfigContent(Optional<String> globalConfig, List<String> responses) {
}
