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
package org.openhab.binding.lgthinq.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;
import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.LG_API_PLATFORM_TYPE_V2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lgthinq.internal.LGThinQStateDescriptionProvider;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientService;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerSnapshot;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests that the per-device command queue doesn't hold threads of the binding pool and runs the commands of a device
 * in order and one at a time.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
class LGThinQCommandQueueTest {
    private static final String BLOCKING_COMMAND = "BLOCKING";
    private static final int HANDLER_COUNT = 8;
    private static final long TIMEOUT_SECONDS = 30;

    private final List<RecordingHandler> handlers = new ArrayList<>();

    @AfterEach
    void tearDown() {
        handlers.forEach(LGThinQAbstractDeviceHandler::disableCommandQueue);
    }

    @Test
    void idleHandlersDoNotHoldSchedulerThreads() throws Exception {
        for (int i = 0; i < HANDLER_COUNT; i++) {
            createHandler("idle" + i, new CountDownLatch(0)).enableCommandQueue();
        }

        assertSchedulerAvailable();
    }

    @Test
    void handlersReleaseSchedulerThreadsAfterProcessingCommands() throws Exception {
        CountDownLatch processed = new CountDownLatch(HANDLER_COUNT);
        for (int i = 0; i < HANDLER_COUNT; i++) {
            RecordingHandler handler = createHandler("busy" + i, processed);
            handler.enableCommandQueue();
            handler.handleCommand(courseChannel(handler), new StringType("COTTON"));
        }

        assertTrue(processed.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertSchedulerAvailable();
    }

    @Test
    void commandsOfADeviceRunInOrderAndOneAtATime() throws Exception {
        int commandCount = 20;
        CountDownLatch processed = new CountDownLatch(commandCount);
        RecordingHandler handler = createHandler("ordered", processed);
        List<String> expected = new ArrayList<>();
        for (int i = 0; i < commandCount; i++) {
            expected.add("C" + i);
            handler.handleCommand(courseChannel(handler), new StringType("C" + i));
            if (i == commandCount / 2) {
                // commands queued before the queue is enabled are executed once it is
                handler.enableCommandQueue();
            }
        }

        assertTrue(processed.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals(expected, handler.processedCommands);
        assertEquals(1, handler.maxConcurrentCommands.get());
    }

    @Test
    void disabledQueueDiscardsPendingCommands() throws Exception {
        CountDownLatch processed = new CountDownLatch(1);
        RecordingHandler handler = createHandler("disabled", processed);
        handler.handleCommand(courseChannel(handler), new StringType("DISCARDED"));
        handler.disableCommandQueue();
        handler.enableCommandQueue();
        handler.handleCommand(courseChannel(handler), new StringType("EXECUTED"));

        assertTrue(processed.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals(List.of("EXECUTED"), handler.processedCommands);
    }

    @Test
    void reenabledQueueDoesNotRunCommandsConcurrently() throws Exception {
        CountDownLatch processed = new CountDownLatch(1);
        RecordingHandler handler = createHandler("reenabled", processed);
        handler.enableCommandQueue();
        handler.handleCommand(courseChannel(handler), new StringType(BLOCKING_COMMAND));
        assertTrue(handler.blockingCommandStarted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        handler.disableCommandQueue();
        handler.enableCommandQueue();
        handler.handleCommand(courseChannel(handler), new StringType("AFTER"));

        // a second drain task would execute AFTER while the blocking command is still running
        assertFalse(processed.await(1, TimeUnit.SECONDS));
        handler.releaseBlockingCommand.countDown();
        assertTrue(processed.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals(List.of(BLOCKING_COMMAND, "AFTER"), handler.processedCommands);
        assertEquals(1, handler.maxConcurrentCommands.get());
    }

    private void assertSchedulerAvailable() throws Exception {
        // the pool has 5 threads by default, fewer than HANDLER_COUNT
        ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(BINDING_ID);
        assertTrue(scheduler.submit(() -> true).get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    private RecordingHandler createHandler(String id, CountDownLatch processed) {
        Thing thing = ThingBuilder.create(THING_TYPE_WASHING_MACHINE, new ThingUID(THING_TYPE_WASHING_MACHINE, id))
                .withProperties(Map.of(PROP_INFO_PLATFORM_TYPE, LG_API_PLATFORM_TYPE_V2)).build();
        RecordingHandler handler = new RecordingHandler(thing, processed);
        handlers.add(handler);
        return handler;
    }

    private static ChannelUID courseChannel(RecordingHandler handler) {
        return new ChannelUID(handler.getThing().getUID(), CHANNEL_DASHBOARD_GRP_ID + "#" + CHANNEL_WMD_COURSE_ID);
    }

    /**
     * Minimal device handler that records the commands it processes. It extends the abstract handler directly
     * because the handler resolves its snapshot class from its generic superclass.
     */
    private static class RecordingHandler
            extends LGThinQAbstractDeviceHandler<WasherDryerCapability, WasherDryerSnapshot> {
        private final Logger logger = LoggerFactory.getLogger(RecordingHandler.class);
        private final CountDownLatch processed;
        private final List<String> processedCommands = new CopyOnWriteArrayList<>();
        private final AtomicInteger runningCommands = new AtomicInteger();
        private final AtomicInteger maxConcurrentCommands = new AtomicInteger();
        private final CountDownLatch blockingCommandStarted = new CountDownLatch(1);
        private final CountDownLatch releaseBlockingCommand = new CountDownLatch(1);

        RecordingHandler(Thing thing, CountDownLatch processed) {
            super(thing, mock(LGThinQStateDescriptionProvider.class), mock(ItemChannelLinkRegistry.class));
            this.processed = processed;
        }

        @Override
        protected void processCommand(AsyncCommandParams params) {
            maxConcurrentCommands.accumulateAndGet(runningCommands.incrementAndGet(), Math::max);
            String command = params.command().toString();
            if (BLOCKING_COMMAND.equals(command)) {
                blockingCommandStarted.countDown();
                // like a request that doesn't react to the interruption sent when the queue is disabled
                awaitIgnoringInterruption(releaseBlockingCommand);
                processedCommands.add(command);
                runningCommands.decrementAndGet();
                return;
            }
            processedCommands.add(command);
            runningCommands.decrementAndGet();
            processed.countDown();
        }

        private static void awaitIgnoringInterruption(CountDownLatch latch) {
            boolean interrupted = false;
            try {
                while (true) {
                    try {
                        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        return;
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public String getDeviceAlias() {
            return "test";
        }

        @Override
        public String getDeviceUriJsonConfig() {
            return "";
        }

        @Override
        public void onDeviceRemoved() {
        }

        @Override
        public void onDeviceDisconnected() {
        }

        @Override
        public void updateChannelDynStateDescription() {
        }

        @Override
        @SuppressWarnings("unchecked")
        public LGThinQApiClientService<WasherDryerCapability, WasherDryerSnapshot> getLgThinQAPIClientService() {
            return mock(LGThinQApiClientService.class);
        }

        @Override
        protected Logger getLogger() {
            return logger;
        }

        @Override
        protected void updateDeviceChannels(WasherDryerSnapshot snapshot) {
        }

        @Override
        protected DeviceTypes getDeviceType() {
            return DeviceTypes.WASHERDRYER_MACHINE;
        }
    }
}
