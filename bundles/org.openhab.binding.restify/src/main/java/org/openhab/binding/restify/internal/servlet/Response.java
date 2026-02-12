package org.openhab.binding.restify.internal.servlet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public record Response(@Nullable Authorization authorization, Schema.JsonSchema schema) {
}
