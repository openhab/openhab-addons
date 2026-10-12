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
package org.openhab.binding.e3dc.internal;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.rscp.RscpClient;
import org.openhab.binding.e3dc.internal.rscp.RscpError;
import org.openhab.binding.e3dc.internal.rscp.RscpException;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
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
            E3DCBindingConstants.THING_TYPE_BATTERY_MODULE,
            List.of(new GroupRequest(E3DCBindingConstants.GROUP_MODULE, null)), //
            E3DCBindingConstants.THING_TYPE_POWER_PLANT,
            List.of(new GroupRequest(E3DCBindingConstants.GROUP_INVERTER, null),
                    new GroupRequest(E3DCBindingConstants.GROUP_RESERVE, null)));

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
        specs.addAll(E3DCChannelSpec.batteryModule().all());
        return specs;
    }

    /** Consecutive poll failures per tag; a tag is dropped once it reaches the threshold. */
    private final Map<Integer, Integer> tagFailures = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(E3DCBridgeHandler.class);
    private final List<E3DCDataListener> listeners = new CopyOnWriteArrayList<>();
    private final Set<Integer> droppedTags = new HashSet<>();

    private @Nullable E3DCBridgeConfiguration config;
    private @Nullable RscpClient client;
    private @Nullable ScheduledFuture<?> pollJob;

    public E3DCBridgeHandler(Bridge bridge) {
        super(bridge);
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
        updateStatus(ThingStatus.ONLINE);
        logger.info("Connected to E3/DC device at {}:{}", current.host, current.port);

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
            RscpValue response = current.request(buildRequest());
            reportTagSupport(response);
            logContainerAnswers(response);
            for (E3DCDataListener listener : listeners) {
                try {
                    listener.onData(response);
                } catch (RuntimeException e) {
                    logger.warn("A thing handler failed to process poll data", e);
                }
            }
        } catch (RscpException e) {
            logger.debug("Poll failed: {}", e.getMessage());
            handleCommunicationFailure(e);
        }
    }

    /**
     * Logs the containers the parameterised requests were answered with.
     *
     * <p>
     * A request the device rejects is otherwise invisible: the container simply stays without values and the
     * channels of the thing stay empty.
     */
    private void logContainerAnswers(RscpValue response) {
        for (E3DCContainerRequests.Container container : E3DCContainerRequests.all()) {
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
    private List<RscpValue> buildRequest() {
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
            if (POLL_CONTAINERS.contains(container)) {
                items.add(historyRequest(container));
            }
        }
        items.addAll(containerRequests());
        return items;
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
        for (Thing thing : getThing().getThings()) {
            for (GroupRequest group : GROUP_REQUESTS.getOrDefault(thing.getThingTypeUID(), List.of())) {
                E3DCContainerRequests.Container container = E3DCContainerRequests.forGroup(group.groupId());
                Integer index = configuredIndex(thing, group.indexConfigurationKey());
                if (container != null && index != null) {
                    requests.putIfAbsent(container.containerTag() + "/" + index, container.request(index, droppedTags));
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
     * Counts error responses per tag and stops requesting tags the device does not implement, so a
     * system with fewer features keeps polling without repeatedly asking for the same missing value.
     */
    private void reportTagSupport(RscpValue response) {
        Set<Integer> errored = new HashSet<>();
        collectErrors(response, errored);
        for (int tag : errored) {
            RscpError error = findErrorFor(response, tag);
            logger.debug("{} was answered with {}", RscpTag.nameOf(tag), error);
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

    public boolean isConnected() {
        RscpClient current = client;
        return current != null && current.isConnected();
    }

    public void registerDataListener(E3DCDataListener listener) {
        listeners.add(listener);
    }

    public void unregisterDataListener(E3DCDataListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // the bridge itself exposes no channels
    }

    @Override
    public void dispose() {
        cancelPollJob();
        closeClient();
        listeners.clear();
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
