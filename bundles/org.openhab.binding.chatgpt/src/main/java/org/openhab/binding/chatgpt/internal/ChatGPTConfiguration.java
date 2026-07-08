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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ChatGPTConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class ChatGPTConfiguration {

    public String apiKey = "";
    public String baseUrl = "https://api.openai.com/v1";
    public String model = "gpt-4o-mini";
    public Double temperature = 1.0;
    public Integer maxTokens = 1000;
    public Double topP = 1.0;
    public String systemMessage = "";
    public Integer requestTimeout = ChatGPTHandler.DEFAULT_REQUEST_TIMEOUT_S;
    public Integer maxToolCalls = 10;
}
