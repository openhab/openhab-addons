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
package org.openhab.binding.chatgpt.internal;

import static org.openhab.binding.chatgpt.internal.ChatGPTBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ChatGPTConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class ChatGPTConfiguration {
    // API
    public String baseUrl = DEFAULT_BASE_URL;
    public String apiKey = "";
    // Connection
    public Integer requestTimeout = DEFAULT_REQUEST_TIMEOUT;
    // HLI
    public String model = DEFAULT_MODEL;
    public Double temperature = DEFAULT_TEMPERATURE;
    public Double topP = DEFAULT_TOP_P;
    public Integer maxTokens = DEFAULT_MAX_TOKENS;
    public Integer maxToolCalls = DEFAULT_MAX_TOOL_CALLS;
}
