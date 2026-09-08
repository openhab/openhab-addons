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
package org.openhab.binding.chatgpt.internal.hli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.openhab.binding.chatgpt.internal.ChatGPTHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * Unit tests for {@link ChatGPTHLIService}.
 *
 * @author Florian Hotze - Initial contribution
 */
public class ChatGPTHLIServiceTest {
    @Test
    public void testGetIdAndGetLabel() {
        ChatGPTHLIService service = new ChatGPTHLIService();

        ChatGPTHandler handler = mock(ChatGPTHandler.class);
        Thing thing = mock(Thing.class);
        ThingUID thingUid = new ThingUID("chatgpt:account:ollama");

        when(thing.getUID()).thenReturn(thingUid);
        when(thing.getLabel()).thenReturn("Ollama");
        when(handler.getThing()).thenReturn(thing);

        service.setThingHandler(handler);

        assertEquals("chatgpt:ollama", service.getId());
        assertEquals("ChatGPT Interpreter (Ollama)", service.getLabel(null));
        assertEquals("ChatGPT Interpreter (Ollama)", service.getLabel(Locale.ENGLISH));
    }

    @Test
    public void testGetLabelWithNullLabelFallback() {
        ChatGPTHLIService service = new ChatGPTHLIService();

        ChatGPTHandler handler = mock(ChatGPTHandler.class);
        Thing thing = mock(Thing.class);
        ThingUID thingUid = new ThingUID("chatgpt:account:ollama");

        when(thing.getUID()).thenReturn(thingUid);
        when(thing.getLabel()).thenReturn(null);
        when(handler.getThing()).thenReturn(thing);

        service.setThingHandler(handler);

        assertEquals("ChatGPT Interpreter (ollama)", service.getLabel(null));
    }

    @Test
    public void testIdUniquenessAcrossThings() {
        ChatGPTHLIService service1 = new ChatGPTHLIService();
        ChatGPTHLIService service2 = new ChatGPTHLIService();

        ChatGPTHandler handler1 = mock(ChatGPTHandler.class);
        Thing thing1 = mock(Thing.class);
        ThingUID thingUid1 = new ThingUID("chatgpt:account:ollama");
        when(thing1.getUID()).thenReturn(thingUid1);
        when(handler1.getThing()).thenReturn(thing1);

        ChatGPTHandler handler2 = mock(ChatGPTHandler.class);
        Thing thing2 = mock(Thing.class);
        ThingUID thingUid2 = new ThingUID("chatgpt:account:mistral");
        when(thing2.getUID()).thenReturn(thingUid2);
        when(handler2.getThing()).thenReturn(thing2);

        service1.setThingHandler(handler1);
        service2.setThingHandler(handler2);

        assertNotEquals(service1.getId(), service2.getId());
        assertEquals("chatgpt:ollama", service1.getId());
        assertEquals("chatgpt:mistral", service2.getId());
    }
}
