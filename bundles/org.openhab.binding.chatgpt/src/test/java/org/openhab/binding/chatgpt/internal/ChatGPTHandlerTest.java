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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Thing;

/**
 * Unit tests for {@link ChatGPTHandler}.
 *
 * @author Florian Hotze - Initial contribution
 */
public class ChatGPTHandlerTest {

    private ChatGPTHandler handler;

    @BeforeEach
    public void setUp() {
        Thing thing = mock(Thing.class);
        HttpClient httpClient = mock(HttpClient.class);
        TranslationProvider translationProvider = mock(TranslationProvider.class);
        handler = new ChatGPTHandler(thing, httpClient, translationProvider);
    }

    @Test
    public void testIsTokenRequiredEndpointForDefaultAndKnownCloudEndpoints() {
        assertTrue(handler.isTokenRequiredEndpoint(null));
        assertTrue(handler.isTokenRequiredEndpoint(""));
        assertTrue(handler.isTokenRequiredEndpoint("  "));
        assertTrue(handler.isTokenRequiredEndpoint("https://api.openai.com/v1"));
        assertTrue(handler.isTokenRequiredEndpoint("https://api.openai.com/v1/chat/completions"));
        assertTrue(handler.isTokenRequiredEndpoint("https://openrouter.ai/api/v1"));
        assertTrue(handler.isTokenRequiredEndpoint("https://api.mistral.ai/v1"));
    }

    @Test
    public void testIsTokenRequiredEndpointForLocalEndpoints() {
        assertFalse(handler.isTokenRequiredEndpoint("http://localhost:11434/v1"));
        assertFalse(handler.isTokenRequiredEndpoint("http://localhost:8080/v1"));
        assertFalse(handler.isTokenRequiredEndpoint("http://192.168.1.100:1234/v1"));
        assertFalse(handler.isTokenRequiredEndpoint("http://modelrunner.local/v1"));
    }
}
