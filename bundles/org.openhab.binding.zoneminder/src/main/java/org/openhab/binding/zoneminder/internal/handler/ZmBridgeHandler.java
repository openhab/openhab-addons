/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.zoneminder.internal.handler;

import static org.openhab.binding.zoneminder.internal.ZmBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.zoneminder.internal.ZmStateDescriptionOptionsProvider;
import org.openhab.binding.zoneminder.internal.config.ZmBridgeConfig;
import org.openhab.binding.zoneminder.internal.discovery.MonitorDiscoveryService;
import org.openhab.binding.zoneminder.internal.dto.EventDTO;
import org.openhab.binding.zoneminder.internal.dto.EventSummaryDTO;
import org.openhab.binding.zoneminder.internal.dto.EventsDTO;
import org.openhab.binding.zoneminder.internal.dto.MonitorDTO;
import org.openhab.binding.zoneminder.internal.dto.MonitorItemDTO;
import org.openhab.binding.zoneminder.internal.dto.MonitorStateDTO;
import org.openhab.binding.zoneminder.internal.dto.MonitorStatusDTO;
import org.openhab.binding.zoneminder.internal.dto.MonitorsDTO;
import org.openhab.binding.zoneminder.internal.dto.VersionDTO;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ZmBridgeHandler} represents the Zoneminder server. It handles all communication
 * with the Zoneminder server.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class ZmBridgeHandler extends BaseBridgeHandler {

    private static final int MONITOR_REFRESH_INTERVAL_SECONDS = 10;
    private static final int MONITOR_REFRESH_STARTUP_DELAY_SECONDS = 5;

    private static final int API_TIMEOUT_MSEC = 10000;

    private static final String LOGIN_PATH = "/api/host/login.json";

    private static final String STREAM_IMAGE = "single";
    private static final String STREAM_VIDEO = "jpeg";

    private static final List<String> EMPTY_LIST = Collections.emptyList();

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private final Logger logger = LoggerFactory.getLogger(ZmBridgeHandler.class);

    private @Nullable Future<?> refreshMonitorsJob;

    private List<Monitor> savedMonitors = new ArrayList<>();

    private String host = "";
    private boolean useSSL;
    private @Nullable String portNumber;
    private String urlPath = DEFAULT_URL_PATH;
    private int monitorRefreshInterval;
    private boolean backgroundDiscoveryEnabled;
    private int defaultAlarmDuration;
    private @Nullable Integer defaultImageRefreshInterval;

    private final HttpClient httpClient;
    private final ZmStateDescriptionOptionsProvider stateDescriptionProvider;

    private ZmAuth zmAuth;

    // Maintain mapping of handler and monitor id
    private final Map<String, ZmMonitorHandler> monitorHandlers = new ConcurrentHashMap<>();

    public ZmBridgeHandler(Bridge thing, HttpClient httpClient,
            ZmStateDescriptionOptionsProvider stateDescriptionProvider) {
        super(thing);
        this.httpClient = httpClient;
        this.stateDescriptionProvider = stateDescriptionProvider;
        // Default to use no authentication
        zmAuth = new ZmAuth(this);
    }

    @Override
    public void initialize() {
        ZmBridgeConfig config = getConfigAs(ZmBridgeConfig.class);

        Integer value;
        value = config.refreshInterval;
        monitorRefreshInterval = value == null ? MONITOR_REFRESH_INTERVAL_SECONDS : value;

        value = config.defaultAlarmDuration;
        defaultAlarmDuration = value == null ? DEFAULT_ALARM_DURATION_SECONDS : value;

        defaultImageRefreshInterval = config.defaultImageRefreshInterval;

        backgroundDiscoveryEnabled = config.discoveryEnabled;
        logger.debug("Bridge: Background discovery is {}", backgroundDiscoveryEnabled ? "ENABLED" : "DISABLED");

        host = config.host;
        useSSL = config.useSSL.booleanValue();
        portNumber = config.portNumber != null ? Integer.toString(config.portNumber) : null;
        urlPath = "/".equals(config.urlPath) ? "" : config.urlPath;

        // If user and password are configured, then use Zoneminder authentication
        if (config.user != null && config.pass != null) {
            zmAuth = new ZmAuth(this, config.user, config.pass);
        }
        if (isHostValid()) {
            updateStatus(ThingStatus.ONLINE);
            scheduleRefreshJob();
        }
    }

    @Override
    public void dispose() {
        cancelRefreshJob();
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        String monitorId = (String) childThing.getConfiguration().get(CONFIG_MONITOR_ID);
        monitorHandlers.put(monitorId, (ZmMonitorHandler) childHandler);
        logger.debug("Bridge: Monitor handler was initialized for {} with id {}", childThing.getUID(), monitorId);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        String monitorId = (String) childThing.getConfiguration().get(CONFIG_MONITOR_ID);
        monitorHandlers.remove(monitorId);
        logger.debug("Bridge: Monitor handler was disposed for {} with id {}", childThing.getUID(), monitorId);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_IMAGE_MONITOR_ID:
                handleMonitorIdCommand(command, CHANNEL_IMAGE_MONITOR_ID, CHANNEL_IMAGE_URL, STREAM_IMAGE);
                break;
            case CHANNEL_VIDEO_MONITOR_ID:
                handleMonitorIdCommand(command, CHANNEL_VIDEO_MONITOR_ID, CHANNEL_VIDEO_URL, STREAM_VIDEO);
                break;
        }
    }

    private void handleMonitorIdCommand(Command command, String monitorIdChannelId, String urlChannelId, String type) {
        if (command instanceof RefreshType || command == OnOffType.OFF) {
            updateState(monitorIdChannelId, UnDefType.UNDEF);
            updateState(urlChannelId, UnDefType.UNDEF);
        } else if (command instanceof StringType) {
            String id = command.toString();
            if (isMonitorIdValid(id)) {
                updateState(urlChannelId, new StringType(buildStreamUrl(id, type)));
            } else {
                updateState(monitorIdChannelId, UnDefType.UNDEF);
                updateState(urlChannelId, UnDefType.UNDEF);
            }
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(MonitorDiscoveryService.class);
    }

    public boolean isBackgroundDiscoveryEnabled() {
        return backgroundDiscoveryEnabled;
    }

    public Integer getDefaultAlarmDuration() {
        return defaultAlarmDuration;
    }

    public @Nullable Integer getDefaultImageRefreshInterval() {
        return defaultImageRefreshInterval;
    }

    public List<Monitor> getSavedMonitors() {
        return savedMonitors;
    }

    public Gson getGson() {
        return GSON;
    }

    public void setFunction(String id, MonitorFunction function) {
        if (!zmAuth.isAuthorized()) {
            return;
        }
        logger.debug("Bridge: Setting monitor {} function to {}", id, function);
        executePost(buildUrl(String.format("/api/monitors/%s.json", id)),
                String.format("Monitor[Function]=%s", function.toString()));
    }

    public void setEnabled(String id, OnOffType enabled) {
        if (!zmAuth.isAuthorized()) {
            return;
        }
        logger.debug("Bridge: Setting monitor {} to {}", id, enabled);
        executePost(buildUrl(String.format("/api/monitors/%s.json", id)),
                String.format("Monitor[Enabled]=%s", enabled == OnOffType.ON ? "1" : "0"));
    }

    public void setAlarmOn(String id) {
        if (!zmAuth.isAuthorized()) {
            return;
        }
        logger.debug("Bridge: Turning alarm ON for monitor {}", id);
        setAlarm(buildUrl(String.format("/api/monitors/alarm/id:%s/command:on.json", id)));
    }

    public void setAlarmOff(String id) {
        if (!zmAuth.isAuthorized()) {
            return;
        }
        logger.debug("Bridge: Turning alarm OFF for monitor {}", id);
        setAlarm(buildUrl(String.format("/api/monitors/alarm/id:%s/command:off.json", id)));
    }

    public @Nullable RawType getImage(String id, @Nullable Integer imageRefreshIntervalSeconds) {
        Integer localRefreshInterval = imageRefreshIntervalSeconds;
        if (localRefreshInterval == null || localRefreshInterval.intValue() < 1 || !zmAuth.isAuthorized()) {
            return null;
        }
        // Call should timeout just before the refresh interval
        int timeout = Math.min((localRefreshInterval * 1000) - 500, API_TIMEOUT_MSEC);
        Request request = httpClient.newRequest(buildStreamUrl(id, STREAM_IMAGE));
        request.method(HttpMethod.GET);
        request.timeout(timeout, TimeUnit.MILLISECONDS);

        String errorMsg;
        try {
            ContentResponse response = request.send();
            if (response.getStatus() == HttpStatus.OK_200) {
                RawType image = new RawType(response.getContent(), response.getHeaders().get(HttpHeader.CONTENT_TYPE));
                return image;
            } else {
                errorMsg = String.format("HTTP GET failed: %d, %s", response.getStatus(), response.getReason());
            }
        } catch (TimeoutException e) {
            errorMsg = String.format("TimeoutException: Call to Zoneminder API timed out after {} msec", timeout);
        } catch (ExecutionException e) {
            errorMsg = String.format("ExecutionException: %s", e.getMessage());
        } catch (InterruptedException e) {
            errorMsg = String.format("InterruptedException: %s", e.getMessage());
            Thread.currentThread().interrupt();
        }
        logger.debug("{}", errorMsg);
        return null;
    }

    @SuppressWarnings("null")
    private synchronized List<Monitor> getMonitors() {
        List<Monitor> monitorList = new ArrayList<>();
        if (!zmAuth.isAuthorized()) {
            return monitorList;
        }
        try {
            String response = executeGet(buildUrl("/api/monitors.json"));
            MonitorsDTO monitorsDTO = GSON.fromJson(response, MonitorsDTO.class);
            if (monitorsDTO != null && monitorsDTO.monitorItems != null) {
                List<StateOption> options = new ArrayList<>();
                for (MonitorItemDTO monitorItemDTO : monitorsDTO.monitorItems) {
                    MonitorDTO monitorDTO = monitorItemDTO.monitor;
                    MonitorStatusDTO monitorStatusDTO = monitorItemDTO.monitorStatus;
                    if (monitorDTO != null && monitorStatusDTO != null) {
                        Monitor monitor = new Monitor(monitorDTO.id, monitorDTO.name, monitorDTO.function,
                                monitorDTO.enabled, monitorStatusDTO.status);
                        extractEventCounts(monitor, monitorItemDTO);
                        monitor.setImageUrl(buildStreamUrl(monitorDTO.id, STREAM_IMAGE));
                        monitor.setVideoUrl(buildStreamUrl(monitorDTO.id, STREAM_VIDEO));
                        monitorList.add(monitor);
                        options.add(new StateOption(monitorDTO.id, "Monitor " + monitorDTO.id));
                    }
                }
                // Update state options
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_IMAGE_MONITOR_ID),
                        options);
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_VIDEO_MONITOR_ID),
                        options);
                // Only update alarm and event info for monitors whose handlers are initialized
                Set<String> ids = monitorHandlers.keySet();
                for (Monitor m : monitorList) {
                    if (ids.contains(m.getId())) {
                        m.setState(getState(m.getId()));
                        m.setLastEvent(getLastEvent(m.getId()));
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Bridge: JsonSyntaxException: {}", e.getMessage(), e);
        }
        return monitorList;
    }

    private void extractEventCounts(Monitor monitor, MonitorItemDTO monitorItemDTO) {
        /*
         * The Zoneminder API changed in version 1.36.x such that the event counts moved from the
         * monitor object to a new event summary object. Therefore, if the event summary object
         * exists in the JSON response, pull the event counts from that object, otherwise get the
         * counts from the monitor object.
         */
        if (monitorItemDTO.eventSummary != null) {
            EventSummaryDTO eventSummaryDTO = monitorItemDTO.eventSummary;
            monitor.setHourEvents(eventSummaryDTO.hourEvents);
            monitor.setDayEvents(eventSummaryDTO.dayEvents);
            monitor.setWeekEvents(eventSummaryDTO.weekEvents);
            monitor.setMonthEvents(eventSummaryDTO.monthEvents);
            monitor.setTotalEvents(eventSummaryDTO.totalEvents);
        } else {
            MonitorDTO monitorDTO = monitorItemDTO.monitor;
            monitor.setHourEvents(monitorDTO.hourEvents);
            monitor.setDayEvents(monitorDTO.dayEvents);
            monitor.setWeekEvents(monitorDTO.weekEvents);
            monitor.setMonthEvents(monitorDTO.monthEvents);
            monitor.setTotalEvents(monitorDTO.totalEvents);
        }
    }

    @SuppressWarnings("null")
    private @Nullable Event getLastEvent(String id) {
        if (!zmAuth.isAuthorized()) {
            return null;
        }
        try {
            List<String> parameters = new ArrayList<>();
            parameters.add("sort=StartTime");
            parameters.add("direction=desc");
            parameters.add("limit=1");
            String response = executeGet(buildUrlWithParameters(
                    String.format("/api/events/index/MonitorId:%s/Name!=:New%%20Event.json", id), parameters));
            EventsDTO events = GSON.fromJson(response, EventsDTO.class);
            if (events != null && events.eventsList != null && events.eventsList.size() == 1) {
                EventDTO e = events.eventsList.get(0).event;
                Event event = new Event(e.eventId, e.name, e.cause, e.notes, e.startTime, e.endTime);
                event.setFrames(e.frames);
                event.setAlarmFrames(e.alarmFrames);
                event.setLength(e.length);
                return event;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Bridge: JsonSyntaxException: {}", e.getMessage(), e);
        }
        return null;
    }

    private @Nullable VersionDTO getVersion() {
        if (!zmAuth.isAuthorized()) {
            return null;
        }
        VersionDTO version = null;
        try {
            String response = executeGet(buildUrl("/api/host/getVersion.json"));
            version = GSON.fromJson(response, VersionDTO.class);
        } catch (JsonSyntaxException e) {
            logger.debug("Bridge: JsonSyntaxException: {}", e.getMessage(), e);
        }
        return version;
    }

    private void setAlarm(String url) {
        executeGet(url);
    }

    @SuppressWarnings("null")
    private MonitorState getState(String id) {
        if (!zmAuth.isAuthorized()) {
            return MonitorState.UNKNOWN;
        }
        try {
            String response = executeGet(buildUrl(String.format("/api/monitors/alarm/id:%s/command:status.json", id)));
            MonitorStateDTO monitorState = GSON.fromJson(response, MonitorStateDTO.class);
            if (monitorState != null) {
                MonitorState state = monitorState.state;
                return state != null ? state : MonitorState.UNKNOWN;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Bridge: JsonSyntaxException: {}", e.getMessage(), e);
        }
        return MonitorState.UNKNOWN;
    }

    public @Nullable String executeGet(String url) {
        try {
            long startTime = System.currentTimeMillis();
            String response = HttpUtil.executeUrl("GET", url, API_TIMEOUT_MSEC);
            logger.trace("Bridge: Http GET of '{}' returned '{}' in {} ms", url, response,
                    System.currentTimeMillis() - startTime);
            return response;
        } catch (IOException e) {
            logger.debug("Bridge: IOException on GET request, url='{}': {}", url, e.getMessage());
        }
        return null;
    }

    private @Nullable String executePost(String url, String content) {
        return executePost(url, content, "application/x-www-form-urlencoded");
    }

    public @Nullable String executePost(String url, String content, String contentType) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            long startTime = System.currentTimeMillis();
            String response = HttpUtil.executeUrl("POST", url, inputStream, contentType, API_TIMEOUT_MSEC);
            logger.trace("Bridge: Http POST content '{}' to '{}' returned: {} in {} ms", content, url, response,
                    System.currentTimeMillis() - startTime);
            return response;
        } catch (IOException e) {
            logger.debug("Bridge: IOException on POST request, url='{}': {}", url, e.getMessage());
        }
        return null;
    }

    public String buildLoginUrl() {
        return buildBaseUrl(LOGIN_PATH).toString();
    }

    public String buildLoginUrl(String tokenParameter) {
        StringBuilder sb = buildBaseUrl(LOGIN_PATH);
        sb.append(tokenParameter);
        return sb.toString();
    }

    private String buildStreamUrl(String id, String streamType) {
        List<String> parameters = new ArrayList<>();
        parameters.add(String.format("mode=%s", streamType));
        parameters.add(String.format("monitor=%s", id));
        return buildUrlWithParameters("/cgi-bin/zms", parameters);
    }

    private String buildUrl(String path) {
        return buildUrlWithParameters(path, EMPTY_LIST);
    }

    private String buildUrlWithParameters(String path, List<String> parameters) {
        StringBuilder sb = buildBaseUrl(path);
        String joiner = "?";
        for (String parameter : parameters) {
            sb.append(joiner).append(parameter);
            joiner = "&";
        }
        if (zmAuth.usingAuthorization()) {
            sb.append(joiner).append("token=").append(zmAuth.getAccessToken());
        }
        return sb.toString();
    }

    private StringBuilder buildBaseUrl(String path) {
        StringBuilder sb = new StringBuilder();
        sb.append(useSSL ? "https://" : "http://");
        sb.append(host);
        if (portNumber != null) {
            sb.append(":").append(portNumber);
        }
        sb.append(urlPath);
        sb.append(path);
        return sb;
    }

    private boolean isMonitorIdValid(String id) {
        return savedMonitors.stream().filter(monitor -> id.equals(monitor.getId())).findAny().isPresent();
    }

    private boolean isHostValid() {
        logger.debug("Bridge: Checking for valid Zoneminder host: {}", host);
        VersionDTO version = getVersion();
        if (version != null) {
            if (checkSoftwareVersion(version.version) && checkApiVersion(version.apiVersion)) {
                return true;
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Can't get version information");
        }
        return false;
    }

    private boolean checkSoftwareVersion(@Nullable String softwareVersion) {
        logger.debug("Bridge: Zoneminder software version is {}", softwareVersion);
        if (softwareVersion != null) {
            String[] versionParts = softwareVersion.split("\\.");
            if (versionParts.length >= 2) {
                try {
                    int versionMajor = Integer.parseInt(versionParts[0]);
                    int versionMinor = Integer.parseInt(versionParts[1]);
                    if (versionMajor == 1 && versionMinor >= 34) {
                        logger.debug("Bridge: Zoneminder software version check OK");
                        return true;
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String
                                .format("Current Zoneminder version: %s. Requires version >= 1.34.0", softwareVersion));
                    }
                } catch (NumberFormatException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            String.format("Badly formatted version number: %s", softwareVersion));
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("Can't parse software version: %s", softwareVersion));
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Software version is null");
        }
        return false;
    }

    private boolean checkApiVersion(@Nullable String apiVersion) {
        logger.debug("Bridge: Zoneminder API version is {}", apiVersion);
        if (apiVersion != null) {
            String[] versionParts = apiVersion.split("\\.");
            if (versionParts.length >= 2) {
                try {
                    int versionMajor = Integer.parseInt(versionParts[0]);
                    if (versionMajor >= 2) {
                        logger.debug("Bridge: Zoneminder API version check OK");
                        return true;
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String
                                .format("Requires API version >= 2.0. This Zoneminder is API version {}", apiVersion));
                    }
                } catch (NumberFormatException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            String.format("Badly formatted API version: %s", apiVersion));
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("Can't parse API version: %s", apiVersion));
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API version is null");
        }
        return false;
    }

    @SuppressWarnings("null")
    private void refreshMonitors() {
        List<Monitor> monitors = getMonitors();
        savedMonitors = monitors;
        for (Monitor monitor : monitors) {
            ZmMonitorHandler handler = monitorHandlers.get(monitor.getId());
            if (handler != null) {
                handler.updateStatus(monitor);
            }
        }
    }

    private void scheduleRefreshJob() {
        logger.debug("Bridge: Scheduling monitors refresh job");
        cancelRefreshJob();
        refreshMonitorsJob = scheduler.scheduleWithFixedDelay(this::refreshMonitors,
                MONITOR_REFRESH_STARTUP_DELAY_SECONDS, monitorRefreshInterval, TimeUnit.SECONDS);
    }

    private void cancelRefreshJob() {
        Future<?> localRefreshThermostatsJob = refreshMonitorsJob;
        if (localRefreshThermostatsJob != null) {
            localRefreshThermostatsJob.cancel(true);
            logger.debug("Bridge: Canceling monitors refresh job");
            refreshMonitorsJob = null;
        }
    }
}
