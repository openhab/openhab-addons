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
public sealed interface Json {
    public record StringValue(String value) implements Json {
    }

    public record NumberValue(Number value) implements Json {
    }

    public record BooleanValue(boolean value) implements Json {
    }

    public record JsonObject(Map<String, ? extends Json> response) implements Json {
    }

    public record JsonArray(List<? extends Json> responses) implements Json {
    }

    public static final class NullValue implements Json {
        public static final NullValue NULL_VALUE = new NullValue();

        private NullValue() {
        }
    }
}
