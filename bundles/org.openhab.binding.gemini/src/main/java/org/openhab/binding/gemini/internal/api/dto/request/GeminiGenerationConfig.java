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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The {@link GeminiGenerationConfig} record contains the configuration parameters for controlling
 * content generation, such as temperature, topP, and max output tokens.
 *
 * @author Florian Hotze - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NonNullByDefault
public record GeminiGenerationConfig(@Nullable Integer maxOutputTokens, @Nullable Double temperature,
        @Nullable Double topP, @Nullable GeminiThinkingConfig thinkingConfig) {
}
