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
package org.openhab.binding.candy.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.candy.internal.CandyBindingConstants.*;
import static org.openhab.binding.candy.internal.protocol.CandyProtocolException.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.candy.internal.BiancaConfiguration;
import org.openhab.binding.candy.internal.CandyActions;
import org.openhab.binding.candy.internal.client.BiancaAccountClient;
import org.openhab.binding.candy.internal.client.BiancaLocalClient;
import org.openhab.binding.candy.internal.protocol.BiancaCommands;
import org.openhab.binding.candy.internal.protocol.BiancaProgramCatalog.Program;
import org.openhab.binding.candy.internal.protocol.BiancaSnapshot;
import org.openhab.binding.candy.internal.protocol.BiancaStatusParser;
import org.openhab.binding.candy.internal.protocol.CandyApplianceSelectionException;
import org.openhab.binding.candy.internal.protocol.CandyAuthenticationException;
import org.openhab.binding.candy.internal.protocol.CandyInvalidHostException;
import org.openhab.binding.candy.internal.protocol.CandyProtocolException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Deterministic lifecycle and coordinator tests with manually advanced time and timers.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class BiancaHandlerTest {
    private final BiancaLocalClient client = mock(BiancaLocalClient.class);
    private final BiancaAccountClient accountClient = mock(BiancaAccountClient.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "candy-test-state");
        thread.setDaemon(true);
        return thread;
    });
    private final BlockingQueue<CompletableFuture<BiancaSnapshot>> reads = new LinkedBlockingQueue<>();
    private final BlockingQueue<CompletableFuture<List<String>>> accountReads = new LinkedBlockingQueue<>();
    private final BlockingQueue<CompletableFuture<List<Program>>> catalogReads = new LinkedBlockingQueue<>();
    private final BlockingQueue<CompletableFuture<Integer>> writes = new LinkedBlockingQueue<>();
    private final BlockingQueue<Timer> allTimers = new LinkedBlockingQueue<>();
    private final AtomicLong clock = new AtomicLong();
    private final Map<String, State> states = new ConcurrentHashMap<>();
    private final AtomicReference<ThingStatusInfo> status = new AtomicReference<>();
    private final AtomicReference<Timer> timer = new AtomicReference<>();
    private final AtomicBoolean preventCancellation = new AtomicBoolean();
    private final CountDownLatch cleanupEnqueued = new CountDownLatch(1);
    private final AtomicBoolean blockInitialization = new AtomicBoolean();
    private final CountDownLatch preparingSession = new CountDownLatch(1);
    private final CountDownLatch continueInitialization = new CountDownLatch(1);
    private final List<ScheduledExecutorService> executors = new ArrayList<>();
    private final BiancaHandler handler = new BiancaHandler(thing("washer.local", "abcdefghijklmnop"), client,
            clock::get, () -> accountClient) {
        @Override
        ScheduledExecutorService createExecutor() {
            ScheduledExecutorService executor = executor();
            if (blockInitialization.get()) {
                preparingSession.countDown();
                try {
                    assertTrue(continueInitialization.await(10, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(e);
                }
            }
            return executor;
        }
    };

    @BeforeEach
    void setup() {
        doAnswer(invocation -> {
            CompletableFuture<List<Program>> future = new CompletableFuture<>();
            catalogReads.add(future);
            return future;
        }).when(accountClient).fetchPrograms(any(), anyString());
        doAnswer(invocation -> {
            CompletableFuture<Integer> future = new CompletableFuture<>();
            writes.add(future);
            return future;
        }).when(client).write(any(), anyString());
        doAnswer(invocation -> {
            CompletableFuture<List<String>> future = preventCancellation.get() ? new CompletableFuture<>() {
                @Override
                public boolean cancel(boolean interrupt) {
                    return false;
                }
            } : new CompletableFuture<>();
            accountReads.add(future);
            return future;
        }).when(accountClient).fetchKeys(any());
        doAnswer(invocation -> {
            CompletableFuture<BiancaSnapshot> future = new CompletableFuture<>();
            reads.add(future);
            return future;
        }).when(client).read(any(), anyList());
        doAnswer(invocation -> {
            CompletableFuture<BiancaSnapshot> future = preventCancellation.get() ? new CompletableFuture<>() {
                @Override
                public boolean cancel(boolean interrupt) {
                    return false;
                }
            } : new CompletableFuture<>();
            reads.add(future);
            return future;
        }).when(client).read(any());
        doAnswer(invocation -> {
            ChannelUID channel = invocation.getArgument(0);
            states.put(channel.getId(), invocation.getArgument(1));
            return null;
        }).when(callback).stateUpdated(any(), any());
        doAnswer(invocation -> {
            status.set(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(), any());
        handler.setCallback(callback);
    }

    @AfterEach
    void cleanup() throws InterruptedException {
        handler.dispose();
        worker.shutdownNow();
        assertTrue(worker.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    void validatesConfigurationBeforeAnyRead() {
        handler.thingUpdated(thing("washer.local", ""));
        assertEquals(ThingStatus.OFFLINE, status.get().getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.get().getStatusDetail());
        assertEquals("@text/config-error-account", status.get().getDescription());
        verifyNoInteractions(client);
        assertTrue(executors.isEmpty());
        assertEquals(UnDefType.UNDEF, states.get(CHANNEL_LAST_UPDATE));
    }

    @Test
    void reportsInvalidHostAsLocalizedConfigurationError() {
        handler.thingUpdated(thing("washer.local/path", "abcdefghijklmnop"));
        assertEquals(ThingStatus.OFFLINE, status.get().getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.get().getStatusDetail());
        assertEquals("@text/config-error-host", status.get().getDescription());
        verifyNoInteractions(client);
        assertTrue(executors.isEmpty());
    }

    @Test
    void invalidReconfigurationStopsPollingUntilTheConfigurationIsFixed() throws Exception {
        preventCancellation.set(true);
        handler.initialize();
        nextRead().complete(snapshot(2));
        flush();
        runTimer();
        var obsolete = nextRead();

        handler.thingUpdated(thing("washer.local/path", "abcdefghijklmnop"));
        assertEquals(ThingStatus.OFFLINE, status.get().getStatus());
        assertEquals("@text/config-error-host", status.get().getDescription());
        assertEquals(UnDefType.UNDEF, states.get(CHANNEL_MACHINE_STATE));
        assertEquals(UnDefType.UNDEF, states.get(CHANNEL_LAST_UPDATE));
        assertEquals(1, executors.size());
        verify(executors.getFirst()).shutdown();

        clearInvocations(callback);
        obsolete.complete(snapshot(7));
        handler.handleCommand(channel(), RefreshType.REFRESH);
        flush();
        verifyNoInteractions(callback);
        verify(client, times(2)).read(any());

        handler.thingUpdated(thing("washer.local", "abcdefghijklmnop"));
        nextRead().complete(snapshot(2));
        flush();
        assertEquals(ThingStatus.ONLINE, status.get().getStatus());
        assertEquals(new StringType("RUNNING"), states.get(CHANNEL_MACHINE_STATE));
        assertEquals(2, executors.size());
    }

    @Test
    void disposalDuringConfigurationErrorDeliveryPreventsFurtherUpdates() {
        doAnswer(invocation -> {
            handler.dispose();
            return null;
        }).when(callback).stateUpdated(eq(new ChannelUID(handler.getThing().getUID(), CHANNEL_LAST_UPDATE)),
                eq(UnDefType.UNDEF));
        handler.thingUpdated(thing("washer.local/path", "abcdefghijklmnop"));
        verifyNoInteractions(client);
        verify(callback, never()).statusUpdated(any(), any());
        assertTrue(executors.isEmpty());
    }

    @Test
    void publishesReadThenInvalidatesOnFailureAndRecovers() throws Exception {
        handler.initialize();
        nextRead().complete(snapshot(2));
        flush();
        assertEquals(ThingStatus.ONLINE, status.get().getStatus());
        assertEquals(new StringType("RUNNING"), states.get(CHANNEL_MACHINE_STATE));
        State lastSuccessfulRead = states.get(CHANNEL_LAST_UPDATE);
        assertNotEquals(UnDefType.UNDEF, lastSuccessfulRead);
        assertEquals(30, timer.get().seconds());

        runTimer();
        nextRead().completeExceptionally(new CandyProtocolException(TRANSPORT));
        flush();
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, status.get().getStatusDetail());
        assertEquals(new CandyProtocolException(TRANSPORT).getMessage(), status.get().getDescription());
        assertEquals(UnDefType.UNDEF, states.get(CHANNEL_MACHINE_STATE));
        assertEquals(lastSuccessfulRead, states.get(CHANNEL_LAST_UPDATE));
        assertEquals(30, timer.get().seconds());

        runTimer();
        nextRead().completeExceptionally(new CandyProtocolException(TRANSPORT));
        flush();
        assertEquals(60, timer.get().seconds());
        runTimer();
        nextRead().complete(snapshot(7));
        flush();
        assertEquals(ThingStatus.ONLINE, status.get().getStatus());
        assertEquals(new StringType("FINISHED"), states.get(CHANNEL_MACHINE_STATE));
        assertEquals(30, timer.get().seconds());
    }

    @ParameterizedTest
    @MethodSource("clientFailures")
    void localizesClientFailuresAndRecovers(CandyProtocolException failure, ThingStatusDetail detail) throws Exception {
        handler.initialize();
        nextRead().completeExceptionally(new CompletionException(new ExecutionException(failure)));
        flush();
        assertEquals(ThingStatus.OFFLINE, status.get().getStatus());
        assertEquals(detail, status.get().getStatusDetail());
        assertEquals(failure.getMessage(), status.get().getDescription());
        assertEquals(UnDefType.UNDEF, states.get(CHANNEL_MACHINE_STATE));
        assertEquals(UnDefType.UNDEF, states.get(CHANNEL_LAST_UPDATE));
        if (detail == ThingStatusDetail.CONFIGURATION_ERROR) {
            verify(executors.getFirst(), never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
            verify(client, times(1)).read(any());
            handler.thingUpdated(thing("other.local", "abcdefghijklmnop"));
        } else {
            assertEquals(30, timer.get().seconds());
            runTimer();
        }
        nextRead().complete(snapshot(2));
        flush();
        assertEquals(ThingStatus.ONLINE, status.get().getStatus());
        assertEquals(30, timer.get().seconds());
    }

    private static Stream<Arguments> clientFailures() {
        return Stream.of(Arguments.of(new CandyInvalidHostException(), ThingStatusDetail.CONFIGURATION_ERROR),
                Arguments.of(new CandyProtocolException(TRANSPORT), ThingStatusDetail.COMMUNICATION_ERROR),
                Arguments.of(new CandyProtocolException(TIMEOUT), ThingStatusDetail.COMMUNICATION_ERROR),
                Arguments.of(new CandyProtocolException(HTTP_STATUS), ThingStatusDetail.COMMUNICATION_ERROR),
                Arguments.of(new CandyProtocolException(CLIENT_UNAVAILABLE), ThingStatusDetail.COMMUNICATION_ERROR),
                Arguments.of(new CandyProtocolException(INVALID_RESPONSE), ThingStatusDetail.COMMUNICATION_ERROR),
                Arguments.of(new CandyAuthenticationException(), ThingStatusDetail.CONFIGURATION_ERROR),
                Arguments.of(new CandyApplianceSelectionException(), ThingStatusDetail.CONFIGURATION_ERROR),
                Arguments.of(new CandyAuthenticationException("plain authentication failure"),
                        ThingStatusDetail.CONFIGURATION_ERROR),
                Arguments.of(new CandyProtocolException("@text/account-error-authentication"),
                        ThingStatusDetail.COMMUNICATION_ERROR));
    }

    @ParameterizedTest
    @ValueSource(strings = { "@text/protocol-error-json", "plain diagnostic message" })
    void forwardsTranslatedAndPlainExceptionMessages(String message) throws Exception {
        handler.initialize();
        var failure = new CandyProtocolException(message);
        nextRead().completeExceptionally(new CompletionException(failure));
        flush();
        assertEquals(ThingStatus.OFFLINE, status.get().getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, status.get().getStatusDetail());
        assertEquals(failure.getMessage(), status.get().getDescription());
    }

    @ParameterizedTest
    @CsvSource({ "false", "true" })
    void sanitizesUnexpectedFailuresAndKeepsPolling(boolean synchronous) throws Exception {
        IllegalStateException failure = new IllegalStateException("private response and key details");
        CountDownLatch failed = new CountDownLatch(1);
        doAnswer(invocation -> {
            ThingStatusInfo updated = invocation.getArgument(1);
            status.set(updated);
            if (updated.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR) {
                failed.countDown();
            }
            return null;
        }).when(callback).statusUpdated(any(), any());
        if (synchronous) {
            doThrow(failure).when(client).read(any());
        }
        handler.initialize();
        if (!synchronous) {
            nextRead().completeExceptionally(failure);
        }
        assertTrue(failed.await(10, TimeUnit.SECONDS));
        flush();
        assertEquals(ThingStatus.OFFLINE, status.get().getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, status.get().getStatusDetail());
        assertEquals("@text/communication-error", status.get().getDescription());
        assertEquals(30, timer.get().seconds());
        doReturn(CompletableFuture.completedFuture(snapshot(2))).when(client).read(any());
        runTimer();
        flush();
        assertEquals(ThingStatus.ONLINE, status.get().getStatus());
        assertEquals(new StringType("RUNNING"), states.get(CHANNEL_MACHINE_STATE));
    }

    @Test
    void obsoleteFailureCannotChangeTheReplacementSession() throws Exception {
        preventCancellation.set(true);
        handler.initialize();
        var obsolete = nextRead();
        handler.thingUpdated(thing("other.local", "ponmlkjihgfedcba"));
        nextRead().complete(snapshot(2));
        flush();
        clearInvocations(callback);
        obsolete.completeExceptionally(new CandyInvalidHostException());
        flush();
        verifyNoInteractions(callback);
        assertEquals(ThingStatus.ONLINE, status.get().getStatus());
        assertEquals(30, timer.get().seconds());
    }

    @Test
    void accountLoginTimeDoesNotConsumeTheLocalReadRateLimit() throws Exception {
        handler.thingUpdated(accountThing());
        var account = nextAccountRead();
        clock.addAndGet(TimeUnit.SECONDS.toNanos(10));
        account.complete(List.of("abcdefghijklmnop"));
        nextRead().complete(snapshot(1));
        flush();
        handler.handleCommand(channel(), RefreshType.REFRESH);
        flush();
        assertEquals(15, timer.get().seconds());
        runTimer();
        nextRead().complete(snapshot(1));
        flush();
        verify(accountClient, times(1)).fetchKeys(any());
    }

    @Test
    void retrievesAccountKeysThenKeepsTheMatchingKeyForLocalPolling() throws Exception {
        handler.thingUpdated(accountThing());
        nextAccountRead().complete(List.of("ponmlkjihgfedcba", "abcdefghijklmnop"));
        var local = nextRead();
        ArgumentCaptor<BiancaConfiguration> configuration = ArgumentCaptor.forClass(BiancaConfiguration.class);
        verify(client).read(configuration.capture(), eq(List.of("ponmlkjihgfedcba", "abcdefghijklmnop")));
        configuration.getValue().encryptionKey = "abcdefghijklmnop";
        local.complete(snapshot(2));
        flush();
        assertEquals(ThingStatus.ONLINE, status.get().getStatus());
        assertFalse(handler.getThing().getConfiguration().containsKey("encryptionKey"));
        runTimer();
        nextRead().complete(snapshot(7));
        flush();
        verify(accountClient, times(1)).fetchKeys(any());
        verify(client).read(any());
        assertEquals(new StringType("FINISHED"), states.get(CHANNEL_MACHINE_STATE));
    }

    @Test
    void keepsAccountCandidatesWhenTheWasherIsInitiallyUnreachable() throws Exception {
        handler.thingUpdated(accountThing());
        nextAccountRead().complete(List.of("abcdefghijklmnop"));
        nextRead().completeExceptionally(new CandyProtocolException(TRANSPORT));
        flush();
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, status.get().getStatusDetail());
        assertEquals(30, timer.get().seconds());
        runTimer();
        var local = nextRead();
        ArgumentCaptor<BiancaConfiguration> configuration = ArgumentCaptor.forClass(BiancaConfiguration.class);
        verify(client, times(2)).read(configuration.capture(), eq(List.of("abcdefghijklmnop")));
        configuration.getValue().encryptionKey = "abcdefghijklmnop";
        local.complete(snapshot(2));
        flush();
        verify(accountClient, times(1)).fetchKeys(any());
        assertEquals(ThingStatus.ONLINE, status.get().getStatus());
    }

    @Test
    void disposalCancelsAccountBootstrap() throws Exception {
        handler.thingUpdated(accountThing());
        var keys = nextAccountRead();
        handler.dispose();
        assertTrue(keys.isCancelled());
        verifyNoInteractions(client);
    }

    @Test
    void disposalCancelsLocalKeyMatching() throws Exception {
        handler.thingUpdated(accountThing());
        nextAccountRead().complete(List.of("abcdefghijklmnop"));
        var local = nextRead();
        handler.dispose();
        assertTrue(local.isCancelled());
    }

    @Test
    void obsoleteAccountKeysCannotStartALocalReadAfterReconfiguration() throws Exception {
        preventCancellation.set(true);
        handler.thingUpdated(accountThing());
        var keys = nextAccountRead();
        handler.thingUpdated(thing("washer.local", "abcdefghijklmnop"));
        nextRead().complete(snapshot(2));
        flush();
        clearInvocations(callback);
        keys.complete(List.of("ponmlkjihgfedcba"));
        flush();
        verify(client, never()).read(any(), anyList());
        verifyNoInteractions(callback);
        assertEquals(ThingStatus.ONLINE, status.get().getStatus());
    }

    @Test
    void authenticationFailureWaitsForAnExplicitRetryOrConfigurationChange() throws Exception {
        handler.thingUpdated(accountThing());
        nextAccountRead().completeExceptionally(new CandyAuthenticationException());
        flush();
        assertEquals(ThingStatus.OFFLINE, status.get().getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.get().getStatusDetail());
        assertEquals(new CandyAuthenticationException().getMessage(), status.get().getDescription());
        verify(executors.getFirst(), never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        verifyNoInteractions(client);
        handler.thingUpdated(accountThing());
        nextAccountRead().complete(List.of("abcdefghijklmnop"));
        nextRead().complete(snapshot(2));
        flush();
        assertEquals(ThingStatus.ONLINE, status.get().getStatus());
    }

    @Test
    void backsOffTemporaryAccountFailuresAndRecovers() throws Exception {
        handler.thingUpdated(accountThing());
        nextAccountRead().completeExceptionally(new CandyProtocolException("@text/account-error-transport"));
        flush();
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, status.get().getStatusDetail());
        assertEquals(300, timer.get().seconds());
        runTimer();
        nextAccountRead().complete(List.of("abcdefghijklmnop"));
        var local = nextRead();
        ArgumentCaptor<BiancaConfiguration> configuration = ArgumentCaptor.forClass(BiancaConfiguration.class);
        verify(client).read(configuration.capture(), eq(List.of("abcdefghijklmnop")));
        configuration.getValue().encryptionKey = "abcdefghijklmnop";
        local.complete(snapshot(2));
        flush();
        assertEquals(ThingStatus.ONLINE, status.get().getStatus());
        assertEquals(30, timer.get().seconds());
    }

    @Test
    void coalescesInFlightRefreshesAndRejectsOrdinaryCommands() throws Exception {
        handler.initialize();
        var first = nextRead();
        for (int i = 0; i < 100; i++) {
            handler.handleCommand(channel(), RefreshType.REFRESH);
        }
        handler.handleCommand(channel(), OnOffType.ON);
        handler.handleCommand(channel(), new StringType("STOP"));
        flush();
        verify(client, times(1)).read(any());
        clock.set(TimeUnit.SECONDS.toNanos(15));
        first.complete(snapshot(2));
        flush();
        assertEquals(0, timer.get().seconds());
        runTimer();
        nextRead().complete(snapshot(2));
        flush();
        verify(client, times(2)).read(any());
        assertEquals(30, timer.get().seconds());
    }

    @Test
    void rateLimitsExplicitRefreshes() throws Exception {
        handler.initialize();
        nextRead().complete(snapshot(2));
        flush();
        handler.handleCommand(channel(), RefreshType.REFRESH);
        flush();
        verify(client, times(1)).read(any());
        assertEquals(15, timer.get().seconds());
        runTimer();
        nextRead();
        verify(client, times(2)).read(any());
    }

    @Test
    void capsBackoffAndPreservesLongerConfiguredIntervals() throws Exception {
        handler.initialize();
        for (long delay : List.of(30L, 60L, 120L, 240L, 300L, 300L)) {
            nextRead().completeExceptionally(new CandyProtocolException(TRANSPORT));
            flush();
            assertEquals(delay, timer.get().seconds());
            runTimer();
        }
        var oldRequest = nextRead();
        handler.thingUpdated(ThingBuilder.create(THING_TYPE_BIANCA, "test").withConfiguration(new Configuration(
                Map.of("hostName", "washer.local", "encryptionKey", "abcdefghijklmnop", "refreshInterval", 900)))
                .build());
        assertTrue(oldRequest.isCancelled());
        nextRead().completeExceptionally(new CandyProtocolException(TRANSPORT));
        flush();
        assertEquals(900, timer.get().seconds());
    }

    @Test
    void disposalCancelsInFlightWorkAndIsIdempotent() throws Exception {
        handler.initialize();
        var request = nextRead();
        flush();
        handler.dispose();
        assertTrue(request.isCancelled());
        clearInvocations(callback);
        handler.handleCommand(channel(), RefreshType.REFRESH);
        handler.dispose();
        flush();
        verifyNoInteractions(callback);
        verify(executors.getFirst()).shutdown();
    }

    @Test
    void obsoleteResponseCannotPublishIntoReconfiguredSession() throws Exception {
        preventCancellation.set(true);
        handler.initialize();
        var obsolete = nextRead();
        flush();
        handler.thingUpdated(thing("other.local", "ponmlkjihgfedcba"));
        var replacement = nextRead();
        flush();
        assertEquals(UnDefType.UNDEF, states.get(CHANNEL_LAST_UPDATE));
        replacement.complete(snapshot(2));
        flush();
        clearInvocations(callback);
        obsolete.complete(snapshot(7));
        flush();
        verifyNoInteractions(callback);
        assertEquals(new StringType("RUNNING"), states.get(CHANNEL_MACHINE_STATE));
        verify(client).read(argThat(config -> "other.local".equals(config.hostName)));
        verify(executors.getFirst()).shutdown();
        assertEquals(2, executors.size());
    }

    @Test
    void disposalOrdersAgainstAlreadyRunningPublication() throws Exception {
        CountDownLatch publishing = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            if (new StringType("RUNNING").equals(invocation.getArgument(1))) {
                publishing.countDown();
                assertTrue(release.await(10, TimeUnit.SECONDS));
            }
            return null;
        }).when(callback).stateUpdated(any(), any());
        handler.initialize();
        nextRead().complete(snapshot(2));
        assertTrue(publishing.await(10, TimeUnit.SECONDS));
        ExecutorService disposer = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "candy-test-dispose");
            thread.setDaemon(true);
            return thread;
        });
        try {
            var disposed = disposer.submit(handler::dispose);
            assertTrue(cleanupEnqueued.await(10, TimeUnit.SECONDS));
            release.countDown();
            disposed.get(10, TimeUnit.SECONDS);
            clearInvocations(callback);
            handler.handleCommand(channel(), RefreshType.REFRESH);
            flush();
            verifyNoInteractions(callback);
        } finally {
            release.countDown();
            disposer.shutdownNow();
            assertTrue(disposer.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void reentrantDisposalDoesNotDeadlockOrRestoreOnline() throws Exception {
        doAnswer(invocation -> {
            if (new StringType("RUNNING").equals(invocation.getArgument(1))) {
                handler.dispose();
            }
            return null;
        }).when(callback).stateUpdated(any(), any());
        handler.initialize();
        nextRead().complete(snapshot(2));
        flush();
        assertNotEquals(ThingStatus.ONLINE, status.get().getStatus());
        verify(executors.getFirst()).shutdown();
    }

    @Test
    void disposalDuringInitializationCannotInstallANewSession() throws Exception {
        blockInitialization.set(true);
        ExecutorService initializer = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "candy-test-initialize");
            thread.setDaemon(true);
            return thread;
        });
        try {
            var initialized = initializer.submit(handler::initialize);
            assertTrue(preparingSession.await(10, TimeUnit.SECONDS));
            handler.dispose();
            continueInitialization.countDown();
            initialized.get(10, TimeUnit.SECONDS);
            flush();
            verifyNoInteractions(client, callback);
            verify(executors.getFirst()).shutdown();
        } finally {
            continueInitialization.countDown();
            initializer.shutdownNow();
            assertTrue(initializer.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void disposalFromAnInitialChannelCallbackPreventsPolling() throws Exception {
        doAnswer(invocation -> {
            handler.dispose();
            return null;
        }).when(callback).stateUpdated(eq(new ChannelUID(handler.getThing().getUID(), CHANNEL_LAST_UPDATE)),
                eq(UnDefType.UNDEF));
        handler.initialize();
        flush();
        verifyNoInteractions(client);
        verify(callback, never()).statusUpdated(any(), any());
        verify(executors.getFirst()).shutdown();
    }

    @Test
    void nestedHandlerExecutionPreservesReentrantDisposalContext() throws Exception {
        ScheduledExecutorService inlineExecutor = mock(ScheduledExecutorService.class);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(inlineExecutor).execute(any(Runnable.class));
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return CompletableFuture.completedFuture(null);
        }).when(inlineExecutor).submit(any(Runnable.class));
        BiancaHandler nested = new BiancaHandler(thing("other.local", "ponmlkjihgfedcba"), client,
                () -> accountClient) {
            @Override
            ScheduledExecutorService createExecutor() {
                return inlineExecutor;
            }
        };
        nested.setCallback(mock(ThingHandlerCallback.class));
        doAnswer(invocation -> {
            if (new StringType("RUNNING").equals(invocation.getArgument(1))) {
                nested.initialize();
                handler.dispose();
            }
            return null;
        }).when(callback).stateUpdated(any(), any());
        try {
            handler.initialize();
            nextRead().complete(snapshot(2));
            flush();
            verify(executors.getFirst(), never()).submit(any(Runnable.class));
            verify(executors.getFirst()).shutdown();
            assertNotEquals(ThingStatus.ONLINE, status.get().getStatus());
        } finally {
            nested.dispose();
        }
    }

    @Test
    void controlIsDisabledByDefaultAndOffCommandsNeverStartAnything() throws Exception {
        handler.initialize();
        nextRead().complete(snapshot(1));
        flush();
        send(CHANNEL_STOP, OnOffType.ON);
        assertEquals(new StringType("DISABLED"), states.get(CHANNEL_LAST_COMMAND_RESULT));
        send(CHANNEL_START, OnOffType.OFF);
        assertEquals(new StringType("REJECTED"), states.get(CHANNEL_LAST_COMMAND_RESULT));
        verify(client, never()).write(any(), anyString());
        verify(client, times(1)).read(any());
    }

    @ParameterizedTest
    @CsvSource({ "1,65,1,65", "1,65,01,065", "01,065,1,65", "01,065,001,00065", "01,65,1,065", "1,065,01,65",
            "1,1,01,001", "255,255,0255,00255" })
    void startsOnlyAfterFreshPermissionAndConfirmsMatchingProgramFromStatus(String selector, String code,
            String reportedSelector, String reportedCode) throws Exception {
        prepareControls(selector, code);
        send(CHANNEL_SELECTED_PROGRAM, new StringType("cottons"));
        verify(client, never()).write(any(), anyString());
        send(CHANNEL_START, OnOffType.ON);
        assertEquals(new StringType("PENDING"), states.get(CHANNEL_LAST_COMMAND_RESULT));
        runTimer();
        nextRead().complete(controlSnapshot(1, 1));
        flush();
        var write = writes.poll(10, TimeUnit.SECONDS);
        assertNotNull(write);
        write.complete(200);
        flush();
        assertEquals(new StringType("SENT"), states.get(CHANNEL_LAST_COMMAND_RESULT));
        assertEquals(new StringType("SELECTING"), states.get(CHANNEL_MACHINE_STATE));
        runTimer();
        nextRead().complete(runningProgramSnapshot(reportedSelector, reportedCode));
        flush();
        assertEquals(new StringType("CONFIRMED"), states.get(CHANNEL_LAST_COMMAND_RESULT));
        assertEquals(new StringType(reportedSelector), states.get(CHANNEL_PROGRAM_NUMBER));
        assertEquals(new StringType(reportedCode), states.get(CHANNEL_PROGRAM_CODE));
        verify(client, times(1)).write(any(),
                contains("&PrNm=" + Integer.parseInt(selector) + "&PrCode=" + Integer.parseInt(code) + "&"));
    }

    @ParameterizedTest
    @CsvSource({ "2,65", "1,66", "0,65", "256,65", "-1,65", "unknown,65", "2147483648,65", "'',65", "1,0", "1,256",
            "1,-65", "1,unknown", "1,2147483648", "1,''" })
    void doesNotConfirmStartWithMismatchedOrInvalidProgramCodes(String reportedSelector, String reportedCode)
            throws Exception {
        prepareControls();
        send(CHANNEL_START_PROGRAM, new StringType("cottons"));
        runTimer();
        nextRead().complete(controlSnapshot(1, 1));
        flush();
        var write = writes.poll(10, TimeUnit.SECONDS);
        assertNotNull(write);
        write.complete(200);
        flush();
        assertEquals(new StringType("SENT"), states.get(CHANNEL_LAST_COMMAND_RESULT));

        runTimer();
        nextRead().complete(runningProgramSnapshot(reportedSelector, reportedCode));
        flush();
        assertEquals(new StringType("SENT"), states.get(CHANNEL_LAST_COMMAND_RESULT));
        Timer expiry = allTimers.stream().filter(task -> task.seconds() == 60).findFirst().orElseThrow();
        expiry.runnable().run();
        flush();
        assertEquals(new StringType("UNKNOWN"), states.get(CHANNEL_LAST_COMMAND_RESULT));
        verify(client, times(1)).write(any(), anyString());
    }

    @ParameterizedTest
    @CsvSource({ "1,0,REMOTE_DISABLED", "2,1,NOT_READY", "0,1,NOT_READY" })
    void freshDeviceStateCanRejectStart(int mode, int remote, String result) throws Exception {
        prepareControls();
        send(CHANNEL_START_PROGRAM, new StringType("cottons"));
        runTimer();
        nextRead().complete(controlSnapshot(mode, remote));
        flush();
        assertEquals(new StringType(result), states.get(CHANNEL_LAST_COMMAND_RESULT));
        verify(client, never()).write(any(), anyString());
    }

    @Test
    void ambiguousWriteIsNotReplayedAndConfirmationExpires() throws Exception {
        prepareControls();
        send(CHANNEL_START_PROGRAM, new StringType("cottons"));
        send(CHANNEL_START_PROGRAM, new StringType("cottons"));
        assertEquals(new StringType("BUSY"), states.get(CHANNEL_LAST_COMMAND_RESULT));
        runTimer();
        nextRead().complete(controlSnapshot(1, 1));
        flush();
        var write = writes.poll(10, TimeUnit.SECONDS);
        assertNotNull(write);
        write.completeExceptionally(new CandyProtocolException(TIMEOUT));
        flush();
        assertEquals(new StringType("UNKNOWN"), states.get(CHANNEL_LAST_COMMAND_RESULT));
        Timer expiry = allTimers.stream().filter(task -> task.seconds() == 60).findFirst().orElseThrow();
        runTimer();
        nextRead().complete(controlSnapshot(1, 1));
        flush();
        expiry.runnable().run();
        flush();
        assertEquals(new StringType("UNKNOWN"), states.get(CHANNEL_LAST_COMMAND_RESULT));
        verify(client, times(1)).write(any(), anyString());
    }

    @Test
    void disposalCancelsAnActiveWriteAndDoesNotPublishLateResults() throws Exception {
        prepareControls();
        send(CHANNEL_STOP, OnOffType.ON);
        runTimer();
        nextRead().complete(controlSnapshot(5, 1));
        flush();
        var write = writes.poll(10, TimeUnit.SECONDS);
        assertNotNull(write);
        verify(client).write(any(), eq("Write=1&StSt=0&PrNm=1&DelVl=0"));
        handler.dispose();
        assertTrue(write.isCancelled());
        clearInvocations(callback);
        write.complete(200);
        flush();
        verifyNoInteractions(callback);
    }

    @Test
    void catalogFailureDoesNotInterruptLocalMonitoringAndRefreshIsRateLimited() throws Exception {
        handler.thingUpdated(controlThing());
        nextRead().complete(controlSnapshot(1, 1));
        var request = nextCatalogRead();
        request.completeExceptionally(new CandyProtocolException(TRANSPORT));
        flush();
        assertEquals(ThingStatus.ONLINE, status.get().getStatus());
        assertEquals(new StringType("FAILED"), states.get(CHANNEL_CATALOG_STATUS));
        send(CHANNEL_PROGRAM_CATALOG, new StringType("LIST"));
        assertEquals(new StringType("RATE_LIMITED"), states.get(CHANNEL_CATALOG_STATUS));
        verify(accountClient, times(1)).fetchPrograms(any(), anyString());
        clock.addAndGet(TimeUnit.MINUTES.toNanos(5));
        send(CHANNEL_PROGRAM_CATALOG, RefreshType.REFRESH);
        verify(accountClient, times(2)).fetchPrograms(any(), anyString());
        handler.dispose();
        var cancelled = catalogReads.poll();
        assertNotNull(cancelled);
        assertTrue(cancelled.isCancelled());
    }

    @Test
    void registersPerThingActions() {
        assertTrue(handler.getServices().contains(CandyActions.class));
    }

    @Test
    void actionStartUsesFreshPreflightAndDeviceConfirmation() throws Exception {
        prepareControls();
        assertTrue(handler.startProgram("cottons"));
        flush();
        assertEquals("PENDING", handler.getLastCommandResult());
        verify(client, never()).write(any(), anyString());
        runTimer();
        nextRead().complete(controlSnapshot(1, 1));
        flush();
        var write = writes.poll(10, TimeUnit.SECONDS);
        assertNotNull(write);
        write.complete(200);
        flush();
        assertEquals("SENT", handler.getLastCommandResult());
        runTimer();
        nextRead().complete(controlSnapshot(2, 1));
        flush();
        assertEquals("CONFIRMED", handler.getLastCommandResult());
        verify(client, times(1)).write(any(), contains("&StSt=1&"));
    }

    @Test
    void actionStopCancelsADelayedCycleAndConfirmsIt() throws Exception {
        prepareControls();
        handler.stopProgram();
        flush();
        runTimer();
        nextRead().complete(controlSnapshot(5, 1));
        flush();
        var write = writes.poll(10, TimeUnit.SECONDS);
        assertNotNull(write);
        write.complete(200);
        flush();
        assertEquals("SENT", handler.getLastCommandResult());
        runTimer();
        nextRead().complete(controlSnapshot(1, 1));
        flush();
        assertEquals("CONFIRMED", handler.getLastCommandResult());
        verify(client, times(1)).write(any(), eq("Write=1&StSt=0&PrNm=1&DelVl=0"));
    }

    @Test
    void actionsRespectDisabledControlsAndForgetDisposedSessions() throws Exception {
        handler.initialize();
        nextRead().complete(snapshot(1));
        flush();
        handler.startProgram("cottons");
        flush();
        assertEquals("DISABLED", handler.getLastCommandResult());
        handler.stopProgram();
        flush();
        assertEquals("DISABLED", handler.getLastCommandResult());
        handler.dispose();
        clearInvocations(client, accountClient);
        assertFalse(handler.startProgram("cottons"));
        assertFalse(handler.stopProgram());
        assertFalse(handler.refreshPrograms());
        flush();
        assertEquals(List.of(), handler.getPrograms());
        assertEquals("NOT_READY", handler.getCatalogStatus());
        assertEquals("NOT_READY", handler.getLastCommandResult());
        verifyNoInteractions(client, accountClient);
    }

    @Test
    void actionCatalogQueriesRetainUsableDataOnFailureAndResetAfterReconfiguration() throws Exception {
        prepareControls();
        var programs = handler.getPrograms();
        assertEquals(1, programs.size());
        assertEquals("cottons", programs.getFirst().id());
        assertEquals("Cottons", programs.getFirst().label());
        assertTrue(programs.getFirst().startable());
        assertEquals("READY", handler.getCatalogStatus());
        verify(accountClient, times(1)).fetchPrograms(any(), anyString());
        handler.refreshPrograms();
        flush();
        assertEquals("RATE_LIMITED", handler.getCatalogStatus());
        clock.addAndGet(TimeUnit.MINUTES.toNanos(5));
        handler.refreshPrograms();
        flush();
        assertEquals("LOADING", handler.getCatalogStatus());
        var request = nextCatalogRead();
        request.completeExceptionally(new CandyProtocolException(TRANSPORT));
        flush();
        assertEquals("FAILED", handler.getCatalogStatus());
        assertEquals(programs, handler.getPrograms());
        handler.thingUpdated(controlThing());
        assertEquals(List.of(), handler.getPrograms());
        assertEquals("NOT_READY", handler.getCatalogStatus());
        assertEquals("NOT_READY", handler.getLastCommandResult());
    }

    @Test
    void failedActionPreflightUpdatesTheResultSnapshotWithoutAWrite() throws Exception {
        prepareControls();
        handler.startProgram("cottons");
        flush();
        runTimer();
        nextRead().completeExceptionally(new CandyProtocolException(TRANSPORT));
        flush();
        assertEquals("FAILED", handler.getLastCommandResult());
        verify(client, never()).write(any(), anyString());
    }

    private void prepareControls() throws Exception {
        prepareControls("1", "65");
    }

    private void prepareControls(String selector, String code) throws Exception {
        handler.thingUpdated(controlThing());
        nextRead().complete(controlSnapshot(1, 1));
        var request = nextCatalogRead();
        Map<String, String> parameters = Map.of("selector_position", selector, "pr_code", code, "default_temperature",
                "60", "default_spin_speed", "1000", "minimum_soil_level", "1", "maximum_soil_level", "3",
                "default_soil_level", "3", "steam", "5", "dry", "0");
        request.complete(List.of(new Program("cottons", "Cottons", parameters,
                Optional.of(BiancaCommands.start("Cottons", parameters)))));
        flush();
    }

    private Thing controlThing() {
        return ThingBuilder.create(THING_TYPE_BIANCA, "test")
                .withConfiguration(
                        new Configuration(Map.of("hostName", "washer.local", "encryptionKey", "abcdefghijklmnop",
                                "username", "owner@example.test", "password", "dummy-password", "enableControl", true)))
                .build();
    }

    private BiancaSnapshot controlSnapshot(int mode, int remote) throws CandyProtocolException {
        return new BiancaStatusParser().parse("{\"statusLavatrice\":{\"MachMd\":" + mode + ",\"WiFiStatus\":" + remote
                + ",\"PrNm\":1,\"PrCode\":65}}");
    }

    private BiancaSnapshot runningProgramSnapshot(String selector, String code) throws CandyProtocolException {
        return new BiancaStatusParser().parse("""
                {"statusLavatrice":{"MachMd":2,"WiFiStatus":1,"PrNm":"%s","PrCode":"%s"}}
                """.formatted(selector, code));
    }

    private void send(String channel, org.openhab.core.types.Command command) throws Exception {
        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), channel), command);
        flush();
    }

    private ScheduledExecutorService executor() {
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        executors.add(executor);
        doAnswer(invocation -> {
            worker.execute(invocation.getArgument(0));
            return null;
        }).when(executor).execute(any());
        doAnswer(invocation -> {
            ScheduledFuture<?> future = mock(ScheduledFuture.class);
            Timer scheduled = new Timer(invocation.getArgument(0), invocation.getArgument(1),
                    invocation.getArgument(2));
            timer.set(scheduled);
            allTimers.add(scheduled);
            return future;
        }).when(executor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        doAnswer(invocation -> {
            cleanupEnqueued.countDown();
            return worker.submit((Runnable) invocation.getArgument(0));
        }).when(executor).submit(any(Runnable.class));
        return executor;
    }

    private CompletableFuture<BiancaSnapshot> nextRead() throws Exception {
        var request = reads.poll(10, TimeUnit.SECONDS);
        assertNotNull(request);
        flush();
        return request;
    }

    private CompletableFuture<List<String>> nextAccountRead() throws Exception {
        var request = accountReads.poll(10, TimeUnit.SECONDS);
        assertNotNull(request);
        flush();
        return request;
    }

    private CompletableFuture<List<Program>> nextCatalogRead() throws Exception {
        var request = catalogReads.poll(10, TimeUnit.SECONDS);
        assertNotNull(request);
        flush();
        return request;
    }

    private Thing accountThing() {
        return ThingBuilder.create(THING_TYPE_BIANCA, "test").withConfiguration(new Configuration(
                Map.of("hostName", "washer.local", "username", "owner@example.test", "password", "dummy-password")))
                .build();
    }

    private void flush() throws Exception {
        worker.submit(() -> {
        }).get(10, TimeUnit.SECONDS);
    }

    private void runTimer() throws Exception {
        Timer scheduled = timer.get();
        clock.addAndGet(scheduled.unit().toNanos(scheduled.delay()));
        scheduled.runnable().run();
        flush();
    }

    private ChannelUID channel() {
        return new ChannelUID(handler.getThing().getUID(), CHANNEL_MACHINE_STATE);
    }

    private BiancaSnapshot snapshot(int mode) throws CandyProtocolException {
        return new BiancaStatusParser().parse("{\"statusLavatrice\":{\"MachMd\":" + mode + "}}");
    }

    private Thing thing(String host, String key) {
        return ThingBuilder.create(THING_TYPE_BIANCA, "test")
                .withConfiguration(new Configuration(Map.of("hostName", host, "encryptionKey", key))).build();
    }

    private record Timer(Runnable runnable, long delay, TimeUnit unit) {
        private long seconds() {
            return unit.toSeconds(delay);
        }
    }
}
