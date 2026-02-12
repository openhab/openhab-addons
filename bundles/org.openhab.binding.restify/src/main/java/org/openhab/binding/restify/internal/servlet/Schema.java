/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
