package org.openhab.binding.restify.internal.servlet;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public sealed interface Schema {
    public record StringSchema(String value) implements Schema {
    }

    public record ItemSchema(String itemName, String expression) implements Schema {
    }

    public record ThingSchema(String thingUid, String expression) implements Schema {
    }

    public record JsonSchema(Map<String, ? extends Schema> values) implements Schema {
    }

    public record ArraySchema(List<? extends Schema> values) implements Schema {
    }
}
