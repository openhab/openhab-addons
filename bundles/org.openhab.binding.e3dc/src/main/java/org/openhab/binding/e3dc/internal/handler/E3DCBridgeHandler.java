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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
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
 * Represents the connection to one E3/DC device.
 *
 * <p>
 * This handler owns the RSCP connection and the poll cycle. Child handlers register as
 * {@link E3DCDataListener} and extract their channels from each response.
 *
 * <p>
 * Not every device implements every data point. A request for an unsupported tag is answered with an
 * error item rather than a failure, so such tags are counted and dropped after a few attempts. That
 * keeps the binding working on systems with fewer features without asserting anything about which
 * model supports what.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCBridgeHandler extends BaseBridgeHandler {

    /** How many consecutive error responses a tag may produce before it is no longer requested. */
    private static final int UNSUPPORTED_TAG_THRESHOLD = 3;

    /** Wait before establishing a lost connection again. */
    private static final int RECONNECT_DELAY_SECONDS = 30;

    /**
     * The tags requested on every poll, which are the root values of the channels of all thing types. A
     * fixed set keeps the poll independent of which things the user has created.
     *
     * <p>
     * A value that is read through a container that addresses one of several devices does not belong here:
     * it is part of the request of that container, see {@link #containerRequests()}.
     */
    private static final Set<Integer> POLL_TAGS = pollTags();

    /**
     * Containers whose nested values the binding publishes, requested with no parameter. Containers that
     * address one of several devices are not part of this set for the same reason.
     */
    private static final Set<Integer> POLL_CONTAINERS = pollContainers();

    /**
     * Containers the device only answers when the request states the period it covers. They are asked
     * for separately from {@link #POLL_CONTAINERS} because they need parameters a bare container
     * request does not carry.
     */
    private static final Set<Integer> WINDOWED_CONTAINERS = Set.of(RscpTag.DB_HISTORY_DATA_DAY,
            RscpTag.DB_HISTORY_DATA_WEEK, RscpTag.DB_HISTORY_DATA_MONTH, RscpTag.DB_HISTORY_DATA_YEAR);

    /**
     * The channel groups a thing needs a container request for, with the configuration key holding the index
     * of the addressed device. A group without a key is requested with index {@code 0}: the cell modules are
     * asked for as the first battery, which answers with the container of every module.
     */
    private static final Map<ThingTypeUID, List<GroupRequest>> GROUP_REQUESTS = Map.of(//
            E3DCBindingConstants.THING_TYPE_WALLBOX,
            List.of(new GroupRequest(E3DCBindingConstants.GROUP_WALLBOX, "wallboxIndex")), //
            E3DCBindingConstants.THING_TYPE_POWER_METER,
            List.of(new GroupRequest(E3DCBindingConstants.GROUP_METER, "meterIndex")), //
            E3DCBindingConstants.THING_TYPE_BATTERY,
            List.of(new GroupRequest(E3DCBindingConstants.GROUP_PACK, "batteryIndex")), //
            E3DCBindingConstants.THING_TYPE_BATTERY_MODULE,
            List.of(new GroupRequest(E3DCBindingConstants.GROUP_MODULE, null)), //
            E3DCBindingConstants.THING_TYPE_POWER_PLANT,
            List.of(new GroupRequest(E3DCBindingConstants.GROUP_INVERTER, null),
                    new GroupRequest(E3DCBindingConstants.GROUP_EMERGENCY, null)));

    /** One channel group of a thing, with the configuration key of the device it addresses. */
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
    private @Nullable RscpClient client;
    private @Nullable ScheduledFuture<?> pollJob;

    public E3DCBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(E3DCDiscoveryService.class);
    }

    @Override
    public void initialize() {
        config = getConfigAs(E3DCBridgeConfiguration.class);

        E3DCBridgeConfiguration current = config;
        if (current == null) {
            return;
        }
        if (current.host.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host is not configured");
            return;
        }
        if (current.rscpPassword.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "RSCP password is not configured");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::connectAndPoll);
    }

    private void connectAndPoll() {
        E3DCBridgeConfiguration current = config;
        if (current == null || !isActive()) {
            return;
        }

        RscpClient newClient = new RscpClient(current.host, current.port, current.userName, current.password,
                current.rscpPassword, current.timeout * 1000);
        try {
            newClient.connect();
        } catch (RscpException e) {
            newClient.close();
            // a connection that does not come up is what the user has to act on, so it belongs in the log at
            // the default level and not only in the thing status
            logger.warn("Cannot connect to {}:{}: {}", current.host, current.port, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }
        client = newClient;
        droppedTags.clear();
        loggedAnswers.clear();
        loggedErrors.clear();
        // a new connection starts with the history, so the channels are filled with the first poll
        lastHistoryPollMillis = 0;
        lastBusyNanos = newClient.getBusyNanos();
        updateStatus(ThingStatus.ONLINE);
        logger.info("Connected to E3/DC device at {}:{}", current.host, current.port);
        for (Runnable listener : onlineListeners) {
            listener.run();
        }

        pollOnce();
        scheduleNextPoll();
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

    /**
     * Replaces any pending job with the given one, so exactly one timer is ever outstanding.
     *
     * <p>
     * Every path that wants to run again goes through here. Without this a lost connection would stop
     * the cycle silently, because a failing poll returns without rescheduling.
     */
    private void schedule(Runnable task, long delaySeconds) {
        if (!isActive()) {
            return;
        }
        cancelPollJob();
        pollJob = scheduler.schedule(task, delaySeconds, TimeUnit.SECONDS);
    }

    /**
     * Asks for an immediate poll, used when a channel receives a refresh command.
     *
     * <p>
     * The work is queued on the scheduler rather than run here: a refresh arrives on a framework thread
     * and the poll performs blocking socket I/O.
     */
    public void requestRefresh() {
        if (!isActive()) {
            return;
        }
        scheduler.execute(() -> {
            if (isActive() && isConnected()) {
                pollCycle();
            }
        });
    }

    private void pollCycle() {
        if (!isActive()) {
            return;
        }
        if (!isConnected()) {
            // the connection was lost between two cycles, establish it again
            connectAndPoll();
            return;
        }
        try {
            pollOnce();
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (RuntimeException e) {
            logger.debug("Unexpected problem during the poll cycle", e);
        }
        scheduleNextPoll();
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
                lastHistoryDay = LocalDate.now(ZoneOffset.UTC);
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
     * Records the loop ratio of the cycle: the time the device was occupied by this client divided by the
     * poll interval.
     *
     * <p>
     * The time covers everything that went over the connection since the last poll, so commands and history
     * fetches are included. The dispatch to the thing handlers is not, because it loads openHAB and not the
     * device. A ratio of 1 means the client keeps the device busy for the whole interval. The target is a
     * working assumption until the measurements show where the device starts to suffer.
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

    /** Publishes the figures of the last poll on the channels of the statistics group. */
    private void updateStatistics(RscpClient current, double ratio, long dispatchNanos) {
        publishStatistic("loop-ratio", new QuantityType<>(ratio * 100, Units.PERCENT));
        publishStatistic("poll-duration", milliseconds(current.getLastWireNanos()));
        publishStatistic("device-wait", milliseconds(current.getLastDeviceWaitNanos()));
        publishStatistic("dispatch-time", milliseconds(dispatchNanos));
        publishStatistic("answer-size", new QuantityType<>(current.getLastAnswerBytes(), Units.BYTE));
    }

    /** Publishes the summary of the last window of polls. */
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

    /**
     * The size of the answer per top level element, as the number of values it holds.
     *
     * <p>
     * Only used for the trace: it shows which parts of the answer are large, which is where the device spends
     * its time and the connection its bytes.
     */
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

    /**
     * Logs the containers the parameterised requests were answered with.
     *
     * <p>
     * A request the device rejects is otherwise invisible: the container simply stays without values and the
     * channels of the thing stay empty.
     */
    private void logContainerAnswers(RscpValue response) {
        // the trees are long texts, so nothing is searched or built while debug logging is off
        if (!logger.isDebugEnabled()) {
            return;
        }
        for (E3DCContainerRequests.Container container : E3DCContainerRequests.all()) {
            // the trees are the same with every poll, so debug shows each one once per connection and
            // trace shows all of them
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
     * per container whose answer depends on the parameters of the request.
     *
     * <p>
     * The channel specs name the tags the device answers with, but a request has to carry the matching
     * {@code *_REQ_*} tag: the device does not recognise a response tag in a request. A container that
     * answers everything it has is requested by its own request tag without listing its members.
     *
     * <p>
     * The elements are top level items of the frame, not children of a common container: an RSCP frame
     * has no root element other than the list itself.
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

    /**
     * Whether this poll has to carry the history containers.
     *
     * <p>
     * The history counters change slowly, so they are requested at the history interval and not with every
     * poll. They are also due on the first poll after a connect and when the day has changed, so the
     * periods of a request never lag behind the date.
     */
    private boolean isHistoryDue() {
        E3DCBridgeConfiguration current = config;
        if (current == null || lastHistoryPollMillis == 0) {
            return true;
        }
        if (!LocalDate.now(ZoneOffset.UTC).equals(lastHistoryDay)) {
            return true;
        }
        return System.currentTimeMillis() - lastHistoryPollMillis >= current.historyIntervalSeconds() * 1000L;
    }

    /**
     * One request per container whose answer depends on the parameters of the request.
     *
     * <p>
     * Which containers a system needs follows from the things it has, so a system without a wallbox does not
     * ask for one. A container of a thing that exists more than once is requested once per configured index.
     */
    private List<RscpValue> containerRequests() {
        Map<String, RscpValue> requests = new LinkedHashMap<>();
        // the modules are read through the request of the first battery, one BAT_REQ_DCB_INFO per module index
        TreeSet<Integer> moduleIndexes = new TreeSet<>();
        for (Thing thing : getThing().getThings()) {
            if (E3DCBindingConstants.THING_TYPE_BATTERY_MODULE.equals(thing.getThingTypeUID())) {
                Integer module = configuredIndex(thing, "moduleIndex");
                if (module != null && module >= 0) {
                    moduleIndexes.add(module);
                }
            }
        }
        for (Thing thing : getThing().getThings()) {
            for (GroupRequest group : GROUP_REQUESTS.getOrDefault(thing.getThingTypeUID(), List.of())) {
                E3DCContainerRequests.Container container = E3DCContainerRequests.forGroup(group.groupId());
                Integer index = configuredIndex(thing, group.indexConfigurationKey());
                if (container != null && index != null) {
                    List<Integer> modules = index == 0 ? List.copyOf(moduleIndexes) : List.of();
                    requests.putIfAbsent(container.containerTag() + "/" + index,
                            container.request(index, droppedTags, modules));
                }
            }
        }
        return new ArrayList<>(requests.values());
    }

    /**
     * The index a container is requested with: the configured one when the group names a configuration key,
     * otherwise the first device of that kind.
     */
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
     * The history containers need the period they cover, otherwise the device answers them with an
     * error. {@code START} is the beginning of the period in epoch seconds, {@code INTERVAL} the
     * resolution of a single value and {@code SPAN} the length of the period.
     *
     * <p>
     * Each container is asked for the period that ends with the current one, so the day container
     * covers today, the week container the last seven days and so on.
     */
    private RscpValue historyRequest(int container) {
        int requestTag = RscpTag.pollTagFor(container);
        // RSCP timestamps are UTC, so the period boundaries are taken in UTC as well
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate start = switch (container) {
            case RscpTag.DB_HISTORY_DATA_DAY -> today;
            case RscpTag.DB_HISTORY_DATA_WEEK -> today.minusDays(6);
            case RscpTag.DB_HISTORY_DATA_MONTH -> today.withDayOfMonth(1);
            case RscpTag.DB_HISTORY_DATA_YEAR -> today.withDayOfYear(1);
            default -> today;
        };
        long startSeconds = start.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        // the span has to reach the end of the current period, so it covers the days from start to today
        long dayCount = switch (container) {
            case RscpTag.DB_HISTORY_DATA_WEEK -> 7;
            case RscpTag.DB_HISTORY_DATA_MONTH -> today.lengthOfMonth();
            case RscpTag.DB_HISTORY_DATA_YEAR -> today.lengthOfYear();
            default -> 1;
        };
        long seconds = Duration.ofDays(dayCount).toSeconds();
        return RscpValue.container(requestTag,
                List.of(RscpValue.of(RscpTag.DB_REQ_HISTORY_TIME_START, RscpType.UINT64, startSeconds),
                        RscpValue.of(RscpTag.DB_REQ_HISTORY_TIME_INTERVAL, RscpType.UINT64, seconds),
                        RscpValue.of(RscpTag.DB_REQ_HISTORY_TIME_SPAN, RscpType.UINT64, seconds)));
    }

    /**
     * The request for the history of one whole calendar year, used by the history fetch of the thing action.
     *
     * <p>
     * It carries the same parameters as the request of the running year, only with the boundaries of the
     * given year, so the answer belongs to exactly this year.
     *
     * @param year the calendar year
     * @return the request for the year container
     */
    public static RscpValue yearHistoryRequest(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        return periodHistoryRequest(RscpTag.DB_HISTORY_DATA_YEAR, start, start.lengthOfYear());
    }

    /**
     * The request for the history of a finished period, used by the history fetch of the thing action.
     *
     * @param container the history container of the period, for example {@link RscpTag#DB_HISTORY_DATA_DAY}
     * @param start the first day of the period
     * @param days the number of days the period covers
     * @return the request for the container
     */
    public static RscpValue periodHistoryRequest(int container, LocalDate start, int days) {
        long startSeconds = start.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        long seconds = Duration.ofDays(days).toSeconds();
        return RscpValue.container(RscpTag.pollTagFor(container),
                List.of(RscpValue.of(RscpTag.DB_REQ_HISTORY_TIME_START, RscpType.UINT64, startSeconds),
                        RscpValue.of(RscpTag.DB_REQ_HISTORY_TIME_INTERVAL, RscpType.UINT64, seconds),
                        RscpValue.of(RscpTag.DB_REQ_HISTORY_TIME_SPAN, RscpType.UINT64, seconds)));
    }

    /**
     * Counts error responses per tag and stops requesting tags the device does not implement, so a
     * system with fewer features keeps polling without repeatedly asking for the same missing value.
     */
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
            if (error == RscpError.UNKNOWN_TAG || error == RscpError.NOT_AVAILABLE || error == RscpError.NOT_HANDLED) {
                Integer previous = tagFailures.get(tag);
                int failures = (previous != null ? previous : 0) + 1;
                tagFailures.put(tag, failures);
                if (failures >= UNSUPPORTED_TAG_THRESHOLD && droppedTags.add(tag)) {
                    logger.debug("The device does not provide {} ({}); it will no longer be requested",
                            RscpTag.nameOf(tag), error);
                }
            }
        }
        loggedErrors.retainAll(errored);
        // tags that answered successfully are considered supported again
        for (int tag : new HashSet<>(tagFailures.keySet())) {
            if (!errored.contains(tag)) {
                tagFailures.remove(tag);
            }
        }
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
        if (isActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            // a fresh connection is attempted after a short wait
            scheduleReconnect();
        }
    }

    /** Sends one RSCP request over the existing connection. Used by child handlers for commands. */
    public RscpValue sendRequest(List<RscpValue> items) throws RscpException {
        RscpClient current = client;
        if (current == null || !current.isConnected()) {
            throw new RscpException("Not connected to the device");
        }
        return current.request(items);
    }

    /** The interval between two poll cycles in seconds. */
    public int getRefreshIntervalSeconds() {
        E3DCBridgeConfiguration current = config;
        return current != null ? current.refreshIntervalSeconds() : 60;
    }

    public boolean isConnected() {
        RscpClient current = client;
        return current != null && current.isConnected();
    }

    /** Registers something to run each time the connection to the device has come up, as the discovery does. */
    public void addOnlineListener(Runnable listener) {
        onlineListeners.add(listener);
    }

    public void removeOnlineListener(Runnable listener) {
        onlineListeners.remove(listener);
    }

    public void registerDataListener(E3DCDataListener listener) {
        listeners.add(listener);
    }

    public void unregisterDataListener(E3DCDataListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // the channels of the bridge are read only statistics
    }

    @Override
    public void dispose() {
        cancelPollJob();
        closeClient();
        listeners.clear();
        onlineListeners.clear();
        super.dispose();
    }

    private void cancelPollJob() {
        ScheduledFuture<?> job = pollJob;
        pollJob = null;
        if (job != null) {
            job.cancel(false);
        }
    }

    private void closeClient() {
        RscpClient current = client;
        client = null;
        if (current != null) {
            current.close();
        }
    }

    /**
     * Whether this handler still wants to talk to the device. A disposed or uninitialized handler
     * must not reconnect, otherwise a scheduled retry could outlive the thing.
     */
    private boolean isActive() {
        return getThing().getStatus() != ThingStatus.UNINITIALIZED && getCallback() != null;
    }
}
