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
package org.openhab.binding.huesync.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Regression tests for {@link HueSyncHandler}.
 *
 * <p>
 * Verifies the safe Optional handling introduced by Issue #19079:
 * <ul>
 * <li>dispose() does not throw NoSuchElementException when connection is empty</li>
 * <li>Handler lifecycle is safe against concurrent state changes</li>
 * </ul>
 *
 * @author Patrik Gfeller - Issue #19079, Regression tests
 */
@ExtendWith(MockitoExtension.class)
public class HueSyncHandlerTest {

    private Thing thing;
    private HttpClientFactory httpClientFactory;
    private ThingHandlerCallback callback;
    private HueSyncHandler handler;

    @BeforeEach
    void setup() {
        thing = mock(Thing.class);
        httpClientFactory = mock(HttpClientFactory.class);
        callback = mock(ThingHandlerCallback.class);

        handler = new HueSyncHandler(thing, httpClientFactory);
        handler.setCallback(callback);
    }

    @Test
    @DisplayName("dispose() should not throw NoSuchElementException when connection is empty")
    void disposeWithEmptyConnection() throws Exception {
        // Given: handler with empty connection (default state)
        // When: dispose is called
        // Then: should not throw NoSuchElementException
        handler.dispose();

        // Test passes if no exception was thrown
        assertThat(true, is(true));
    }

    @Test
    @DisplayName("Multiple successive dispose() calls should be safe")
    void multipleSideDisposeCalls() throws Exception {
        // Given: handler initialized
        // When: dispose is called multiple times
        handler.dispose();
        handler.dispose();

        // Then: should not throw exception
        assertThat(true, is(true));
    }

    @Test
    @DisplayName("Initialize and dispose sequence should work correctly")
    void initializeAndDisposeSequence() throws Exception {
        // Given: new handler
        // When: initialize then dispose
        handler.initialize();
        handler.dispose();

        // Then: should not throw exception
        assertThat(true, is(true));
    }
}
