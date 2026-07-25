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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gemini.internal.api.dto.GeminiContent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The {@link GeminiRequest} record represents the payload for a content generation request, including
 * chat history, system instructions, tools, and generation configurations.
 *
 * @author Florian Hotze - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NonNullByDefault
public record GeminiRequest(@Nullable List<GeminiContent> contents, @Nullable GeminiContent systemInstruction,
        @Nullable GeminiGenerationConfig generationConfig, @Nullable List<GeminiTool> tools) {
}
