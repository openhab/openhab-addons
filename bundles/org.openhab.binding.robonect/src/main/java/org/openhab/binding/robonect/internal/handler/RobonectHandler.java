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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.robonect.internal.RobonectClient;
import org.openhab.binding.robonect.internal.RobonectCommunicationException;
import org.openhab.binding.robonect.internal.RobonectEndpoint;
import org.openhab.binding.robonect.internal.RobonectPushServlet;
import org.openhab.binding.robonect.internal.config.RobonectConfig;
import org.openhab.binding.robonect.internal.model.cmd.ServiceCommand.Operation;
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
 * @author Nico Brüttner - Ajax UI compatibility
 * @author Nico Brüttner - Dynamic telemetry and push trigger channels
 * @author Nico Brüttner - Command responses and delayed state refresh
 * @author Nico Brüttner - Write-only push password channel
 * @author Nico Brüttner - Weather location and condition channels
 * @author Nico Brüttner - Indexed channel labels and quantity metadata
 * @author Nico Brüttner - Independent serialized detailed polling
 * @author Nico Brüttner - Weather interval web interface fallback
 */
public class RobonectHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RobonectHandler.class);

    private ScheduledFuture<?> pollingJob;
    private ScheduledFuture<?> detailedPollingJob;
    private final ScheduledExecutorService requestScheduler;
    private final ReentrantLock pollingLock = new ReentrantLock(true);
    private volatile @Nullable PollingSession pollingSession;
    private final ThreadLocal<BooleanSupplier> pollingActive = new ThreadLocal<>();
    private final Map<String, CommandRefresh> commandRefreshJobs = new ConcurrentHashMap<>();
    private final ThreadLocal<BooleanSupplier> commandRefreshActive = new ThreadLocal<>();
    private volatile boolean disposed;

    private HttpClient httpClient;
    private TimeZoneProvider timeZoneProvider;

    private volatile ZoneId timeZone;

    private RobonectClient robonectClient;
    private final RobonectMowerHandler mowerHandler;
    private final RobonectDiagnosticsHandler diagnosticsHandler;
    private final RobonectPushHandler pushHandler;
    private final RobonectTimerHandler timerHandler;
    private final RobonectRemoteStartHandler remoteStartHandler;
    private final RobonectWeatherHandler weatherHandler;
    private final @Nullable RobonectPushServlet pushServlet;
    private boolean ajaxUiScraping;
    private volatile boolean useUnofficialApi;

    private static final Pattern BATTERY_CHANNEL_PATTERN = Pattern.compile("battery-\\d+-.+");
    private static final Pattern WIRE_CHANNEL_PATTERN = Pattern.compile("wire-sensor-\\d+-.+");
    private static final Pattern PUSH_TRIGGER_CHANNEL_PATTERN = Pattern.compile("push-trigger-(.+)-(name|enter|leave)");
    private static final Pattern REMOTE_CHANNEL_PATTERN = Pattern
            .compile("remote-([1-5])-(name|distance|proportion|visible|path|corridor-cut)");

    public RobonectHandler(Thing thing, HttpClientFactory httpClientFactory, TimeZoneProvider timeZoneProvider) {
        this(thing, httpClientFactory, timeZoneProvider, null);
    }

    public RobonectHandler(Thing thing, HttpClientFactory httpClientFactory, TimeZoneProvider timeZoneProvider,
            @Nullable RobonectPushServlet pushServlet) {
        this(thing, httpClientFactory, timeZoneProvider, pushServlet, null);
    }

    RobonectHandler(Thing thing, HttpClientFactory httpClientFactory, TimeZoneProvider timeZoneProvider,
            @Nullable RobonectPushServlet pushServlet, @Nullable ScheduledExecutorService commandScheduler) {
        super(thing);
        requestScheduler = commandScheduler == null ? scheduler : commandScheduler;
        httpClient = httpClientFactory
                .createHttpClient(ThingWebClientUtil.buildWebClientConsumerName(thing.getUID(), null));
        this.timeZoneProvider = timeZoneProvider;
        timeZone = timeZoneProvider.getTimeZone();
        this.pushServlet = pushServlet;
        diagnosticsHandler = new RobonectDiagnosticsHandler(this);
        mowerHandler = new RobonectMowerHandler(this, diagnosticsHandler);
        pushHandler = new RobonectPushHandler(this);
        timerHandler = new RobonectTimerHandler(this);
        remoteStartHandler = new RobonectRemoteStartHandler(this);
        weatherHandler = new RobonectWeatherHandler(this);
    }

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

    private void sendCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_MOWER_NAME:
                if (command instanceof StringType stringCommand) {
                    mowerHandler.updateName(stringCommand);
                } else {
                    logger.debug("Got name update of type {} but StringType is expected.",
                            command.getClass().getName());
                }
                break;

            case CHANNEL_STATUS_MODE:
                if (command instanceof StringType) {
                    mowerHandler.setMowerMode(command);
                } else {
                    logger.debug("Got job remote start update of type {} but StringType is expected.",
                            command.getClass().getName());
                }
                break;

            case CHANNEL_MOWER_START:
                if (command instanceof OnOffType onOffCommand) {
                    mowerHandler.handleStartStop(onOffCommand);
                } else {
                    logger.debug("Got stopped update of type {} but OnOffType is expected.",
                            command.getClass().getName());
                }
                break;

            case CHANNEL_JOB:
                if (command instanceof OnOffType onOffCommand) {
                    mowerHandler.handleJobCommand(channelUID, onOffCommand);
                } else {
                    logger.debug("Got job update of type {} but OnOffType is expected.", command.getClass().getName());
                }
                break;

            case CHANNEL_RESET_ERROR:
                if (command == OnOffType.ON) {
                    mowerHandler.resetErrors();
                } else {
                    logger.debug("Got error reset command {} but On is expected.", command);
                }
                break;
            case CHANNEL_SERVICE_REBOOT:
                mowerHandler.performServiceOperation(command, Operation.REBOOT);
                break;
            case CHANNEL_SERVICE_SHUTDOWN:
                mowerHandler.performServiceOperation(command, Operation.SHUTDOWN);
                break;
            case CHANNEL_SERVICE_SLEEP:
                mowerHandler.performServiceOperation(command, Operation.SLEEP);
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
                pushHandler.updatePush(channelUID.getId(), command);
                break;
            case CHANNEL_PUSH_MODE:
                pushHandler.updatePushMode(command);
                break;
            default:
                if (REMOTE_CHANNEL_PATTERN.matcher(channelUID.getId()).matches()) {
                    remoteStartHandler.handleCommand(channelUID.getId(), command);
                } else if (channelUID.getId().startsWith("timer-")) {
                    timerHandler.handleCommand(channelUID.getId(), command);
                } else if (channelUID.getId().startsWith("push-trigger-")) {
                    pushHandler.updatePushTrigger(channelUID.getId(), command);
                }
                break;
        }
    }

    Channel createChannel(String id, String itemType, String channelType, String label) {
        Channel existing = getThing().getChannel(id);
        if (existing != null) {
            return ChannelBuilder.create(existing).withLabel(label).build();
        }
        return ChannelBuilder.create(new ChannelUID(getThing().getUID(), id), itemType)
                .withType(new ChannelTypeUID(BINDING_ID, channelType)).withLabel(label).build();
    }

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

    void cancelCommandRefresh(String endpoint) {
        CommandRefresh previous = commandRefreshJobs.remove(endpoint);
        if (previous != null) {
            previous.cancel();
        }
    }

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

        private void cancel() {
            ScheduledFuture<?> current = job;
            if (current != null) {
                current.cancel(true);
            }
        }
    }

    boolean canPublishState() {
        // Responses may finish after disposal, reinitialization, or a newer command superseding a delayed read.
        BooleanSupplier active = commandRefreshActive.get();
        BooleanSupplier poll = pollingActive.get();
        return !disposed && (active == null || active.getAsBoolean()) && (poll == null || poll.getAsBoolean());
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        if (canPublishState()) {
            super.updateState(channelUID, state);
        }
    }

    void setState(String channelId, State state) {
        updateState(new ChannelUID(getThing().getUID(), channelId), state);
    }

    RobonectClient getRobonectClient() {
        return robonectClient;
    }

    Map<String, String> editEndpointProperties() {
        return editProperties();
    }

    void updateEndpointProperties(Map<String, String> properties) {
        updateProperties(properties);
    }

    ZoneId getTimeZone() {
        return timeZone;
    }

    boolean isAjaxUiScraping() {
        return ajaxUiScraping;
    }

    boolean isUnofficialApiEnabled() {
        return useUnofficialApi;
    }

    @Override
    protected void updateThing(Thing thing) {
        if (canPublishState()) {
            super.updateThing(thing);
        }
    }

    @Override
    protected void updateStatus(ThingStatus status) {
        updateStatus(status, ThingStatusDetail.NONE, null);
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
        if (canPublishState()) {
            super.updateStatus(status, detail, description);
        }
    }

    void configureAjaxChannels(boolean enabled) {
        ajaxUiScraping = enabled;
        updateDynamicChannels(
                Pattern.compile(CHANNEL_JOB_START + "|" + CHANNEL_JOB_END), enabled
                        ? List.of(createChannel(CHANNEL_JOB_START, "DateTime", "jobTimeType", "Mowing Job Start Time"),
                                createChannel(CHANNEL_JOB_END, "DateTime", "jobTimeType", "Mowing Job End Time"))
                        : List.of());
    }

    void configureUnofficialChannels(boolean enabled) {
        useUnofficialApi = enabled;
        updateDynamicChannels(Pattern.compile(CHANNEL_PUSH_MODE),
                enabled ? List.of(createChannel(CHANNEL_PUSH_MODE, "Number", "pushModeType", "Push Service Mode"))
                        : List.of());
    }

    void updateSwitch(String channel, @Nullable Boolean value) {
        if (value != null)
            updateState(channel, OnOffType.from(value));
    }

    private void refreshChannels(ChannelUID channelUID) {
        String channelId = channelUID.getId();
        if (channelId.startsWith("weather-")) {
            weatherHandler.refreshWeatherInfo();
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
        if (BATTERY_CHANNEL_PATTERN.matcher(channelId).matches() || channelId.startsWith("battery-")) {
            diagnosticsHandler.refreshBatteryInfo();
            return;
        }
        if (WIRE_CHANNEL_PATTERN.matcher(channelId).matches()) {
            diagnosticsHandler.refreshWireInfo();
            return;
        }
        if (PUSH_TRIGGER_CHANNEL_PATTERN.matcher(channelId).matches() || channelId.startsWith("push-")) {
            pushHandler.refreshPushInfo();
            return;
        }
        if (channelId.startsWith("statistics-")) {
            diagnosticsHandler.refreshHourInfo();
            return;
        }
        switch (channelId) {
            case CHANNEL_MOWER_NAME:
            case CHANNEL_STATUS_BATTERY:
            case CHANNEL_STATUS:
            case CHANNEL_STATUS_PREVENT:
            case CHANNEL_STATUS_HOME:
            case CHANNEL_STATUS_DURATION:
            case CHANNEL_STATUS_HOURS:
            case CHANNEL_STATUS_MODE:
            case CHANNEL_MOWER_START:
            case CHANNEL_TIMER_NEXT_TIMER:
            case CHANNEL_TIMER_STATUS:
            case CHANNEL_WLAN_SIGNAL:
                mowerHandler.refreshMowerInfo();
                break;
            case CHANNEL_WLAN_ENABLED:
            case CHANNEL_WLAN_SSID:
            case CHANNEL_WLAN_IP:
            case CHANNEL_WLAN_AP_ENABLED:
            case CHANNEL_WLAN_AP_SSID:
                diagnosticsHandler.refreshWlanInfo();
                break;
            case CHANNEL_JOB:
                mowerHandler.refreshMowerInfo();
                break;
            case CHANNEL_DOOR_ENABLED:
            case CHANNEL_DOOR_STATE:
            case CHANNEL_DOOR_ARRESTED:
                mowerHandler.refreshDoorInfo();
                break;
            default:
            case CHANNEL_LAST_ERROR_CODE:
            case CHANNEL_LAST_ERROR_DATE:
            case CHANNEL_LAST_ERROR_MESSAGE:
                diagnosticsHandler.refreshLastErrorInfo();
                break;
        }
    }

    DateTimeType toTimeOnlyDateTime(LocalTime time) {
        return new DateTimeType(LocalDate.ofEpochDay(0).atTime(time).atZone(timeZone));
    }

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

    @Override
    public void initialize() {
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

        robonectClient = new RobonectClient(httpClient, endpoint);
        if (pushServlet != null) {
            pushServlet.registerHandler(getThing().getUID(), this);
        }
        PollingSession session = new PollingSession(robonectConfig);
        pollingSession = session;
        pollingJob = requestScheduler.scheduleWithFixedDelay(new MowerChannelPoller(session), 0,
                Math.max(1, robonectConfig.getPollInterval()), TimeUnit.SECONDS);
        int detailedPollInterval = robonectConfig.getDetailedPollInterval();
        if (detailedPollInterval > 0) {
            detailedPollingJob = requestScheduler.scheduleWithFixedDelay(new DetailedMowerChannelPoller(session), 0,
                    detailedPollInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        cancelPolling();
        commandRefreshJobs.values().forEach(CommandRefresh::cancel);
        commandRefreshJobs.clear();
        if (pushServlet != null) {
            pushServlet.unregisterHandler(getThing().getUID());
        }
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.warn("Exception while trying to stop HTTP client", e);
        }
    }

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

    /** Requests an immediate status refresh after a Robonect push notification. */
    public void handlePushNotification() {
        scheduler.execute(() -> {
            try {
                mowerHandler.refreshMowerInfo();
            } catch (RobonectCommunicationException e) {
                logger.debug("Could not refresh mower state after push notification: {}", e.getMessage());
            }
        });
    }

    /**
     * method to inject the robonect client to be used in test cases to allow mocking.
     *
     * @param robonectClient
     */
    protected void setRobonectClient(RobonectClient robonectClient) {
        this.robonectClient = robonectClient;
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
        private long offlineSince = -1;
        private boolean offlineTimeoutTriggered;
        private boolean loadVersionInfo = true;

        private PollingSession(RobonectConfig config) {
            offlineTriggerDelay = TimeUnit.SECONDS.toMillis(config.getOfflineTimeout());
        }
    }

    @NonNullByDefault
    private class MowerChannelPoller implements Runnable {
        private final PollingSession session;
        private final List<Runnable> refreshes;

        private MowerChannelPoller(PollingSession session) {
            this(session, List.of(() -> {
                if (session.loadVersionInfo) {
                    diagnosticsHandler.refreshVersionInfo();
                    session.loadVersionInfo = false;
                }
            }, mowerHandler::refreshMowerInfo, diagnosticsHandler::refreshWlanInfo));
        }

        private MowerChannelPoller(PollingSession session, List<Runnable> refreshes) {
            this.session = session;
            this.refreshes = List.copyOf(refreshes);
        }

        @Override
        public final void run() {
            try {
                pollingLock.lockInterruptibly();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            pollingActive.set(() -> session.equals(pollingSession) && !Thread.currentThread().isInterrupted());
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
                pollingActive.remove();
                pollingLock.unlock();
            }
        }

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

    @NonNullByDefault
    private List<Runnable> createDetailedRefreshes() {
        List<Runnable> updates = new ArrayList<>();
        if (useUnofficialApi) {
            updates.add(pushHandler::refreshPushMode);
        }

        updates.addAll(List.<Runnable> of(diagnosticsHandler::refreshBatteryInfo, diagnosticsHandler::refreshMotorInfo,
                diagnosticsHandler::refreshHourInfo, remoteStartHandler::refreshRemoteInfo,
                diagnosticsHandler::refreshWireInfo, diagnosticsHandler::refreshPortalInfo,
                timerHandler::refreshTimersInfo, weatherHandler::refreshWeatherInfo, pushHandler::refreshExtensionInfo,
                pushHandler::refreshPushInfo, mowerHandler::refreshDoorInfo));
        return updates;
    }

    @NonNullByDefault
    private class DetailedMowerChannelPoller extends MowerChannelPoller {
        private DetailedMowerChannelPoller(PollingSession session) {
            super(session, RobonectHandler.this.createDetailedRefreshes());
        }
    }
}
