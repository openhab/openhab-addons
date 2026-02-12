package org.openhab.binding.restify.internal.servlet;

import org.eclipse.jdt.annotation.Nullable;

public record Response(@Nullable Authorization authorization, Schema.JsonSchema schema) {
}
