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
package org.openhab.binding.e3dc.internal.handler;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCChannelSpec;
import org.openhab.binding.e3dc.internal.E3DCContainerRequests;
import org.openhab.binding.e3dc.internal.E3DCDataListener;
import org.openhab.binding.e3dc.internal.config.E3DCBridgeConfiguration;
import org.openhab.binding.e3dc.internal.discovery.E3DCDiscoveryService;
import org.openhab.binding.e3dc.internal.rscp.RscpClient;
import org.openhab.binding.e3dc.internal.rscp.RscpError;
import org.openhab.binding.e3dc.internal.rscp.RscpException;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the connection to one E3/DC device, owning the RSCP connection and the poll cycle. Not every
 * device implements every data point: an unsupported tag is answered with an error item rather than a
 * failure, so such tags are counted and dropped after a few attempts.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCBridgeHandler extends BaseBridgeHandler {

    /** How many consecutive error responses a tag may produce before it is no longer requested. */
    private static final int UNSUPPORTED_TAG_THRESHOLD = 3;

    private static final int RECONNECT_DELAY_SECONDS = 30;

    /** Shortest distance between two polls that refresh commands asked for, so a burst becomes one poll. */
    private static final long REFRESH_MIN_INTERVAL_MILLIS = 1000;

    /** The tags requested on every poll, independent of which things exist. */
    private static final Set<Integer> POLL_TAGS = pollTags();

    /** Containers whose nested values the binding publishes, requested with no parameter. */
    private static final Set<Integer> POLL_CONTAINERS = pollContainers();

    /** History containers, which the device answers only when the request states the period it covers. */
    private static final Set<Integer> WINDOWED_CONTAINERS = Set.of(RscpTag.DB_HISTORY_DATA_DAY,
            RscpTag.DB_HISTORY_DATA_WEEK, RscpTag.DB_HISTORY_DATA_MONTH, RscpTag.DB_HISTORY_DATA_YEAR);

    /** The channel groups a thing needs a container request for, with the config key of the addressed device. */
    private static final Map<ThingTypeUID, List<GroupRequest>> GROUP_REQUESTS = Map.of(//
            E3DCBindingConstants.THING_TYPE_WALLBOX,
            List.of(new GroupRequest(E3DCBindingConstants.GROUP_WALLBOX, "wallboxIndex")), //
            E3DCBindingConstants.THING_TYPE_POWER_METER,
            List.of(new GroupRequest(E3DCBindingConstants.GROUP_METER, "meterIndex")), //
            E3DCBindingConstants.THING_TYPE_BATTERY,
            List.of(new GroupRequest(E3DCBindingConstants.GROUP_PACK, "batteryIndex")), //
            E3DCBindingConstants.THING_TYPE_BATTERY_MODULE,
            List.of(new GroupRequest(E3DCBindingConstants.GROUP_MODULE, "batteryIndex")), //
            E3DCBindingConstants.THING_TYPE_POWER_PLANT,
            List.of(new GroupRequest(E3DCBindingConstants.GROUP_INVERTER, null),
                    new GroupRequest(E3DCBindingConstants.GROUP_EMERGENCY, null)));

    private record GroupRequest(String groupId, @Nullable String indexConfigurationKey) {
    }

    private static Set<Integer> pollTags() {
        Set<Integer> tags = new LinkedHashSet<>();
        for (E3DCChannelSpec spec : allSpecs()) {
            int requestTag = RscpTag.pollTagFor(spec.tag());
            if (spec.container() == 0 && E3DCContainerRequests.forContainerTag(requestTag) == null
                    && !E3DCContainerRequests.isParameter(requestTag)) {
                tags.add(spec.tag());
            }
        }
        return tags;
    }

    private static Set<Integer> pollContainers() {
        Set<Integer> containers = new LinkedHashSet<>();
        for (E3DCChannelSpec spec : allSpecs()) {
            if (spec.container() != 0 && E3DCContainerRequests.forResponseTag(spec.container()) == null) {
                containers.add(spec.container());
            }
        }
        return containers;
    }

    private static List<E3DCChannelSpec> allSpecs() {
        List<E3DCChannelSpec> specs = new ArrayList<>();
        specs.addAll(E3DCChannelSpec.powerPlant().all());
        specs.addAll(E3DCChannelSpec.wallbox().all());
        specs.addAll(E3DCChannelSpec.powerMeter().all());
        specs.addAll(E3DCChannelSpec.battery().all());
        specs.addAll(E3DCChannelSpec.batteryModule().all());
        return specs;
    }

    /** Consecutive poll failures per tag; a tag is dropped once it reaches the threshold. */
    private final Map<Integer, Integer> tagFailures = new HashMap<>();

    /** Time of the last poll that carried the history containers, {@code 0} when none has been made yet. */
    private long lastHistoryPollMillis;

    /** The day, in UTC, the history was last requested for; a new day makes the history due at once. */
    private @Nullable LocalDate lastHistoryDay;

    /** Share of the poll interval the device may be occupied by this client, a working assumption. */
    private static final double LOOP_RATIO_TARGET = 0.2;

    /** Number of polls the loop ratio is summarised over. */
    private static final int LOOP_RATIO_WINDOW = 20;

    /** Busy time of the connection at the last poll, so the next poll can take the difference. */
    private long lastBusyNanos;

    private int windowPolls;
    private double windowRatioSum;
    private double windowRatioMax;
    private int windowAboveTarget;

    private final Logger logger = LoggerFactory.getLogger(E3DCBridgeHandler.class);
    private final List<E3DCDataListener> listeners = new CopyOnWriteArrayList<>();
    private final List<Runnable> onlineListeners = new CopyOnWriteArrayList<>();
    private final Set<Integer> droppedTags = new HashSet<>();

    /** The containers whose answer was logged since the connection came up, so debug shows each tree once. */
    private final Set<Integer> loggedAnswers = new HashSet<>();

    /** The tags whose error answer was logged and that still answer with an error. */
    private final Set<Integer> loggedErrors = new HashSet<>();

    private @Nullable E3DCBridgeConfiguration config;
    // read by the command path and the dispose while the poll replaces it, so it has to be visible to all of them
    private volatile @Nullable RscpClient client;

    /** The attempt under way, so a dispose can close it; until it finishes the client is not in {@link #client}. */
    private volatile @Nullable RscpClient pendingClient;

    /** Counts the connection attempts; closing raises it, so a stale attempt does not install its connection. */
    private final AtomicInteger connectionGeneration = new AtomicInteger();

    /** Guards {@link #pollJob}, which the poll thread, a refresh and the dispose all replace or cancel. */
    private final Object jobLock = new Object();

    private @Nullable ScheduledFuture<?> pollJob;

    /**
     * Serializes the connect and poll cycles. All state a poll keeps (dropped tags, logged answers, the history
     * bookkeeping and the loop ratio window) is only touched while this lock is held, so a scheduled poll, a
     * refresh and a reconnect can never run through it at the same time.
     */
    private final ReentrantLock pollLock = new ReentrantLock();

    private final TimeZoneProvider timeZoneProvider;

    /** Time the last refresh command started a poll, so a burst of refreshes becomes one poll. */
    private final AtomicLong lastRefreshMillis = new AtomicLong();

    /**
     * Creates the handler of the bridge.
     */
    public E3DCBridgeHandler(Bridge bridge, TimeZoneProvider timeZoneProvider) {
        super(bridge);
        this.timeZoneProvider = timeZoneProvider;
    }

    /**
     * The time zone the day, week, month and year boundaries of the history are taken in. The device counts its
     * periods in local time, so one zone is used for the request, for the day that makes the history due and for
     * the timestamps of the published series.
     */
    public ZoneId getZone() {
        return timeZoneProvider.getTimeZone();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(E3DCDiscoveryService.class);
    }

    @Override
    public void initialize() {
        // a re-initialization starts over: no job and no connection of the previous initialization may outlive it
        connectionGeneration.incrementAndGet();
        cancelPollJob();
        closeClient();
        config = getConfigAs(E3DCBridgeConfiguration.class);

        E3DCBridgeConfiguration current = config;
        if (current == null) {
            return;
        }
        // the updateStatus overloads with a status detail are deprecated since openHAB 5.3, but they are the only
        // ones openHAB 5.2 offers; keeping them lets the binding run on both versions
        if (current.host.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Host is not configured");
            return;
        }
        if (current.rscpPassword.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "RSCP password is not configured");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::connectAndPoll);
    }

    private void connectAndPoll() {
        pollLock.lock();
        try {
            connectAndPollLocked();
        } finally {
            pollLock.unlock();
        }
    }

    private void connectAndPollLocked() {
        E3DCBridgeConfiguration current = config;
        if (current == null || !isActive()) {
            return;
        }

        int generation = connectionGeneration.get();
        RscpClient newClient = new RscpClient(current.host, current.port, current.userName, current.password,
                current.rscpPassword, current.timeoutSeconds() * 1000);
        // a dispose during the blocking connect has to be able to close the socket this attempt has opened
        pendingClient = newClient;
        try {
            newClient.connect();
        } catch (RscpException e) {
            newClient.close();
            pendingClient = null;
            if (!isActive() || generation != connectionGeneration.get()) {
                // a dispose closed the socket of this attempt, which is no error of the device
                return;
            }
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            logger.debug("Cannot connect to {}:{}: {}", current.host, current.port, e.getMessage());
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            // without a scheduled retry the bridge would stay offline until the thing is re-enabled
            scheduleReconnect();
            return;
        }
        pendingClient = null;
        closeClient();
        client = newClient;
        if (!isActive() || generation != connectionGeneration.get()) {
            // the handler was disposed or initialized again while the connection was being established
            closeClient();
            return;
        }
        droppedTags.clear();
        loggedAnswers.clear();
        loggedErrors.clear();
        // a new connection starts with the history, so the channels are filled with the first poll
        lastHistoryPollMillis = 0;
        lastBusyNanos = newClient.getBusyNanos();
        updateStatus(ThingStatus.ONLINE);
        logger.debug("Connected to E3/DC device at {}:{}", current.host, current.port);
        for (Runnable listener : onlineListeners) {
            listener.run();
        }

        pollGuarded();
        // a first poll that lost the connection has already scheduled the next attempt
        if (isConnected()) {
            scheduleNextPoll();
        }
    }

    private void scheduleNextPoll() {
        E3DCBridgeConfiguration current = config;
        if (current == null) {
            return;
        }
        schedule(this::pollCycle, current.refreshIntervalSeconds());
    }

    private void scheduleReconnect() {
        schedule(this::connectAndPoll, RECONNECT_DELAY_SECONDS);
    }

    /** Replaces any pending job with the given one, so exactly one timer is ever outstanding. */
    private void schedule(Runnable task, long delaySeconds) {
        if (!isActive()) {
            return;
        }
        synchronized (jobLock) {
            cancelPollJob();
            pollJob = scheduler.schedule(task, delaySeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * Asks for an immediate poll when a channel receives a refresh. The work is queued on the scheduler rather
     * than run here, because a refresh arrives on a framework thread and the poll performs blocking socket I/O.
     */
    public void requestRefresh() {
        if (!isActive() || !isConnected()) {
            return;
        }
        long now = System.currentTimeMillis();
        long previous = lastRefreshMillis.get();
        if (now - previous < REFRESH_MIN_INTERVAL_MILLIS || !lastRefreshMillis.compareAndSet(previous, now)) {
            return;
        }
        scheduler.execute(() -> {
            // a poll that is running delivers fresh data anyway, so a refresh does not queue up behind it
            if (!pollLock.tryLock()) {
                logger.trace("Refresh skipped, a poll is already running");
                return;
            }
            try {
                if (isActive() && isConnected()) {
                    pollCycleLocked();
                }
            } finally {
                pollLock.unlock();
            }
        });
    }

    private void pollCycle() {
        pollLock.lock();
        try {
            pollCycleLocked();
        } finally {
            pollLock.unlock();
        }
    }

    private void pollCycleLocked() {
        if (!isActive()) {
            return;
        }
        if (!isConnected()) {
            // the connection was lost between two cycles, establish it again
            connectAndPoll();
            return;
        }
        pollGuarded();
        // the reconnect of a failed poll reports offline, so the status is only restored while connected
        if (isConnected() && getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        if (isConnected()) {
            scheduleNextPoll();
        }
        // otherwise the reconnect that the failed poll scheduled continues the cycle
    }

    /**
     * Polls once and treats an unexpected failure like a lost connection. A thread that dies on a defective
     * answer would leave the bridge online without any data, and after a defective frame the cipher chaining of
     * the connection cannot be trusted either, so the connection is closed and built up again.
     */
    private void pollGuarded() {
        try {
            pollOnce();
        } catch (RuntimeException e) {
            logger.warn("Unexpected problem during the poll, the connection is built up again", e);
            handleCommunicationFailure(new RscpException("Unexpected problem during the poll: " + e, e));
        }
    }

    private void pollOnce() {
        RscpClient current = client;
        if (current == null) {
            return;
        }
        try {
            long pollStarted = System.nanoTime();
            boolean withHistory = isHistoryDue();
            RscpValue response = current.request(buildRequest(withHistory));
            long answered = System.nanoTime();
            if (withHistory) {
                lastHistoryPollMillis = System.currentTimeMillis();
                lastHistoryDay = LocalDate.now(getZone());
            }
            reportTagSupport(response);
            logContainerAnswers(response);
            for (E3DCDataListener listener : listeners) {
                try {
                    listener.onData(response);
                } catch (RuntimeException e) {
                    logger.warn("A thing handler failed to process poll data", e);
                }
            }
            long dispatched = System.nanoTime();
            recordLoopRatio(current, withHistory, dispatched - answered);
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "Poll: history {}, answer {} ms, dispatch to {} listeners {} ms, "
                                + "weight of the answer by element: {}",
                        withHistory ? "included" : "skipped", (answered - pollStarted) / 1_000_000, listeners.size(),
                        (dispatched - answered) / 1_000_000, answerWeight(response));
            }
        } catch (RscpException e) {
            logger.debug("Poll failed: {}", e.getMessage());
            handleCommunicationFailure(e);
        }
    }

    /**
     * Records the loop ratio: the time the device was occupied by this client divided by the poll interval,
     * including commands and history fetches but not the dispatch to the thing handlers.
     */
    private void recordLoopRatio(RscpClient current, boolean withHistory, long dispatchNanos) {
        E3DCBridgeConfiguration currentConfig = config;
        if (currentConfig == null) {
            return;
        }
        long busy = current.getBusyNanos();
        long busyDelta = busy - lastBusyNanos;
        lastBusyNanos = busy;
        long intervalNanos = currentConfig.refreshIntervalSeconds() * 1_000_000_000L;
        double ratio = (double) busyDelta / intervalNanos;
        if (logger.isTraceEnabled()) {
            logger.trace("Loop ratio {} % (busy {} ms of {} ms interval, history {})",
                    String.format("%.1f", ratio * 100), busyDelta / 1_000_000, intervalNanos / 1_000_000,
                    withHistory ? "included" : "skipped");
        }
        updateStatistics(current, ratio, dispatchNanos);
        windowPolls++;
        windowRatioSum += ratio;
        windowRatioMax = Math.max(windowRatioMax, ratio);
        if (ratio > LOOP_RATIO_TARGET) {
            windowAboveTarget++;
        }
        if (windowPolls >= LOOP_RATIO_WINDOW) {
            updateWindowStatistics(windowRatioSum / windowPolls, windowRatioMax);
            logger.debug(
                    "Loop ratio over the last {} polls: average {} %, peak {} %, "
                            + "{} polls above the target of {} %",
                    windowPolls, String.format("%.1f", windowRatioSum / windowPolls * 100),
                    String.format("%.1f", windowRatioMax * 100), windowAboveTarget,
                    String.format("%.0f", LOOP_RATIO_TARGET * 100));
            windowPolls = 0;
            windowRatioSum = 0;
            windowRatioMax = 0;
            windowAboveTarget = 0;
        }
    }

    private void updateStatistics(RscpClient current, double ratio, long dispatchNanos) {
        publishStatistic("loop-ratio", new QuantityType<>(ratio * 100, Units.PERCENT));
        publishStatistic("poll-duration", milliseconds(current.getLastWireNanos()));
        publishStatistic("device-wait", milliseconds(current.getLastDeviceWaitNanos()));
        publishStatistic("dispatch-time", milliseconds(dispatchNanos));
        publishStatistic("answer-size", new QuantityType<>(current.getLastAnswerBytes(), Units.BYTE));
    }

    private void updateWindowStatistics(double average, double peak) {
        publishStatistic("loop-ratio-average", new QuantityType<>(average * 100, Units.PERCENT));
        publishStatistic("loop-ratio-peak", new QuantityType<>(peak * 100, Units.PERCENT));
    }

    private static QuantityType<?> milliseconds(long nanos) {
        return new QuantityType<>(nanos / 1_000_000.0, MetricPrefix.MILLI(Units.SECOND));
    }

    private void publishStatistic(String channelId, QuantityType<?> value) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), E3DCBindingConstants.GROUP_STATISTICS, channelId);
        if (isLinked(channelUID)) {
            updateState(channelUID, value);
        }
    }

    /** The size of the answer per top level element, in values, only used for the trace. */
    private static String answerWeight(RscpValue response) {
        List<RscpValue> elements = response.getTag() == 0 ? response.getChildren() : List.of(response);
        StringBuilder text = new StringBuilder();
        for (RscpValue element : elements) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(RscpTag.nameOf(element.getTag())).append('=').append(countValues(element));
        }
        return text.toString();
    }

    private static int countValues(RscpValue value) {
        int count = 1;
        for (RscpValue child : value.getChildren()) {
            count += countValues(child);
        }
        return count;
    }

    /** Logs the containers the parameterised requests were answered with, so a rejected request is visible. */
    private void logContainerAnswers(RscpValue response) {
        // the trees are long texts, so nothing is searched or built while debug logging is off
        if (!logger.isDebugEnabled()) {
            return;
        }
        for (E3DCContainerRequests.Container container : E3DCContainerRequests.all()) {
            // the trees are the same with every poll, so debug shows each one once per connection
            if (!logger.isTraceEnabled() && !loggedAnswers.add(container.responseTag())) {
                continue;
            }
            for (RscpValue answer : response.findAll(container.responseTag())) {
                logger.debug("Answer of {}: {}", RscpTag.nameOf(container.responseTag()), answer.describeTree());
            }
        }
    }

    /**
     * Builds the elements of one request: every root tag, the values nested in containers and one request
     * per container whose answer depends on the parameters of the request. A request has to carry the
     * matching {@code *_REQ_*} tag, because the device does not recognise a response tag in a request.
     */
    private List<RscpValue> buildRequest(boolean withHistory) {
        List<RscpValue> items = new ArrayList<>();
        for (int responseTag : POLL_TAGS) {
            if (!droppedTags.contains(responseTag)) {
                items.add(RscpValue.none(RscpTag.pollTagFor(responseTag)));
            }
        }
        for (int container : POLL_CONTAINERS) {
            if (WINDOWED_CONTAINERS.contains(container)) {
                // built separately below, its mandatory time parameters would be missing here
                continue;
            }
            int requestTag = RscpTag.pollTagFor(container);
            // the container itself is requested, its own tag acting as the requested data point
            items.add(RscpValue.container(requestTag, List.of(RscpValue.none(requestTag))));
        }
        for (int container : WINDOWED_CONTAINERS) {
            if (withHistory && POLL_CONTAINERS.contains(container)) {
                items.add(historyRequest(container));
            }
        }
        items.addAll(containerRequests());
        if (logger.isTraceEnabled()) {
            StringBuilder names = new StringBuilder();
            for (RscpValue item : items) {
                names.append(names.length() > 0 ? ", " : "").append(RscpTag.nameOf(item.getTag())).append('(')
                        .append(item.getChildren().size()).append(')');
            }
            logger.trace("Request of {} elements, name(children): {}", items.size(), names);
        }
        return items;
    }

    /** Whether this poll has to carry the history containers, due at the history interval and on a new local day. */
    private boolean isHistoryDue() {
        E3DCBridgeConfiguration current = config;
        if (current == null || lastHistoryPollMillis == 0) {
            return true;
        }
        if (!LocalDate.now(getZone()).equals(lastHistoryDay)) {
            return true;
        }
        return System.currentTimeMillis() - lastHistoryPollMillis >= current.historyIntervalSeconds() * 1000L;
    }

    /**
     * One request per container whose answer depends on the parameters of the request. Which containers are
     * needed follows from the things that exist; a container of a thing that exists more than once is requested
     * once per configured index.
     */
    private List<RscpValue> containerRequests() {
        Map<String, RscpValue> requests = new LinkedHashMap<>();
        // a module index counts inside its battery, so the modules are requested with the data of their battery
        Map<Integer, Set<Integer>> modules = moduleIndexesByBattery();
        for (Thing thing : getThing().getThings()) {
            for (GroupRequest group : GROUP_REQUESTS.getOrDefault(thing.getThingTypeUID(), List.of())) {
                E3DCContainerRequests.Container container = E3DCContainerRequests.forGroup(group.groupId());
                Integer index = configuredIndex(thing, group.indexConfigurationKey());
                if (index == null && E3DCBindingConstants.THING_TYPE_BATTERY_MODULE.equals(thing.getThingTypeUID())) {
                    // a module thing created from a file may omit its battery, which is the first one
                    index = 0;
                }
                if (container != null && index != null) {
                    Set<Integer> moduleIndexes = modules.get(index);
                    requests.putIfAbsent(container.containerTag() + "/" + index,
                            container.request(index, droppedTags, moduleIndexes != null ? moduleIndexes : Set.of()));
                }
            }
        }
        return new ArrayList<>(requests.values());
    }

    /** The module indexes of every battery, keyed by the index of the battery that reports them. */
    private Map<Integer, Set<Integer>> moduleIndexesByBattery() {
        Map<Integer, Set<Integer>> modules = new TreeMap<>();
        for (Thing thing : getThing().getThings()) {
            if (E3DCBindingConstants.THING_TYPE_BATTERY_MODULE.equals(thing.getThingTypeUID())) {
                Integer module = configuredIndex(thing, "moduleIndex");
                Integer battery = configuredIndex(thing, "batteryIndex");
                if (module != null && module >= 0 && (battery == null || battery >= 0)) {
                    int batteryIndex = battery != null ? battery : 0;
                    Set<Integer> indexes = modules.get(batteryIndex);
                    if (indexes == null) {
                        indexes = new TreeSet<>();
                        modules.put(batteryIndex, indexes);
                    }
                    indexes.add(module);
                }
            }
        }
        return modules;
    }

    /** The index a container is requested with, or the first device of that kind when no config key applies. */
    private static @Nullable Integer configuredIndex(Thing thing, @Nullable String configKey) {
        if (configKey == null) {
            return 0;
        }
        Object value = thing.getConfiguration().get(configKey);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.valueOf(text.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * The history containers need the period they cover, otherwise the device answers them with an error:
     * {@code START} is the beginning of the period in epoch seconds, {@code INTERVAL} the resolution of a value
     * and {@code SPAN} the length of the period.
     */
    private RscpValue historyRequest(int container) {
        int requestTag = RscpTag.pollTagFor(container);
        // the device counts its periods in local time, so the date and the start of the period use one zone
        ZoneId zone = getZone();
        LocalDate today = LocalDate.now(zone);
        LocalDate start = switch (container) {
            case RscpTag.DB_HISTORY_DATA_DAY -> today;
            case RscpTag.DB_HISTORY_DATA_WEEK -> today.minusDays(6);
            case RscpTag.DB_HISTORY_DATA_MONTH -> today.withDayOfMonth(1);
            case RscpTag.DB_HISTORY_DATA_YEAR -> today.withDayOfYear(1);
            default -> today;
        };
        Instant startTime = start.atStartOfDay(zone).toInstant();
        // the span has to reach the end of the current period, so it covers the days from start to today
        long dayCount = switch (container) {
            case RscpTag.DB_HISTORY_DATA_WEEK -> 7;
            case RscpTag.DB_HISTORY_DATA_MONTH -> today.lengthOfMonth();
            case RscpTag.DB_HISTORY_DATA_YEAR -> today.lengthOfYear();
            default -> 1;
        };
        return historyPeriodRequest(requestTag, startTime, Duration.ofDays(dayCount).toSeconds());
    }

    /**
     * The request for one history container. The interface declares {@code START}, {@code INTERVAL} and
     * {@code SPAN} as timestamps, so each is a moment of twelve bytes rather than a number; a duration is
     * carried in the seconds of such a moment, with zero nanoseconds.
     */
    private static RscpValue historyPeriodRequest(int requestTag, Instant start, long seconds) {
        return RscpValue.container(requestTag,
                List.of(RscpValue.timestamp(RscpTag.DB_REQ_HISTORY_TIME_START, start),
                        RscpValue.timestamp(RscpTag.DB_REQ_HISTORY_TIME_INTERVAL, Instant.ofEpochSecond(seconds)),
                        RscpValue.timestamp(RscpTag.DB_REQ_HISTORY_TIME_SPAN, Instant.ofEpochSecond(seconds))));
    }

    /** The request for the history of one whole calendar year, used by the history fetch of the thing action. */
    /**
     * The request that reads the history of one year.
     */
    public static RscpValue yearHistoryRequest(int year, ZoneId zone) {
        LocalDate start = LocalDate.of(year, 1, 1);
        return periodHistoryRequest(RscpTag.DB_HISTORY_DATA_YEAR, start, start.lengthOfYear(), zone);
    }

    /** The request for the history of a finished period, used by the history fetch of the thing action. */
    /**
     * The request that reads the days of one period.
     */
    public static RscpValue periodHistoryRequest(int container, LocalDate start, int days, ZoneId zone) {
        Instant startTime = start.atStartOfDay(zone).toInstant();
        return historyPeriodRequest(RscpTag.pollTagFor(container), startTime, Duration.ofDays(days).toSeconds());
    }

    /** Counts error responses per tag and stops requesting tags the device does not implement. */
    private void reportTagSupport(RscpValue response) {
        Set<Integer> errored = new HashSet<>();
        collectErrors(response, errored);
        for (int tag : errored) {
            RscpError error = findErrorFor(response, tag);
            // an error that stays is logged once, the repeats only at trace level
            if (loggedErrors.add(tag)) {
                logger.debug("{} was answered with {}", RscpTag.nameOf(tag), error);
            } else {
                logger.trace("{} was answered with {}", RscpTag.nameOf(tag), error);
            }
            // the trackers and sensors are asked for indexes the inverter may not have, which says nothing about them
            if (E3DCContainerRequests.isProbed(tag)) {
                continue;
            }
            if (isUnsupported(error)) {
                Integer previous = tagFailures.get(tag);
                int failures = (previous != null ? previous : 0) + 1;
                tagFailures.put(tag, failures);
                if (failures >= UNSUPPORTED_TAG_THRESHOLD && droppedTags.add(tag)) {
                    logger.debug("The device does not provide {} ({}); it will no longer be requested",
                            RscpTag.nameOf(tag), error);
                }
            } else {
                // a condition that passes, such as NOT_AVAILABLE, says nothing about the data point itself
                tagFailures.remove(tag);
            }
        }
        loggedErrors.retainAll(errored);
        for (int tag : new HashSet<>(tagFailures.keySet())) {
            if (!errored.contains(tag)) {
                tagFailures.remove(tag);
            }
        }
    }

    /**
     * Whether the error answer proves that the device does not implement the data point. Every other error is a
     * condition that may pass: the inverter answers {@code NOT_AVAILABLE} for its values while it is switched off
     * and has to be asked for them again when it returns.
     */
    static boolean isUnsupported(@Nullable RscpError error) {
        return error == RscpError.UNKNOWN_TAG || error == RscpError.NOT_HANDLED;
    }

    private void collectErrors(RscpValue item, Set<Integer> errored) {
        if (item.isError()) {
            errored.add(item.getTag());
        }
        for (RscpValue child : item.getChildren()) {
            collectErrors(child, errored);
        }
    }

    private @Nullable RscpError findErrorFor(RscpValue item, int tag) {
        if (item.getTag() == tag && item.isError()) {
            return item.getError();
        }
        for (RscpValue child : item.getChildren()) {
            RscpError found = findErrorFor(child, tag);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private void handleCommunicationFailure(RscpException e) {
        closeClient();
        if (Thread.currentThread().isInterrupted()) {
            // the thread was interrupted, so the handler is shutting down and must not reconnect
            return;
        }
        if (isActive()) {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduleReconnect();
        }
    }

    /** Sends one RSCP request over the existing connection. Used by child handlers for commands. */
    /**
     * Sends the request and returns the answer, or throws when the device does not answer.
     */
    public RscpValue sendRequest(List<RscpValue> items) throws RscpException {
        RscpClient current = client;
        if (current == null || !current.isConnected()) {
            throw new RscpException("Not connected to the device");
        }
        return current.request(items);
    }

    /** The interval between two poll cycles in seconds. */
    /**
     * The poll interval in seconds.
     */
    public int getRefreshIntervalSeconds() {
        E3DCBridgeConfiguration current = config;
        return current != null ? current.refreshIntervalSeconds() : 60;
    }

    /**
     * Whether the connection to the device exists.
     */
    public boolean isConnected() {
        RscpClient current = client;
        return current != null && current.isConnected();
    }

    /** Registers something to run each time the connection to the device has come up, as the discovery does. */
    /**
     * Adds a listener that is told when the bridge came online.
     */
    public void addOnlineListener(Runnable listener) {
        onlineListeners.add(listener);
    }

    /**
     * Removes the online listener.
     */
    public void removeOnlineListener(Runnable listener) {
        onlineListeners.remove(listener);
    }

    /**
     * Registers a child handler that receives the poll answers.
     */
    public void registerDataListener(E3DCDataListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the child handler.
     */
    public void unregisterDataListener(E3DCDataListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // the channels of the bridge are read only statistics
    }

    @Override
    public void dispose() {
        // an attempt that is still under way has to stop and must not install its connection afterwards
        connectionGeneration.incrementAndGet();
        cancelPollJob();
        closeClient();
        listeners.clear();
        onlineListeners.clear();
        super.dispose();
    }

    private void cancelPollJob() {
        synchronized (jobLock) {
            ScheduledFuture<?> job = pollJob;
            pollJob = null;
            if (job != null) {
                job.cancel(false);
            }
        }
    }

    private void closeClient() {
        // the attempt that is under way comes first, because it is not in client while it is being established
        RscpClient pending = pendingClient;
        pendingClient = null;
        if (pending != null) {
            pending.close();
        }
        RscpClient current = client;
        client = null;
        if (current != null) {
            current.close();
        }
    }

    /**
     * Whether this handler still wants to talk to the device, so a disposed handler's scheduled retry cannot
     * outlive it.
     */
    private boolean isActive() {
        return getThing().getStatus() != ThingStatus.UNINITIALIZED && getCallback() != null;
    }
}
