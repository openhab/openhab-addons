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
package org.openhab.binding.robonect.internal.handler;

import static org.openhab.binding.robonect.internal.RobonectBindingConstants.*;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.robonect.internal.RobonectClient;
import org.openhab.binding.robonect.internal.RobonectCommunicationException;
import org.openhab.binding.robonect.internal.RobonectEndpoint;
import org.openhab.binding.robonect.internal.RobonectPushServlet;
import org.openhab.binding.robonect.internal.config.JobChannelConfig;
import org.openhab.binding.robonect.internal.config.RobonectConfig;
import org.openhab.binding.robonect.internal.model.MowerStatus;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.util.ThingWebClientUtil;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link RobonectHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * The channels are periodically updated by polling the mower via HTTP in a separate thread.
 *
 * @author Marco Meyer - Initial contribution
 * @author Nico Brüttner - Channel, polling, and endpoint handling
 * @author Nico Brüttner - Mowing job action
 */
public class RobonectHandler extends BaseThingHandler {

    private static final ChannelTypeUID JOB_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, "jobType");

    private final Logger logger = LoggerFactory.getLogger(RobonectHandler.class);

    private ScheduledFuture<?> pollingJob;
    private ScheduledFuture<?> detailedPollingJob;
    private final ScheduledExecutorService requestScheduler;
    private final ReentrantLock pollingLock = new ReentrantLock(true);
    private volatile @Nullable PollingSession pollingSession;
    private final ThreadLocal<BooleanSupplier> sessionActive = new ThreadLocal<>();
    private volatile @Nullable PushRefresh pushRefresh;
    private final Map<String, CommandRefresh> commandRefreshJobs = new ConcurrentHashMap<>();
    private final ThreadLocal<BooleanSupplier> commandRefreshActive = new ThreadLocal<>();
    private volatile boolean disposed;

    private HttpClient httpClient;
    private TimeZoneProvider timeZoneProvider;

    private volatile ZoneId timeZone;

    private volatile RobonectDiagnosticsHandler diagnosticsHandler;
    private volatile RobonectMowerHandler mowerHandler;
    private volatile RobonectPushHandler pushHandler;
    private volatile RobonectTimerHandler timerHandler;
    private volatile RobonectRemoteStartHandler remoteStartHandler;
    private volatile RobonectWeatherHandler weatherHandler;
    private final @Nullable RobonectPushServlet pushServlet;
    private boolean ajaxUiScraping;
    private volatile boolean useUnofficialApi;

    /**
     * Creates a mower Thing handler without a push servlet.
     *
     * @param thing the mower Thing
     * @param httpClientFactory factory for the mower HTTP client
     * @param timeZoneProvider provider for the default time zone
     */
    public RobonectHandler(Thing thing, HttpClientFactory httpClientFactory, TimeZoneProvider timeZoneProvider) {
        this(thing, httpClientFactory, timeZoneProvider, null);
    }

    /**
     * Creates a mower Thing handler with an optional push servlet.
     *
     * @param thing the mower Thing
     * @param httpClientFactory factory for the mower HTTP client
     * @param timeZoneProvider provider for the default time zone
     * @param pushServlet servlet that delivers push notifications, or {@code null}
     */
    public RobonectHandler(Thing thing, HttpClientFactory httpClientFactory, TimeZoneProvider timeZoneProvider,
            @Nullable RobonectPushServlet pushServlet) {
        this(thing, httpClientFactory, timeZoneProvider, pushServlet, null);
    }

    /**
     * Creates a mower Thing handler with an optional push servlet and scheduler.
     *
     * @param thing the mower Thing
     * @param httpClientFactory factory for the mower HTTP client
     * @param timeZoneProvider provider for the default time zone
     * @param pushServlet servlet that delivers push notifications, or {@code null}
     * @param commandScheduler scheduler for requests, or {@code null} to use the framework scheduler
     */
    RobonectHandler(Thing thing, HttpClientFactory httpClientFactory, TimeZoneProvider timeZoneProvider,
            @Nullable RobonectPushServlet pushServlet, @Nullable ScheduledExecutorService commandScheduler) {
        super(thing);
        requestScheduler = commandScheduler == null ? scheduler : commandScheduler;
        httpClient = httpClientFactory
                .createHttpClient(ThingWebClientUtil.buildWebClientConsumerName(thing.getUID(), null));
        this.timeZoneProvider = timeZoneProvider;
        timeZone = timeZoneProvider.getTimeZone();
        this.pushServlet = pushServlet;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(RobonectActions.class);
    }

    void startJob(RobonectClient.JobSettings settings) {
        PollingSession session = pollingSession;
        RobonectMowerHandler mower = mowerHandler;
        if (session == null || disposed || !session.equals(pollingSession)) {
            throw new IllegalStateException("Robonect Thing is not initialized");
        }
        sessionActive.set(() -> session.equals(pollingSession));
        try {
            if (!canPublishState()) {
                throw new IllegalStateException("Robonect Thing session has ended");
            }
            cancelCommandRefresh("status");
            if (!mower.startJob(settings).isSuccessful()) {
                throw new IllegalStateException("Robonect rejected the mowing job");
            }
            updateJobChannels(null, true, settings);
        } finally {
            sessionActive.remove();
        }
    }

    /**
     * Routes refreshes and commands to endpoint handlers and reports communication failures.
     *
     * @param channelUID the channel identifier
     * @param command the command received for the channel
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (disposed) {
            return;
        }
        try {
            if (command instanceof RefreshType) {
                refreshChannels(channelUID);
            } else {
                sendCommand(channelUID, command);
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (RobonectCommunicationException rce) {
            logger.debug("Failed to communicate with the mower. Taking it offline.", rce);
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, rce.getMessage());
        }
    }

    /**
     * Routes a channel command to the component responsible for that endpoint.
     *
     * @param channelUID the channel identifier
     * @param command the command received for the channel
     */
    private void sendCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_MOWER_NAME:
                mowerHandler.handleNameCommand(command);
                break;
            case CHANNEL_STATUS_MODE:
                mowerHandler.handleModeCommand(command);
                break;
            case CHANNEL_MOWER_START:
                mowerHandler.handleStartCommand(command);
                break;
            case CHANNEL_RESET_ERROR:
                mowerHandler.handleResetErrorCommand(command);
                break;
            case CHANNEL_SERVICE_REBOOT:
            case CHANNEL_SERVICE_SHUTDOWN:
            case CHANNEL_SERVICE_SLEEP:
                mowerHandler.handleServiceCommand(channelUID, command);
                break;
            case CHANNEL_DOOR_STATE:
                mowerHandler.handleDoorCommand(command);
                break;
            case CHANNEL_WEATHER_ENABLED:
            case CHANNEL_WEATHER_INTERVAL:
            case CHANNEL_WEATHER_MAX_RAIN:
            case CHANNEL_WEATHER_MIN_TEMPERATURE:
            case CHANNEL_WEATHER_MAX_TEMPERATURE:
            case CHANNEL_WEATHER_MIN_HUMIDITY:
            case CHANNEL_WEATHER_MAX_HUMIDITY:
            case CHANNEL_WEATHER_SKIP_DAY:
            case CHANNEL_WEATHER_SKIP_NIGHT:
            case CHANNEL_WEATHER_LOCATION_CITY_ID:
            case CHANNEL_WEATHER_LOCATION_ZIP:
                weatherHandler.handleCommand(channelUID.getId(), command);
                break;
            case CHANNEL_PUSH_URL:
            case CHANNEL_PUSH_USERNAME:
            case CHANNEL_PUSH_PASSWORD:
            case CHANNEL_PUSH_INTERVAL:
                pushHandler.handleCommand(channelUID.getId(), command);
                break;
            case CHANNEL_PUSH_MODE:
                pushHandler.handleModeCommand(command);
                break;
            default:
                if (channelUID.getId().startsWith("remote-")) {
                    remoteStartHandler.handleCommand(channelUID.getId(), command);
                } else if (channelUID.getId().startsWith("timer-")) {
                    timerHandler.handleCommand(channelUID.getId(), command);
                } else if (channelUID.getId().startsWith("push-trigger-")) {
                    pushHandler.handleTriggerCommand(channelUID.getId(), command);
                } else {
                    Channel ch = getThing().getChannel(channelUID.getId());
                    if (ch != null && JOB_CHANNEL_TYPE.equals(ch.getChannelTypeUID())) {
                        JobChannelConfig jobConfig = ch.getConfiguration().as(JobChannelConfig.class);
                        mowerHandler.handleJobCommand(channelUID.getId(), jobConfig, command);
                    }
                }
                break;
        }
    }

    /**
     * Creates a channel or updates the label while preserving an existing channel's configuration.
     *
     * @param id the channel identifier
     * @param itemType the accepted Item type
     * @param channelType the channel type identifier
     * @param label the channel label
     * @return the channel built with the requested type and label
     */
    Channel createChannel(String id, String itemType, String channelType, String label) {
        Channel existing = getThing().getChannel(id);
        if (existing != null) {
            return ChannelBuilder.create(existing).withLabel(label).build();
        }
        return ChannelBuilder.create(new ChannelUID(getThing().getUID(), id), itemType)
                .withType(new ChannelTypeUID(BINDING_ID, channelType)).withLabel(label).build();
    }

    /**
     * Reconciles the channels matching a pattern with the desired channels and labels.
     *
     * @param channelPattern the pattern identifying channels to reconcile
     * @param desiredChannels the channels to publish
     */
    void updateDynamicChannels(Pattern channelPattern, List<Channel> desiredChannels) {
        List<Channel> existingChannels = getThing().getChannels();
        Set<ChannelUID> current = existingChannels.stream()
                .filter(channel -> channelPattern.matcher(channel.getUID().getId()).matches()).map(Channel::getUID)
                .collect(Collectors.toSet());
        Set<ChannelUID> desired = desiredChannels.stream().map(Channel::getUID).collect(Collectors.toSet());
        if (current.equals(desired) && desiredChannels.stream().allMatch(channel -> {
            Channel existing = getThing().getChannel(channel.getUID());
            return existing != null && Objects.equals(existing.getLabel(), channel.getLabel());
        })) {
            return;
        }
        List<Channel> channels = new ArrayList<>(existingChannels.stream()
                .filter(channel -> !channelPattern.matcher(channel.getUID().getId()).matches()).toList());
        channels.addAll(desiredChannels);
        updateThing(editThing().withChannels(channels).build());
    }

    /**
     * Adds or relabels supplied dynamic channels without removing channels omitted by a partial response.
     *
     * @param desiredChannels the channels to publish
     */
    void addDynamicChannels(List<Channel> desiredChannels) {
        List<Channel> channels = new ArrayList<>(getThing().getChannels());
        boolean changed = false;
        for (Channel channel : desiredChannels) {
            Channel existing = getThing().getChannel(channel.getUID());
            if (existing == null) {
                channels.add(channel);
                changed = true;
            } else if (!Objects.equals(existing.getLabel(), channel.getLabel())) {
                channels.set(channels.indexOf(existing), channel);
                changed = true;
            }
        }
        if (!changed) {
            return;
        }
        updateThing(editThing().withChannels(channels).build());
    }

    /**
     * Cancels the pending delayed refresh for an endpoint, if any.
     *
     * @param endpoint the endpoint key used to coalesce refreshes
     */
    void cancelCommandRefresh(String endpoint) {
        CommandRefresh previous = commandRefreshJobs.remove(endpoint);
        if (previous != null) {
            previous.cancel();
        }
    }

    /**
     * Schedules a coalesced endpoint refresh after a successful command.
     *
     * @param endpoint the endpoint key used to coalesce refreshes
     * @param refresh the endpoint refresh to schedule
     */
    void scheduleCommandRefresh(String endpoint, Runnable refresh) {
        if (disposed) {
            return;
        }
        // Coalesce commands per endpoint by restarting the five-second delay after each command.
        CommandRefresh pending = new CommandRefresh();
        CommandRefresh previous = commandRefreshJobs.put(endpoint, pending);
        if (previous != null) {
            previous.cancel();
        }
        // Cancellation may not stop an in-flight HTTP request. canPublishState() uses this predicate via
        // commandRefreshActive to discard responses after replacement or disposal.
        BooleanSupplier active = () -> !disposed && pending.equals(commandRefreshJobs.get(endpoint));
        ScheduledFuture<?> job = requestScheduler.schedule(() -> {
            commandRefreshActive.set(active);
            try {
                if (active.getAsBoolean()) {
                    refresh.run();
                }
            } catch (RobonectCommunicationException e) {
                logger.debug("Could not refresh {} after a command: {}", endpoint, e.getMessage());
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } finally {
                commandRefreshActive.remove();
                // A newer command may already have replaced this task; leave its map entry intact.
                commandRefreshJobs.remove(endpoint, pending);
            }
        }, 5, TimeUnit.SECONDS);
        pending.job = job;
        // Replacement or disposal may have cancelled pending before its future was assigned, when cancel()
        // could not yet cancel the scheduled task. Close that race now that the future is available.
        if (!active.getAsBoolean()) {
            job.cancel(true);
            commandRefreshJobs.remove(endpoint, pending);
        }
    }

    @NonNullByDefault
    private static class CommandRefresh {
        private volatile @Nullable ScheduledFuture<?> job;

        /** Cancels the scheduled refresh if its future has been assigned. */
        private void cancel() {
            ScheduledFuture<?> current = job;
            if (current != null) {
                current.cancel(true);
            }
        }
    }

    /**
     * Checks whether the current poll or refresh is still authorized to publish.
     *
     * @return {@code true} if the current work may publish state, status, or Thing updates
     */
    boolean canPublishState() {
        // Responses may finish after disposal, reinitialization, or a newer command superseding a delayed read.
        BooleanSupplier active = commandRefreshActive.get();
        BooleanSupplier session = sessionActive.get();
        return !disposed && (active == null || active.getAsBoolean()) && (session == null || session.getAsBoolean());
    }

    /**
     * Publishes a channel state only while the current work remains valid.
     *
     * @param channelUID the channel identifier
     * @param state the state to publish
     */
    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        if (canPublishState()) {
            super.updateState(channelUID, state);
        }
    }

    /**
     * Publishes a state for a channel identifier through the lifecycle guard.
     *
     * @param channelId the channel identifier
     * @param state the state to publish, or {@code null} if absent
     * @param fullResponse whether an absent state should be published as undefined
     */
    void setState(String channelId, @Nullable State state, boolean fullResponse) {
        if (fullResponse || state != null) {
            updateState(new ChannelUID(getThing().getUID(), channelId), state == null ? UnDefType.UNDEF : state);
        }
    }

    void updateJobChannels(String channelId, boolean isJobRunning, RobonectClient.JobSettings settings) {
        List<Channel> jobChannels = getThing().getChannels().stream()
                .filter(channel -> JOB_CHANNEL_TYPE.equals(channel.getChannelTypeUID())).toList();

        // Only set channel to 'ON', if it is the default 'CHANNEL_JOB' channel or matches the provided channelId
        for (Channel channel : jobChannels) {
            if (ajaxUiScraping && CHANNEL_JOB.equals(channel.getUID().getId())) {
                // If Ajax scraping is enabled, the default 'CHANNEL_JOB' channel should not be updated here, but only
                // during polling.
                continue;
            }
            if (isJobRunning
                    && (CHANNEL_JOB.equals(channel.getUID().getId()) || channel.getUID().getId().equals(channelId))) {
                setState(channel.getUID().getId(), OnOffType.ON, true);
            } else {
                setState(channel.getUID().getId(), OnOffType.OFF, true);
            }
        }

        if (!ajaxUiScraping) {
            // Skip updating job start and end times if Ajax scraping is enabled,
            // because start and end times are obtained from the Ajax polling

            boolean hasStartTime = settings != null && settings.getStart() != null;
            boolean hasEndTime = settings != null && settings.getEnd() != null;
            boolean hasDuration = settings != null && settings.getDuration() != null;

            int duration = hasDuration ? settings.getDuration() : 60;

            LocalTime startTime = null;
            LocalTime endTime = null;

            if (hasStartTime) {
                startTime = settings.getStartLocalTime();
            }
            if (hasEndTime) {
                endTime = settings.getEndLocalTime();
            }

            if (startTime == null && endTime == null) {
                startTime = LocalTime.now();
            }

            if (startTime != null && endTime == null) {
                endTime = startTime.plusMinutes(duration);
            } else if (startTime == null && endTime != null) {
                startTime = endTime.minusMinutes(duration);
            }

            setState(CHANNEL_JOB_START, toTimeOnlyDateTime(startTime), true);
            setState(CHANNEL_JOB_END, toTimeOnlyDateTime(endTime), true);
        }
    }

    /**
     * Returns editable Thing properties for an endpoint component.
     *
     * @return a mutable copy of the Thing properties
     */
    Map<String, String> editEndpointProperties() {
        return editProperties();
    }

    /**
     * Publishes Thing properties supplied by an endpoint component.
     *
     * @param properties the Thing properties to publish
     */
    void updateEndpointProperties(Map<String, String> properties) {
        updateProperties(properties);
    }

    /**
     * Returns the time zone currently used for mower data.
     *
     * @return the configured mower time zone
     */
    ZoneId getTimeZone() {
        return timeZone;
    }

    /**
     * Returns whether the localized Ajax interface may be used for job and mode state.
     *
     * @return {@code true} when Ajax UI scraping is enabled
     */
    boolean isAjaxUiScraping() {
        return ajaxUiScraping;
    }

    /**
     * Returns whether optional web-interface API operations are enabled.
     *
     * @return {@code true} when unofficial API operations are enabled
     */
    boolean isUnofficialApiEnabled() {
        return useUnofficialApi;
    }

    /**
     * Publishes a Thing update only while the current work remains valid.
     *
     * @param thing the updated Thing
     */
    @Override
    protected void updateThing(Thing thing) {
        if (canPublishState()) {
            super.updateThing(thing);
        }
    }

    /**
     * Publishes a Thing status only while the current work remains valid.
     *
     * @param status the Thing status to publish
     */
    @Override
    protected void updateStatus(ThingStatus status) {
        updateStatus(status, ThingStatusDetail.NONE, null);
    }

    /**
     * Publishes a Thing status only while the current work remains valid.
     *
     * @param status the Thing status to publish
     * @param detail the status detail
     * @param description the status description, or {@code null} if absent
     */
    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
        if (canPublishState()) {
            super.updateStatus(status, detail, description);
        }
    }

    /**
     * Applies Ajax scraping configuration and reconciles the job-time channels.
     *
     * @param enabled whether the feature is enabled
     */
    void configureAjaxChannels(boolean enabled) {
        ajaxUiScraping = enabled;
        updateDynamicChannels(
                Pattern.compile(CHANNEL_JOB_START + "|" + CHANNEL_JOB_END), enabled
                        ? List.of(createChannel(CHANNEL_JOB_START, "DateTime", "jobTimeType", "Mowing Job Start Time"),
                                createChannel(CHANNEL_JOB_END, "DateTime", "jobTimeType", "Mowing Job End Time"))
                        : List.of());
    }

    /**
     * Applies unofficial API configuration and reconciles the push-mode channel.
     *
     * @param enabled whether the feature is enabled
     */
    void configureUnofficialChannels(boolean enabled) {
        useUnofficialApi = enabled;
        updateDynamicChannels(Pattern.compile(CHANNEL_PUSH_MODE),
                enabled ? List.of(createChannel(CHANNEL_PUSH_MODE, "Number", "pushModeType", "Push Service Mode"))
                        : List.of());
    }

    /**
     * Reads a nullable field through a possibly absent response object.
     *
     * @param <T> the response object type
     * @param <V> the field value type
     * @param root the response object, or {@code null} if absent
     * @param getter the accessor for the field
     * @return the field value, or an empty optional if the object or field is absent
     */
    static <T, V> Optional<V> read(@Nullable T root, Function<T, @Nullable V> getter) {
        return Optional.ofNullable(root).flatMap(value -> Optional.ofNullable(getter.apply(value)));
    }

    /**
     * Publishes the reported switch value, or an undefined state if absent.
     *
     * @param channel the channel identifier
     * @param value the reported switch value, or {@code null} if absent
     * @param fullResponse whether an absent state should be published as undefined
     */
    void updateSwitch(String channel, @Nullable Boolean value, boolean fullResponse) {
        setState(channel, value == null ? null : OnOffType.from(value), fullResponse);
    }

    /**
     * Refreshes a requested channel, routing to its source endpoint when appropriate.
     *
     * @param channelUID the channel identifier
     */
    private void refreshChannels(ChannelUID channelUID) {
        String channelId = channelUID.getId();
        if (channelId.startsWith("weather-")) {
            weatherHandler.refreshWeatherInfo();
            return;
        }
        if (channelId.startsWith("remote-")) {
            remoteStartHandler.refreshRemoteInfo();
            return;
        }
        if (channelId.equals(CHANNEL_PUSH_PASSWORD)) {
            updateState(CHANNEL_PUSH_PASSWORD, UnDefType.UNDEF);
            return;
        }
        if (channelId.equals(CHANNEL_PUSH_MODE)) {
            pushHandler.refreshPushMode();
            return;
        }
        if (channelId.startsWith("timer-") && !channelId.equals(CHANNEL_TIMER_NEXT_TIMER)
                && !channelId.equals(CHANNEL_TIMER_STATUS)) {
            timerHandler.refreshTimersInfo();
            return;
        }
        if (channelId.startsWith("battery-")) {
            diagnosticsHandler.refreshBatteryInfo();
            return;
        }
        if (channelId.startsWith("wire-sensor-")) {
            diagnosticsHandler.refreshWireInfo();
            return;
        }
        if (channelId.startsWith("push-")) {
            pushHandler.refreshPushInfo();
            return;
        }
        if (channelId.startsWith("statistics-")) {
            diagnosticsHandler.refreshHourInfo();
            return;
        }
        if (channelId.startsWith("extension-")) {
            pushHandler.refreshExtensionInfo();
            return;
        }
        if (channelId.startsWith("left-drive-") || channelId.startsWith("right-drive-")
                || channelId.startsWith("blade-motor-")) {
            diagnosticsHandler.refreshMotorInfo();
            return;
        }
        if (channelId.startsWith("portal-")) {
            diagnosticsHandler.refreshPortalInfo();
            return;
        }
        if (channelId.startsWith("wlan-") && !channelId.equals(CHANNEL_WLAN_SIGNAL)
                && !channelId.equals(CHANNEL_WLAN_SIGNAL_STRENGTH)) {
            diagnosticsHandler.refreshWlanInfo();
            return;
        }
        switch (channelId) {
            case CHANNEL_MOWER_ID:
            case CHANNEL_MOWER_NAME:
            case CHANNEL_TIMESTAMP:
            case CHANNEL_STATUS_BATTERY:
            case CHANNEL_STATUS:
            case CHANNEL_STATUS_PREVENT:
            case CHANNEL_STATUS_HOME:
            case CHANNEL_STATUS_DURATION:
            case CHANNEL_STATUS_DISTANCE:
            case CHANNEL_STATUS_HOURS:
            case CHANNEL_HEALTH_TEMP:
            case CHANNEL_HEALTH_HUM:
            case CHANNEL_BLADES_QUALITY:
            case CHANNEL_BLADES_REPL_DAYS:
            case CHANNEL_BLADES_USAGE_HOURS:
            case CHANNEL_STATUS_MODE:
            case CHANNEL_MOWER_START:
            case CHANNEL_TIMER_NEXT_TIMER:
            case CHANNEL_TIMER_STATUS:
            case CHANNEL_WLAN_SIGNAL:
            case CHANNEL_WLAN_SIGNAL_STRENGTH:
            case CHANNEL_JOB_START:
            case CHANNEL_JOB_END:
            case CHANNEL_ERROR_CODE:
            case CHANNEL_ERROR_DATE:
            case CHANNEL_ERROR_MESSAGE:
                mowerHandler.refreshMowerInfo();
                break;
            case CHANNEL_JOB:
                mowerHandler.refreshMowerInfo();
                break;
            case CHANNEL_DOOR_ENABLED:
            case CHANNEL_DOOR_STATE:
            case CHANNEL_DOOR_ARRESTED:
                mowerHandler.refreshDoorInfo();
                break;
            case CHANNEL_LAST_ERROR_CODE:
            case CHANNEL_LAST_ERROR_DATE:
            case CHANNEL_LAST_ERROR_MESSAGE:
                diagnosticsHandler.refreshLastErrorInfo();
                break;
            default:
                break;
        }
    }

    /**
     * Represents a time of day on the Unix epoch date in the configured time zone.
     *
     * @param time the time of day to represent
     * @return a date-time with the supplied time and epoch date in the configured time zone
     */
    DateTimeType toTimeOnlyDateTime(LocalTime time) {
        return new DateTimeType(LocalDate.ofEpochDay(0).atTime(time).atZone(timeZone));
    }

    /**
     * Interprets a mower local-clock Unix value using the configured time zone.
     *
     * @param unixTimeSec the mower timestamp as seconds since the Unix epoch
     * @return the timestamp interpreted in the configured mower time zone
     */
    State convertUnixToDateTimeType(String unixTimeSec) {
        // the value in unixTimeSec represents the time on the robot in its configured timezone. However, it does not
        // provide which zone this is. Thus we have to add the zone information from the Thing configuration in order to
        // provide correct results.
        Instant rawInstant = Instant.ofEpochSecond(Long.parseLong(unixTimeSec));

        ZoneOffset offsetToConfiguredZone = timeZone.getRules().getOffset(rawInstant);
        long adjustedTime = rawInstant.getEpochSecond() - offsetToConfiguredZone.getTotalSeconds();
        Instant adjustedInstant = Instant.ofEpochSecond(adjustedTime);

        return new DateTimeType(adjustedInstant);
    }

    /** Configures the mower connection and starts polling for the current Thing configuration. */
    @Override
    public void initialize() {
        cancelPushRefresh();
        cancelPolling();
        disposed = false;
        updateState(CHANNEL_PUSH_PASSWORD, UnDefType.UNDEF);
        RobonectConfig robonectConfig = getConfigAs(RobonectConfig.class);
        configureAjaxChannels(robonectConfig.isAjaxUiScraping());
        configureUnofficialChannels(robonectConfig.isUseUnofficialApi());
        RobonectEndpoint endpoint = new RobonectEndpoint(robonectConfig.getHost(), robonectConfig.getUser(),
                robonectConfig.getPassword());

        String timeZoneString = robonectConfig.getTimezone();
        timeZone = timeZoneProvider.getTimeZone();
        try {
            if (timeZoneString != null) {
                timeZone = ZoneId.of(timeZoneString);
            } else {
                logger.warn("No timezone provided, falling back to the default timezone configured in openHAB: '{}'",
                        timeZoneProvider.getTimeZone());
            }
        } catch (DateTimeException e) {
            logger.warn("Error setting timezone '{}', falling back to the default timezone configured in openHAB: '{}'",
                    timeZoneString, timeZoneProvider.getTimeZone(), e);
        }

        try {
            httpClient.start();
        } catch (Exception e) {
            logger.error("Exception while trying to start HTTP client", e);
            throw new IllegalStateException("Could not create HttpClient");
        }

        createEndpointHandlers(new RobonectClient(httpClient, endpoint));
        PollingSession session = new PollingSession(robonectConfig);
        pollingSession = session;
        pollingJob = requestScheduler.scheduleWithFixedDelay(new MowerChannelPoller(session), 0,
                Math.max(1, robonectConfig.getPollInterval()), TimeUnit.SECONDS);
        int detailedPollInterval = robonectConfig.getDetailedPollInterval();
        if (detailedPollInterval > 0) {
            detailedPollingJob = requestScheduler.scheduleWithFixedDelay(new DetailedMowerChannelPoller(session), 0,
                    detailedPollInterval, TimeUnit.SECONDS);
        }
        String pushUser = robonectConfig.getPushUser();
        String pushPass = robonectConfig.getPushPass();
        if (pushServlet != null && pushUser != null && !pushUser.isBlank() && !pushUser.contains(":")
                && pushPass != null && !pushPass.isBlank()) {
            PushRefresh refresh = new PushRefresh(mowerHandler);
            pushRefresh = refresh;
            pushServlet.registerHandler(getThing().getUID(), refresh, pushUser, pushPass);
        }
    }

    /** Cancels scheduled work and releases the handler connection and push registration. */
    @Override
    public void dispose() {
        disposed = true;
        cancelPushRefresh();
        cancelPolling();
        commandRefreshJobs.values().forEach(CommandRefresh::cancel);
        commandRefreshJobs.clear();
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.warn("Exception while trying to stop HTTP client", e);
        }
    }

    /** Invalidates the current polling session and cancels both polling jobs. */
    private void cancelPolling() {
        pollingSession = null;
        ScheduledFuture<?> normalJob = pollingJob;
        pollingJob = null;
        if (normalJob != null) {
            normalJob.cancel(true);
        }
        ScheduledFuture<?> detailedJob = detailedPollingJob;
        detailedPollingJob = null;
        if (detailedJob != null) {
            detailedJob.cancel(true);
        }
    }

    private void cancelPushRefresh() {
        PushRefresh refresh = pushRefresh;
        pushRefresh = null;
        if (refresh != null && pushServlet != null) {
            pushServlet.unregisterHandler(getThing().getUID(), refresh);
            refresh.cancel();
        }
    }

    @NonNullByDefault
    private class PushRefresh implements Runnable {
        private final RobonectMowerHandler mower;
        private @Nullable ScheduledFuture<?> job;
        private boolean reading;
        private boolean followUp;

        private PushRefresh(RobonectMowerHandler mower) {
            this.mower = mower;
        }

        @Override
        public synchronized void run() {
            if (!isActive()) {
                return;
            }
            if (reading) {
                followUp = true;
            } else if (job == null) {
                schedule();
            }
        }

        private boolean isActive() {
            return !disposed && equals(pushRefresh);
        }

        private void schedule() {
            try {
                job = requestScheduler.schedule(this::refresh, 0, TimeUnit.SECONDS);
            } catch (RejectedExecutionException e) {
                logger.debug("Could not schedule mower refresh after push notification", e);
            }
        }

        private void refresh() {
            synchronized (this) {
                if (!isActive()) {
                    return;
                }
                reading = true;
            }
            sessionActive.set(this::isActive);
            try {
                if (canPublishState()) {
                    mower.refreshMowerInfo();
                }
            } catch (RobonectCommunicationException e) {
                logger.debug("Could not refresh mower state after push notification: {}", e.getMessage());
            } finally {
                sessionActive.remove();
                synchronized (this) {
                    reading = false;
                    job = null;
                    if (followUp && isActive()) {
                        followUp = false;
                        schedule();
                    }
                }
            }
        }

        private synchronized void cancel() {
            ScheduledFuture<?> current = job;
            if (current != null) {
                current.cancel(true);
            }
        }
    }

    /**
     * Replaces the mower client for handler tests.
     *
     * @param robonectClient the client used by endpoint components
     */
    protected void setRobonectClient(RobonectClient robonectClient) {
        createEndpointHandlers(robonectClient);
    }

    /**
     * Creates endpoint handlers that retain the APIs belonging to the supplied client.
     *
     * @param client the client whose endpoint APIs are passed to the handlers
     */
    private void createEndpointHandlers(RobonectClient client) {
        RobonectDiagnosticsHandler diagnostics = new RobonectDiagnosticsHandler(this, client.diagnostics());
        diagnosticsHandler = diagnostics;
        mowerHandler = new RobonectMowerHandler(this, client.mower(), diagnostics);
        pushHandler = new RobonectPushHandler(this, client.push());
        timerHandler = new RobonectTimerHandler(this, client.timer());
        remoteStartHandler = new RobonectRemoteStartHandler(this, client.remoteStart());
        weatherHandler = new RobonectWeatherHandler(this, client.weather());
    }

    /**
     * Allows tests to enable UI scraping without initializing a Thing configuration.
     *
     * @param ajaxUiScraping whether UI scraping is enabled
     */
    protected void setAjaxUiScraping(boolean ajaxUiScraping) {
        this.ajaxUiScraping = ajaxUiScraping;
    }

    @NonNullByDefault
    private static class PollingSession {
        private final long offlineTriggerDelay;
        private final boolean detailedPollingEnabled;
        private boolean pollDriveDiagnosticsNormally;
        private long offlineSince = -1;
        private boolean offlineTimeoutTriggered;
        private boolean loadVersionInfo = true;

        /**
         * Creates a polling session with the configured offline timeout.
         *
         * @param config the Thing configuration for the polling session
         */
        private PollingSession(RobonectConfig config) {
            offlineTriggerDelay = TimeUnit.SECONDS.toMillis(config.getOfflineTimeout());
            detailedPollingEnabled = config.getDetailedPollInterval() > 0;
        }
    }

    @NonNullByDefault
    private class MowerChannelPoller implements Runnable {
        private final PollingSession session;
        private final List<Runnable> refreshes;

        /**
         * Creates a poller for the normal endpoint refreshes of a session.
         *
         * @param session the polling session authorizing the work
         */
        private MowerChannelPoller(PollingSession session) {
            this(session, List.of(() -> {
                if (session.loadVersionInfo) {
                    diagnosticsHandler.refreshVersionInfo();
                    session.loadVersionInfo = false;
                }
            }, () -> {
                session.pollDriveDiagnosticsNormally = false;
                @Nullable
                MowerStatus status = mowerHandler.refreshMowerInfo();
                session.pollDriveDiagnosticsNormally = session.detailedPollingEnabled && switch (status) {
                    case MOWING, SEARCH_CHARGING_STATION, SEARCHING -> true;
                    case null, default -> false;
                };
            }, () -> diagnosticsHandler.refreshWlanInfo(), () -> {
                if (session.pollDriveDiagnosticsNormally) {
                    refreshDriveDiagnostics();
                }
            }));
        }

        /**
         * Creates a poller for the given session and refresh operations.
         *
         * @param session the polling session authorizing the work
         * @param refreshes the endpoint refreshes to run in order
         */
        private MowerChannelPoller(PollingSession session, List<Runnable> refreshes) {
            this.session = session;
            this.refreshes = List.copyOf(refreshes);
        }

        /** Serializes one polling cycle and updates connection status or offline timeout state. */
        @Override
        public final void run() {
            try {
                pollingLock.lockInterruptibly();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            sessionActive.set(() -> session.equals(pollingSession) && !Thread.currentThread().isInterrupted());
            try {
                if (!pollChannels()) {
                    return;
                }
                updateStatus(ThingStatus.ONLINE);
                session.offlineSince = -1;
                session.offlineTimeoutTriggered = false;
            } catch (RobonectCommunicationException rce) {
                if (!canPublishState()) {
                    return;
                }
                if (session.offlineSince < 0) {
                    session.offlineSince = System.currentTimeMillis();
                }
                if (!session.offlineTimeoutTriggered
                        && System.currentTimeMillis() - session.offlineSince > session.offlineTriggerDelay) {
                    updateState(CHANNEL_MOWER_STATUS_OFFLINE_TRIGGER, new StringType("OFFLINE_TIMEOUT"));
                    session.offlineTimeoutTriggered = true;
                }
                logger.debug("Failed to communicate with the mower. Taking it offline.", rce);
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, rce.getMessage());
                session.loadVersionInfo = true;
            } catch (JsonSyntaxException jse) {
                // the module sporadically sends invalid json responses. As this is usually recovered with the
                // next poll interval, we just log it to debug here.
                logger.debug("Failed to parse response.", jse);
            } finally {
                sessionActive.remove();
                pollingLock.unlock();
            }
        }

        /**
         * Runs the session refresh operations while their results remain publishable.
         *
         * @return {@code true} if the polling session remains active after the refreshes
         */
        private boolean pollChannels() {
            for (Runnable refresh : refreshes) {
                if (!canPublishState()) {
                    return false;
                }
                refresh.run();
            }
            return canPublishState();
        }
    }

    /**
     * Builds the list of detailed endpoint refreshes enabled for this session.
     *
     * @param session the polling session that determines the diagnostic schedule
     * @return the ordered endpoint refresh operations for detailed polling
     */
    @NonNullByDefault
    private List<Runnable> createDetailedRefreshes(PollingSession session) {
        List<Runnable> updates = new ArrayList<>();
        if (useUnofficialApi) {
            updates.add(() -> pushHandler.refreshPushMode());
        }

        updates.addAll(List.<Runnable> of(() -> {
            if (!session.pollDriveDiagnosticsNormally) {
                refreshDriveDiagnostics();
            }
        }, () -> diagnosticsHandler.refreshHourInfo(), () -> remoteStartHandler.refreshRemoteInfo(),
                () -> diagnosticsHandler.refreshPortalInfo(), () -> timerHandler.refreshTimersInfo(),
                () -> weatherHandler.refreshWeatherInfo(), () -> pushHandler.refreshExtensionInfo(),
                () -> pushHandler.refreshPushInfo(), () -> mowerHandler.refreshDoorInfo()));
        return updates;
    }

    private void refreshDriveDiagnostics() {
        diagnosticsHandler.refreshBatteryInfo();
        if (canPublishState()) {
            diagnosticsHandler.refreshMotorInfo();
        }
        if (canPublishState()) {
            diagnosticsHandler.refreshWireInfo();
        }
    }

    @NonNullByDefault
    private class DetailedMowerChannelPoller extends MowerChannelPoller {
        /**
         * Creates a poller for the detailed endpoint refreshes of a session.
         *
         * @param session the polling session authorizing the work
         */
        private DetailedMowerChannelPoller(PollingSession session) {
            super(session, RobonectHandler.this.createDetailedRefreshes(session));
        }
    }
}
