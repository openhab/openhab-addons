/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthing.internal.client;

import org.junit.Ignore;
import org.junit.Test;
import org.openhab.binding.webthing.internal.client.dto.WebThingDescription;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 *
 * @author Gregor Roth - Initial contribution
 */
public class WebSocketConnectionImplTest {

    @Test
    @Ignore
    public void testStream() throws Exception {
        var testListener = new TestListener();

        var description = new WebThingDescription();
        description.title = "test";

        var webthing = mock(ConsumedThing.class);
        when(webthing.getThingDescription()).thenReturn(description);

        var downStream = new WebSocketConnectionImpl(webthing, URI.create("ws://127.0.0.1:7332/"),testListener, Duration.ofSeconds(10));
        pause(100000 * 1000);

        assertEquals(1, testListener.numOpened.get());
        assertEquals(0, testListener.numClosed.get());

        downStream.close();
        pause(1000);

        assertEquals(1, testListener.numOpened.get());
        downStream.close();
    }

    public static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException igonre) { }
    }

    private final class TestListener implements ConnectionListener {
        public final AtomicInteger numOpened = new AtomicInteger();
        public final AtomicInteger numClosed = new AtomicInteger();

        @Override
        public void onConnected() {
            numOpened.incrementAndGet();
        }

        @Override
        public void onDisconnected(String reason) {
            numClosed.incrementAndGet();
        }
    }
}
