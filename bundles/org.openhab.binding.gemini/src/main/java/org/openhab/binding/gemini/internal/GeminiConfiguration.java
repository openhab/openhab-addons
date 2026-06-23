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
package org.openhab.binding.gemini.internal;

import static org.openhab.binding.gemini.internal.GeminiBindingConstants.DEFAULT_MAX_OUTPUT_TOKENS;
import static org.openhab.binding.gemini.internal.GeminiBindingConstants.DEFAULT_MODEL;
import static org.openhab.binding.gemini.internal.GeminiBindingConstants.DEFAULT_REQUEST_TIMEOUT;
import static org.openhab.binding.gemini.internal.GeminiBindingConstants.DEFAULT_TEMPERATURE;
import static org.openhab.binding.gemini.internal.GeminiBindingConstants.DEFAULT_TOP_P;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GeminiConfiguration} class contains the configuration for the Gemini account thing.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class GeminiConfiguration {
    // Authentication
    public String apiKey = "";
    // Connection
    public int requestTimeout = DEFAULT_REQUEST_TIMEOUT;
    // HLI
    public String model = DEFAULT_MODEL;
    public double temperature = DEFAULT_TEMPERATURE;
    public double topP = DEFAULT_TOP_P;
    public int maxOutputTokens = DEFAULT_MAX_OUTPUT_TOKENS;
}
