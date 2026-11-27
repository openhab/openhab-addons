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
package org.openhab.binding.gemini.internal.api.dto.request;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Allows to specify how much to think. See <a href="https://ai.google.dev/api/generate-content#ThinkingLevel">Gemini
 * API: GenerateContent: ThinkingLevel</a>.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public enum GeminiThinkingLevel {
    THINKING_LEVEL_UNSPECIFIED,
    MINIMAL,
    LOW,
    MEDIUM,
    HIGH;

    public static @Nullable GeminiThinkingLevel fromString(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if ("unspecified".equalsIgnoreCase(value)) {
            return THINKING_LEVEL_UNSPECIFIED;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
