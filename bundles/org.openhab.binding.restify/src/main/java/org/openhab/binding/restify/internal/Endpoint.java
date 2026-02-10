package org.openhab.binding.restify.internal;

import org.eclipse.jdt.annotation.Nullable;

public record Endpoint(String path, String method, @Nullable String authorization, Schema schema) {
}
