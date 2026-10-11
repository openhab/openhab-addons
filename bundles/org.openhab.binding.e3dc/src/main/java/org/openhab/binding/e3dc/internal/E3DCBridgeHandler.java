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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
     * The tags requested on every poll: the union of the channels of all three thing types. Requesting
     * a fixed set keeps the poll independent of which things the user has created.
     */
    private static final Set<Integer> POLL_TAGS = pollTags();

    /** Containers whose nested values the binding publishes. */
    private static final Set<Integer> POLL_CONTAINERS = pollContainers();

    /**
     * Containers the device only answers when the request states the period it covers. They are asked
     * for separately from {@link #POLL_CONTAINERS} because they need parameters a bare container
     * request does not carry.
     */
    private static final Set<Integer> WINDOWED_CONTAINERS = Set.of(RscpTag.DB_HISTORY_DATA_DAY,
            RscpTag.DB_HISTORY_DATA_WEEK, RscpTag.DB_HISTORY_DATA_MONTH, RscpTag.DB_HISTORY_DATA_YEAR);

    private static Set<Integer> pollTags() {
        Set<Integer> tags = new LinkedHashSet<>();
        for (E3DCChannelSpec spec : allSpecs()) {
            if (spec.container() == 0) {
                tags.add(spec.tag());
            }
        }
        return tags;
    }

    private static Set<Integer> pollContainers() {
        Set<Integer> containers = new LinkedHashSet<>();
        for (E3DCChannelSpec spec : allSpecs()) {
            if (spec.container() != 0) {
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
            logger.debug("Cannot connect to {}: {}", current.host, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }
        client = newClient;
        droppedTags.clear();
        updateStatus(ThingStatus.ONLINE);
        logger.debug("Connected to E3/DC device at {}:{}", current.host, current.port);

        pollOnce();
        scheduleNextPoll();
    }

    private void scheduleNextPoll() {
        E3DCBridgeConfiguration current = config;
        if (current == null) {
            return;
        }
        schedule(this::pollCycle, current.refreshInterval);
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
     * Builds one request covering every root tag plus the values nested in containers.
     *
     * <p>
     * The channel specs name the tags the device answers with, but a request has to carry the matching
     * {@code *_REQ_*} tag: the device does not recognise a response tag in a request. A container is
     * requested by its own request tag without listing its members, because asking for the container
     * returns the enclosed values.
     */
    private RscpValue buildRequest() {
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
        return RscpValue.container(0, items);
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
        LocalDate today = LocalDate.now();
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
    public RscpValue sendRequest(RscpValue item) throws RscpException {
        RscpClient current = client;
        if (current == null || !current.isConnected()) {
            throw new RscpException("Not connected to the device");
        }
        return current.request(item);
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
