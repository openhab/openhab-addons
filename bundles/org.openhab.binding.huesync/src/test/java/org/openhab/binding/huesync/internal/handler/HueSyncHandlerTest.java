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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.huesync.internal.handler.tasks.HueSyncUpdateTaskResult;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
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
    void disposeWithEmptyConnection() {
        assertDoesNotThrow(handler::dispose);
    }

    @Test
    @DisplayName("Multiple successive dispose() calls should be safe")
    void multipleSuccessiveDisposeCalls() {
        assertDoesNotThrow(() -> {
            handler.dispose();
            handler.dispose();
        });
    }

    @Test
    @DisplayName("Initialize and dispose sequence should work correctly")
    void initializeAndDisposeSequence() {
        assertDoesNotThrow(() -> {
            handler.initialize();
            handler.dispose();
        });
    }

    @Test
    @DisplayName("handleUpdate with null DTO fields should set thing OFFLINE and trigger recovery")
    void handleUpdateWithNullDtoFieldsSetsThingOffline() throws Exception {
        // Arrange: inject a mock scheduler so exceptionHandler.handle() can call scheduler.execute()
        ScheduledExecutorService mockScheduler = mock(ScheduledExecutorService.class);
        Field schedulerField = BaseThingHandler.class.getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        schedulerField.set(handler, mockScheduler);

        when(thing.getUID()).thenReturn(new ThingUID("huesync", "box", "test"));

        // Act: invoke private handleUpdate with an all-null result (simulates connection returning
        // null for device info — possible when device becomes unregistered mid-poll)
        Method handleUpdate = HueSyncHandler.class.getDeclaredMethod("handleUpdate", HueSyncUpdateTaskResult.class);
        handleUpdate.setAccessible(true);
        handleUpdate.invoke(handler, new HueSyncUpdateTaskResult());

        // Assert: thing must have been set OFFLINE so the recovery path can trigger re-init
        ArgumentCaptor<ThingStatusInfo> captor = forClass(ThingStatusInfo.class);
        verify(thing).setStatusInfo(captor.capture());
        assertThat(captor.getValue().getStatus(), is(ThingStatus.OFFLINE));
    }
}
