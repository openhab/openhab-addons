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

import static org.openhab.binding.candy.internal.CandyBindingConstants.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.candy.internal.BiancaConfiguration;
import org.openhab.binding.candy.internal.CandyActions;
import org.openhab.binding.candy.internal.client.BiancaAccountClient;
import org.openhab.binding.candy.internal.client.BiancaLocalClient;
import org.openhab.binding.candy.internal.protocol.BiancaCommands;
import org.openhab.binding.candy.internal.protocol.BiancaProgramCatalog;
import org.openhab.binding.candy.internal.protocol.BiancaProgramCatalog.Program;
import org.openhab.binding.candy.internal.protocol.BiancaProgramCatalog.ProgramOption;
import org.openhab.binding.candy.internal.protocol.BiancaSnapshot;
import org.openhab.binding.candy.internal.protocol.CandyApplianceSelectionException;
import org.openhab.binding.candy.internal.protocol.CandyAuthenticationException;
import org.openhab.binding.candy.internal.protocol.CandyInvalidHostException;
import org.openhab.binding.candy.internal.protocol.CandyProtocolException;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinates Bianca polling, program catalogs and optional commands without blocking on network I/O.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class BiancaHandler extends BaseThingHandler {
    private static final List<String> SESSION_CHANNELS = Stream.concat(LIVE_CHANNELS.stream(), Stream
            .of(CHANNEL_PROGRAM_CATALOG, CHANNEL_CATALOG_STATUS, CHANNEL_SELECTED_PROGRAM, CHANNEL_LAST_COMMAND_RESULT))
            .toList();
    private static final ThreadLocal<@Nullable Session> EXECUTING_SESSION = new ThreadLocal<>();

    private final Logger logger = LoggerFactory.getLogger(BiancaHandler.class);
    private final BiancaLocalClient client;
    private final LongSupplier monotonicClock;
    private final Supplier<BiancaAccountClient> accountClientFactory;
    private final Consumer<List<Program>> programOptions;
    // Session installation and invalidation are atomic; framework callbacks never run under this lock.
    private final Object lifecycleLock = new Object();
    private final AtomicReference<@Nullable Session> session = new AtomicReference<>();
    private long lifecycleGeneration;

    public BiancaHandler(Thing thing, BiancaLocalClient client, Supplier<BiancaAccountClient> accountClientFactory) {
        this(thing, client, System::nanoTime, accountClientFactory);
    }

    public BiancaHandler(Thing thing, BiancaLocalClient client, Supplier<BiancaAccountClient> accountClientFactory,
            Consumer<List<Program>> programOptions) {
        this(thing, client, System::nanoTime, accountClientFactory, programOptions);
    }

    BiancaHandler(Thing thing, BiancaLocalClient client, LongSupplier monotonicClock,
            Supplier<BiancaAccountClient> accountClientFactory) {
        this(thing, client, monotonicClock, accountClientFactory, programs -> {
        });
    }

    private BiancaHandler(Thing thing, BiancaLocalClient client, LongSupplier monotonicClock,
            Supplier<BiancaAccountClient> accountClientFactory, Consumer<List<Program>> programOptions) {
        super(thing);
        this.programOptions = programOptions;
        this.client = client;
        this.monotonicClock = monotonicClock;
        this.accountClientFactory = accountClientFactory;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Candy Thing {}", getThing().getUID());
        Session previous;
        long generation;
        synchronized (lifecycleLock) {
            generation = ++lifecycleGeneration;
            previous = session.getAndSet(null);
        }
        stopSession(previous);
        BiancaConfiguration configuration = getConfigAs(BiancaConfiguration.class);
        Optional<String> error = configuration.validate();
        if (error.isPresent()) {
            logger.debug("Rejected configuration for {} ({})", getThing().getUID(), error.get());
            reportConfigurationError(generation, error.get());
            return;
        }
        Session current = new Session(configuration, createExecutor());
        boolean installed;
        synchronized (lifecycleLock) {
            installed = lifecycleGeneration == generation;
            if (installed) {
                session.set(current);
            }
        }
        if (!installed) {
            logger.debug("Discarding initialization for {} after its lifecycle changed", getThing().getUID());
            current.executor.shutdown();
            return;
        }
        logger.debug("Starting {} using {} (refresh {} s, local timeout {} s)", getThing().getUID(),
                configuration.encryptionKey.isEmpty() ? "account credentials" : "a manual appliance key",
                configuration.refreshInterval, configuration.requestTimeout);
        execute(current, () -> {
            for (String channel : SESSION_CHANNELS) {
                setState(current, channel, UnDefType.UNDEF);
            }
            if (isCurrent(current)) {
                updateState(CHANNEL_LAST_UPDATE, UnDefType.UNDEF);
            }
            if (isCurrent(current)) {
                updateStatus(ThingStatus.UNKNOWN);
                poll(current);
            }
        });
    }

    private void reportConfigurationError(long generation, String error) {
        for (String channel : SESSION_CHANNELS) {
            if (!isCurrentGeneration(generation)) {
                return;
            }
            updateState(channel, UnDefType.UNDEF);
        }
        if (!isCurrentGeneration(generation)) {
            return;
        }
        updateState(CHANNEL_LAST_UPDATE, UnDefType.UNDEF);
        if (isCurrentGeneration(generation)) {
            updateOfflineStatus(ThingStatusDetail.CONFIGURATION_ERROR, error);
        }
    }

    private boolean isCurrentGeneration(long generation) {
        synchronized (lifecycleLock) {
            return lifecycleGeneration == generation;
        }
    }

    ScheduledExecutorService createExecutor() {
        return ThreadPoolManager.getPoolBasedSequentialScheduledExecutorService("candy",
                "candy-" + getThing().getUID());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(CandyActions.class);
    }

    /** Returns public program metadata from the current session without performing network I/O. */
    public List<ProgramOption> getPrograms() {
        Session current = session.get();
        return current == null ? List.of() : BiancaProgramCatalog.options(current.programs);
    }

    public String getCatalogStatus() {
        Session current = session.get();
        return current == null ? "NOT_READY" : current.catalogStatus;
    }

    public String getLastCommandResult() {
        Session current = session.get();
        return current == null ? "NOT_READY" : current.lastCommandResult;
    }

    /** Queues catalog work before readiness and rate-limit checks; true acknowledges submission only. */
    public boolean refreshPrograms() {
        Session current = session.get();
        return current != null && submit(current, () -> loadCatalog(current));
    }

    /** Queues start validation; true does not indicate device acceptance or a confirmed wash. */
    public boolean startProgram(String programId) {
        Session current = session.get();
        return current != null
                && submit(current, () -> control(current, CHANNEL_START_PROGRAM, new StringType(programId)));
    }

    /** Queues stop validation; true does not indicate device acceptance or a confirmed stop. */
    public boolean stopProgram() {
        Session current = session.get();
        return current != null && submit(current, () -> control(current, CHANNEL_STOP, OnOffType.ON));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();
        Session active = session.get();
        if (CHANNEL_PROGRAM_CATALOG.equals(channel)) {
            if (active != null && (command == RefreshType.REFRESH
                    || command instanceof StringType && "LIST".equalsIgnoreCase(command.toString()))) {
                execute(active, () -> loadCatalog(active));
            }
            return;
        }
        if (List.of(CHANNEL_SELECTED_PROGRAM, CHANNEL_START, CHANNEL_START_PROGRAM, CHANNEL_STOP).contains(channel)) {
            if (active != null && command != RefreshType.REFRESH) {
                execute(active, () -> control(active, channel, command));
            }
            return;
        }
        if (command == RefreshType.REFRESH
                && (LIVE_CHANNELS.contains(channelUID.getId()) || CHANNEL_LAST_UPDATE.equals(channelUID.getId()))) {
            Session current = session.get();
            if (current != null && current.refreshQueued.compareAndSet(false, true)) {
                execute(current, () -> {
                    current.refreshQueued.set(false);
                    if (current.request != null) {
                        if (!current.refreshPending) {
                            logger.debug("Coalescing refresh for {} while a read is active", getThing().getUID());
                        }
                        current.refreshPending = true;
                    } else {
                        logger.debug("Processing explicit refresh for {}", getThing().getUID());
                        cancelTimer(current);
                        poll(current);
                    }
                });
            } else if (current == null) {
                logger.debug("Ignoring refresh for inactive Thing {}", getThing().getUID());
            }
        } else {
            logger.debug("Ignoring command for read-only Candy channel {}", channelUID);
        }
    }

    private void poll(Session current) {
        if (!isCurrent(current) || current.request != null || current.writeRequest != null) {
            return;
        }
        long now = monotonicClock.getAsLong();
        long minimumDelay = TimeUnit.SECONDS.toNanos(15) - (now - current.lastReadStarted);
        if (current.hasRead && minimumDelay > 0) {
            logger.debug("Delaying read for {} by {} ms to maintain the minimum interval", getThing().getUID(),
                    TimeUnit.NANOSECONDS.toMillis(minimumDelay));
            cancelTimer(current);
            current.timer = current.executor.schedule(() -> execute(current, () -> poll(current)), minimumDelay,
                    TimeUnit.NANOSECONDS);
            return;
        }
        current.hasRead = true;
        current.lastReadStarted = now;
        CompletableFuture<BiancaSnapshot> request;
        try {
            request = read(current);
        } catch (RuntimeException e) {
            request = CompletableFuture.failedFuture(e);
        }
        current.request = request;
        request.whenComplete((snapshot, failure) -> execute(current, () -> {
            current.request = null;
            boolean retry = true;
            if (failure == null) {
                logger.debug("Local status read for {} succeeded after {} consecutive failures", getThing().getUID(),
                        current.failures);
                current.failures = 0;
                publish(current, snapshot);
                if (!isCurrent(current)) {
                    return;
                }
                if (!current.catalogRequested && !current.configuration.username.isBlank()) {
                    loadCatalog(current);
                }
                if (current.intent != null && current.confirmationTimer == null) {
                    if (dispatch(current, snapshot)) {
                        return;
                    }
                } else {
                    confirm(current, snapshot);
                }
            } else {
                current.failures = Math.min(6, current.failures + 1);
                logger.debug("Status read for {} failed (consecutive failures: {})", getThing().getUID(),
                        current.failures);
                invalidateChannels(current);
                if (current.intent != null && current.confirmationTimer == null) {
                    current.intent = null;
                    commandResult(current, "FAILED");
                }
                if (isCurrent(current)) {
                    retry = reportReadFailure(failure);
                }
            }
            if (isCurrent(current) && retry) {
                // Coalesced explicit refreshes get one follow-up read, never an unbounded queue.
                long delay = current.refreshPending ? 0 : current.confirmationTimer != null ? 15 : retryDelay(current);
                logger.debug("Scheduling next read for {} in {} s", getThing().getUID(), delay);
                current.refreshPending = false;
                current.timer = current.executor.schedule(() -> execute(current, () -> poll(current)), delay,
                        TimeUnit.SECONDS);
            }
        }));
    }

    private void setState(Session current, String channel, State value) {
        if (isCurrent(current)) {
            updateState(channel, value);
        }
    }

    private void loadCatalog(Session current) {
        if (!isCurrent(current) || current.catalogRequest != null) {
            return;
        }
        if (current.configuration.encryptionKey.isEmpty()) {
            catalogStatus(current, "NOT_READY");
            return;
        }
        if (current.configuration.username.isBlank() || current.configuration.password.isEmpty()) {
            current.catalogRequested = true;
            catalogStatus(current, "ACCOUNT_REQUIRED");
            return;
        }
        long now = monotonicClock.getAsLong();
        if (current.catalogRequested && now - current.lastCatalogAttempt < TimeUnit.MINUTES.toNanos(5)) {
            catalogStatus(current, "RATE_LIMITED");
            return;
        }
        current.catalogRequested = true;
        current.lastCatalogAttempt = now;
        catalogStatus(current, "LOADING");
        if (!isCurrent(current)) {
            return;
        }
        CompletableFuture<List<Program>> request;
        try {
            request = accountClientFactory.get().fetchPrograms(current.configuration,
                    current.configuration.encryptionKey);
        } catch (RuntimeException e) {
            request = CompletableFuture.failedFuture(e);
        }
        current.catalogRequest = request;
        request.whenComplete((programs, failure) -> execute(current, () -> {
            current.catalogRequest = null;
            if (failure != null) {
                logger.debug("Program catalog request failed for {} ({})", getThing().getUID(),
                        failure.getClass().getSimpleName());
                catalogStatus(current, "FAILED");
                return;
            }
            current.programs = List.copyOf(programs);
            if (isCurrent(current)) {
                programOptions.accept(current.programs);
            }
            setState(current, CHANNEL_PROGRAM_CATALOG, new StringType(BiancaProgramCatalog.json(current.programs)));
            catalogStatus(current, "READY");
            if (current.programs.stream().noneMatch(program -> program.id().equals(current.selectedProgram))) {
                current.selectedProgram = "";
                setState(current, CHANNEL_SELECTED_PROGRAM, UnDefType.UNDEF);
            }
        }));
    }

    private void control(Session current, String channel, Command command) {
        if (CHANNEL_SELECTED_PROGRAM.equals(channel) && command instanceof StringType) {
            Optional<Program> selected = program(current, command.toString());
            if (selected.isPresent()) {
                current.selectedProgram = selected.get().id();
                setState(current, channel, new StringType(current.selectedProgram));
            } else {
                commandResult(current, "INVALID_PROGRAM");
            }
            return;
        }
        boolean start = CHANNEL_START.equals(channel) || CHANNEL_START_PROGRAM.equals(channel);
        if (!(command == OnOffType.ON && (CHANNEL_START.equals(channel) || CHANNEL_STOP.equals(channel))
                || command instanceof StringType && CHANNEL_START_PROGRAM.equals(channel))) {
            commandResult(current, "REJECTED");
            return;
        }
        if (!current.configuration.enableControl) {
            commandResult(current, "DISABLED");
            return;
        }
        if (current.intent != null || current.request != null || current.writeRequest != null) {
            commandResult(current, "BUSY");
            return;
        }
        Optional<Program> selected = start
                ? program(current, CHANNEL_START_PROGRAM.equals(channel) ? command.toString() : current.selectedProgram)
                : Optional.empty();
        if (start && selected.isEmpty()) {
            commandResult(current, "INVALID_PROGRAM");
            return;
        }
        if (current.configuration.encryptionKey.isEmpty()) {
            commandResult(current, "NOT_READY");
            return;
        }
        current.intent = new CommandIntent(start, selected);
        commandResult(current, "PENDING");
        if (isCurrent(current)) {
            cancelTimer(current);
            // A fresh, serialized read checks physical remote permission and operating state before every write.
            poll(current);
        }
    }

    private Optional<Program> program(Session current, String id) {
        return current.programs.stream()
                .filter(program -> program.id().equals(id) && program.startPayload().isPresent()).findFirst();
    }

    private void catalogStatus(Session current, String value) {
        if (isCurrent(current)) {
            current.catalogStatus = value;
            setState(current, CHANNEL_CATALOG_STATUS, new StringType(value));
        }
    }

    private void commandResult(Session current, String value) {
        if (isCurrent(current)) {
            current.lastCommandResult = value;
        }
        logger.debug("Candy command result for {}: {}", getThing().getUID(), value);
        setState(current, CHANNEL_LAST_COMMAND_RESULT, new StringType(value));
    }

    private boolean dispatch(Session current, BiancaSnapshot snapshot) {
        CommandIntent intent = current.intent;
        if (intent == null || !isCurrent(current)) {
            return false;
        }
        String mode = value(snapshot, CHANNEL_MACHINE_STATE);
        if (!OnOffType.ON.equals(snapshot.states().get(CHANNEL_REMOTE_CONTROL_ENABLED))) {
            current.intent = null;
            commandResult(current, "REMOTE_DISABLED");
            return false;
        }
        if (!(intent.start() ? List.of("SELECTING", "FINISHED") : List.of("RUNNING", "PAUSED", "DELAYED"))
                .contains(mode)) {
            current.intent = null;
            commandResult(current, "NOT_READY");
            return false;
        }
        String payload;
        try {
            payload = intent.start() ? intent.program().orElseThrow().startPayload().orElseThrow()
                    : BiancaCommands.stop(value(snapshot, CHANNEL_PROGRAM_NUMBER), "DELAYED".equals(mode));
        } catch (CandyProtocolException e) {
            current.intent = null;
            commandResult(current, "REJECTED");
            return false;
        }
        if (!isCurrent(current)) {
            return false;
        }
        CompletableFuture<Integer> request;
        try {
            request = client.write(current.configuration, payload);
        } catch (RuntimeException e) {
            request = CompletableFuture.failedFuture(e);
        }
        current.writeRequest = request;
        request.whenComplete((ignored, failure) -> execute(current, () -> {
            current.writeRequest = null;
            commandResult(current, failure == null ? "SENT" : "UNKNOWN");
            if (isCurrent(current)) {
                current.confirmationTimer = current.executor.schedule(() -> execute(current, () -> {
                    current.confirmationTimer = null;
                    current.intent = null;
                    commandResult(current, "UNKNOWN");
                }), 60, TimeUnit.SECONDS);
                current.timer = current.executor.schedule(() -> execute(current, () -> poll(current)), 15,
                        TimeUnit.SECONDS);
            }
        }));
        return true;
    }

    private void confirm(Session current, BiancaSnapshot snapshot) {
        CommandIntent intent = current.intent;
        ScheduledFuture<?> confirmation = current.confirmationTimer;
        if (intent == null || confirmation == null || !isCurrent(current)) {
            return;
        }
        String mode = value(snapshot, CHANNEL_MACHINE_STATE);
        boolean confirmed;
        if (intent.start()) {
            Program program = intent.program().orElseThrow();
            confirmed = "RUNNING".equals(mode)
                    && program.parameters().getOrDefault("selector_position", "")
                            .equals(value(snapshot, CHANNEL_PROGRAM_NUMBER))
                    && program.parameters().getOrDefault("pr_code", "").equals(value(snapshot, CHANNEL_PROGRAM_CODE));
        } else {
            confirmed = List.of("SELECTING", "FINISHED").contains(mode);
        }
        if (confirmed) {
            confirmation.cancel(false);
            current.confirmationTimer = null;
            current.intent = null;
            commandResult(current, "CONFIRMED");
        }
    }

    private String value(BiancaSnapshot snapshot, String channel) {
        State state = snapshot.states().get(channel);
        return state == null || state == UnDefType.UNDEF ? "" : state.toString();
    }

    private record CommandIntent(boolean start, Optional<Program> program) {
    }

    private CompletableFuture<BiancaSnapshot> read(Session current) {
        if (!current.configuration.encryptionKey.isEmpty()) {
            logger.debug("Reading {} with its configured or previously matched key", getThing().getUID());
            return client.read(current.configuration);
        }
        if (!current.candidateKeys.isEmpty()) {
            logger.debug("Retrying local key matching for {} with {} cached candidates", getThing().getUID(),
                    current.candidateKeys.size());
            return client.read(current.configuration, current.candidateKeys);
        }
        logger.debug("Starting account key lookup for {}", getThing().getUID());
        BiancaAccountClient accountClient = current.accountClient;
        if (accountClient == null) {
            accountClient = accountClientFactory.get();
            current.accountClient = accountClient;
        }
        CompletableFuture<List<String>> keys = accountClient.fetchKeys(current.configuration);
        CompletableFuture<BiancaSnapshot> result = new CompletableFuture<>();
        AtomicReference<CompletableFuture<?>> active = new AtomicReference<>(keys);
        result.whenComplete((snapshot, failure) -> {
            if (result.isCancelled()) {
                logger.debug("Cancelling account setup or key matching for {}", getThing().getUID());
                active.get().cancel(true);
            }
        });
        keys.whenComplete((candidates, failure) -> {
            if (failure != null) {
                result.completeExceptionally(failure);
                return;
            }
            execute(current, () -> {
                if (result.isDone()) {
                    return;
                }
                current.candidateKeys = List.copyOf(candidates);
                logger.debug("Account lookup for {} returned {} candidates; matching against local status",
                        getThing().getUID(), current.candidateKeys.size());
                CompletableFuture<BiancaSnapshot> local;
                try {
                    current.lastReadStarted = monotonicClock.getAsLong();
                    local = client.read(current.configuration, current.candidateKeys);
                } catch (RuntimeException e) {
                    result.completeExceptionally(e);
                    return;
                }
                active.set(local);
                if (result.isCancelled()) {
                    local.cancel(true);
                    return;
                }
                local.whenComplete((snapshot, localFailure) -> {
                    if (localFailure == null) {
                        result.complete(snapshot);
                    } else {
                        result.completeExceptionally(localFailure);
                    }
                });
            });
        });
        return result;
    }

    private boolean reportReadFailure(Throwable failure) {
        Throwable cause = failure.getCause();
        while ((failure instanceof CompletionException || failure instanceof ExecutionException) && cause != null) {
            failure = cause;
            cause = failure.getCause();
        }
        if (failure instanceof CandyProtocolException exception) {
            boolean configurationError = exception instanceof CandyInvalidHostException
                    || exception instanceof CandyAuthenticationException
                    || exception instanceof CandyApplianceSelectionException;
            logger.debug("Setting {} offline after {} (automatic retry: {})", getThing().getUID(),
                    exception.getClass().getSimpleName(), !configurationError);
            updateOfflineStatus(
                    configurationError ? ThingStatusDetail.CONFIGURATION_ERROR : ThingStatusDetail.COMMUNICATION_ERROR,
                    Objects.requireNonNull(exception.getMessage()));
            return !configurationError;
        }
        logger.warn("Unexpected Candy status read failure for {} ({})", getThing().getUID(),
                failure.getClass().getSimpleName());
        updateOfflineStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/communication-error");
        return true;
    }

    @SuppressWarnings("removal")
    private void updateOfflineStatus(ThingStatusDetail detail, String description) {
        // The detail-only overload is absent from openHAB 5.3.0-M3.
        updateStatus(ThingStatus.OFFLINE, detail, description);
    }

    private long retryDelay(Session current) {
        long interval = current.configuration.refreshInterval;
        if (current.configuration.encryptionKey.isEmpty() && current.candidateKeys.isEmpty()) {
            interval = Math.max(300, interval);
        }
        return current.failures == 0 ? interval
                : Math.max(interval, Math.min(300L, interval * (1L << (current.failures - 1))));
    }

    private void publish(Session current, BiancaSnapshot snapshot) {
        for (Map.Entry<String, State> entry : snapshot.states().entrySet()) {
            if (!isCurrent(current)) {
                return;
            }
            updateState(entry.getKey(), entry.getValue());
        }
        if (isCurrent(current)) {
            updateState(CHANNEL_LAST_UPDATE, new DateTimeType());
        }
        if (isCurrent(current)) {
            logger.debug("Publishing {} channel values and setting {} online", snapshot.states().size(),
                    getThing().getUID());
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void invalidateChannels(Session current) {
        for (String channel : LIVE_CHANNELS) {
            if (!isCurrent(current)) {
                return;
            }
            updateState(channel, UnDefType.UNDEF);
        }
    }

    private boolean isCurrent(Session current) {
        return current.equals(session.get());
    }

    private void execute(Session current, Runnable task) {
        submit(current, task);
    }

    private boolean submit(Session current, Runnable task) {
        if (isCurrent(current)) {
            try {
                current.executor.execute(() -> {
                    // Inline execution can enter this session from another handler on the same thread.
                    Session previous = EXECUTING_SESSION.get();
                    EXECUTING_SESSION.set(current);
                    try {
                        if (isCurrent(current)) {
                            task.run();
                        }
                    } finally {
                        if (previous == null) {
                            EXECUTING_SESSION.remove();
                        } else {
                            EXECUTING_SESSION.set(previous);
                        }
                    }
                });
                return true;
            } catch (RejectedExecutionException e) {
                if (isCurrent(current)) {
                    logger.warn("Candy state executor rejected work for active Thing {}", getThing().getUID());
                    throw e;
                }
            }
        } else {
            logger.debug("Ignoring work from an obsolete session for {}", getThing().getUID());
        }
        return false;
    }

    private void cancelTimer(Session current) {
        ScheduledFuture<?> timer = current.timer;
        current.timer = null;
        if (timer != null) {
            timer.cancel(false);
        }
    }

    private void cancelWork(Session current) {
        logger.debug("Cancelling work for {} (scheduled read: {}, active read: {})", getThing().getUID(),
                current.timer != null, current.request != null);
        cancelTimer(current);
        CompletableFuture<List<Program>> catalogRequest = current.catalogRequest;
        current.catalogRequest = null;
        if (catalogRequest != null) {
            catalogRequest.cancel(true);
        }
        CompletableFuture<Integer> writeRequest = current.writeRequest;
        current.writeRequest = null;
        if (writeRequest != null) {
            writeRequest.cancel(true);
        }
        ScheduledFuture<?> confirmationTimer = current.confirmationTimer;
        current.confirmationTimer = null;
        if (confirmationTimer != null) {
            confirmationTimer.cancel(false);
        }
        current.intent = null;
        programOptions.accept(List.of());
        CompletableFuture<BiancaSnapshot> request = current.request;
        current.request = null;
        if (request != null) {
            request.cancel(true);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Candy Thing {}", getThing().getUID());
        Session previous;
        synchronized (lifecycleLock) {
            ++lifecycleGeneration;
            previous = session.getAndSet(null);
        }
        stopSession(previous);
    }

    private void stopSession(@Nullable Session previous) {
        if (previous != null) {
            try {
                if (previous.equals(EXECUTING_SESSION.get())) {
                    cancelWork(previous);
                } else {
                    // Drain state delivery before returning; no network operation runs on this executor.
                    previous.executor.submit(() -> cancelWork(previous)).get(5, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                logger.debug("Candy session cleanup interrupted for {}", getThing().getUID());
                Thread.currentThread().interrupt();
            } catch (ExecutionException | TimeoutException e) {
                logger.warn("Could not finish Candy state delivery during disposal of {}", getThing().getUID());
            } finally {
                previous.executor.shutdown();
            }
        }
    }

    // Each instance identifies one lifecycle and intentionally retains Object's identity equality.
    private static final class Session {
        private final BiancaConfiguration configuration;
        private final ScheduledExecutorService executor;
        private final AtomicBoolean refreshQueued = new AtomicBoolean();
        private @Nullable BiancaAccountClient accountClient;
        private List<String> candidateKeys = List.of();
        private @Nullable CompletableFuture<BiancaSnapshot> request;
        private @Nullable ScheduledFuture<?> timer;
        // Immutable snapshots are also read by rule actions outside the session executor.
        private volatile List<Program> programs = List.of();
        private volatile String catalogStatus = "NOT_READY";
        private volatile String lastCommandResult = "NOT_READY";
        private String selectedProgram = "";
        private boolean catalogRequested;
        private long lastCatalogAttempt;
        private @Nullable CompletableFuture<List<Program>> catalogRequest;
        private @Nullable CompletableFuture<Integer> writeRequest;
        private @Nullable ScheduledFuture<?> confirmationTimer;
        private @Nullable CommandIntent intent;
        private boolean refreshPending;
        private int failures;
        private boolean hasRead;
        private long lastReadStarted;

        private Session(BiancaConfiguration configuration, ScheduledExecutorService executor) {
            this.configuration = configuration;
            this.executor = executor;
        }
    }
}
