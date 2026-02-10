package org.openhab.binding.restify.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

public sealed interface Schema {
    public String name();

    @Nullable
    public Authorization authorization();

    public record StringSchema(String name, @Nullable Authorization authorization, String value) implements Schema {
    }

    public record ItemSchema(String name, @Nullable Authorization authorization, String itemName,
            String expression) implements Schema {
    }

    public record ThingSchema(String name, @Nullable Authorization authorization, String thingUid,
            String expression) implements Schema {
    }

    public record JsonSchema(String name, @Nullable Authorization authorization,
            Map<String, ? extends Schema> values) implements Schema {
    }
}
