/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.airparif.internal.handler;

import static org.openhab.binding.airparif.internal.AirParifBindingConstants.*;
import static org.openhab.binding.airparif.internal.api.AirParifApi.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.binding.airparif.internal.AirParifException;
import org.openhab.binding.airparif.internal.api.AirParifApi.Pollen;
import org.openhab.binding.airparif.internal.api.AirParifDto.Bulletin;
import org.openhab.binding.airparif.internal.api.AirParifDto.Episode;
import org.openhab.binding.airparif.internal.api.AirParifDto.ItineraireResponse;
import org.openhab.binding.airparif.internal.api.AirParifDto.KeyInfo;
import org.openhab.binding.airparif.internal.api.AirParifDto.PollensResponse;
import org.openhab.binding.airparif.internal.api.AirParifDto.Route;
import org.openhab.binding.airparif.internal.api.AirParifDto.Version;
import org.openhab.binding.airparif.internal.api.PollenAlertLevel;
import org.openhab.binding.airparif.internal.api.Pollutant;
import org.openhab.binding.airparif.internal.config.BridgeConfiguration;
import org.openhab.binding.airparif.internal.deserialization.AirParifDeserializer;
import org.openhab.binding.airparif.internal.discovery.AirParifDiscoveryService;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AirParifBridgeHandler} is the handler for OpenUV API and connects it
 * to the webservice.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class AirParifBridgeHandler extends BaseBridgeHandler implements HandlerUtils {
    private static final int REQUEST_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(30);
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final String AQ_JOB = "Air Quality Bulletin";
    private static final String POLLENS_JOB = "Pollens Update";
    private static final String EPISODE_JOB = "Episode";

    private final Logger logger = LoggerFactory.getLogger(AirParifBridgeHandler.class);
    private final Map<String, ScheduledFuture<?>> jobs = new HashMap<>();
    private final AirParifDeserializer deserializer;
    private final HttpClient httpClient;

    private BridgeConfiguration config = new BridgeConfiguration();
    private @Nullable PollensResponse pollens;

    public AirParifBridgeHandler(Bridge bridge, HttpClient httpClient, AirParifDeserializer deserializer) {
        super(bridge);
        this.deserializer = deserializer;
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing AirParif bridge handler.");
        config = getConfigAs(BridgeConfiguration.class);
        if (config.apikey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-unknown-apikey");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::initiateConnexion);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the AirParif bridge handler.");
        cleanJobs();
    }

    public synchronized String executeUri(URI uri, HttpMethod method, @Nullable String payload)
            throws AirParifException {
        logger.debug("executeUrl: {} ", uri);

        Request request = httpClient.newRequest(uri).method(method).timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .header(HttpHeader.ACCEPT, MediaType.APPLICATION_JSON).header("X-Api-Key", config.apikey);

        if (payload != null && HttpMethod.POST.equals(method)) {
            InputStream stream = new ByteArrayInputStream(payload.getBytes(DEFAULT_CHARSET));
            try (InputStreamContentProvider inputStreamContentProvider = new InputStreamContentProvider(stream)) {
                request.content(inputStreamContentProvider, MediaType.APPLICATION_JSON);
            }
            logger.trace(" -with payload : {} ", payload);
        }

        try {
            ContentResponse response = request.send();

            Code statusCode = HttpStatus.getCode(response.getStatus());

            if (statusCode == Code.OK) {
                String content = new String(response.getContent(), DEFAULT_CHARSET);
                logger.trace("executeUrl: {} returned {}", uri, content);
                return content;
            } else if (statusCode == Code.FORBIDDEN) {
                throw new AirParifException("@text/offline.config-error-invalid-apikey");
            }
            throw new AirParifException("Error '%s' requesting: %s", statusCode.getMessage(), uri.toString());
        } catch (TimeoutException | ExecutionException e) {
            throw new AirParifException(e, "Exception while calling %s: %s", request.getURI(), e.getMessage());
        } catch (InterruptedException e) {
            throw new AirParifException(e, "Execution interrupted: %s", e.getMessage());
        }
    }

    public synchronized <T> T executeUri(URI uri, Class<T> clazz) throws AirParifException {
        String content = executeUri(uri, HttpMethod.GET, null);
        return deserializer.deserialize(clazz, content);
    }

    public synchronized <T> T executeUri(URI uri, Class<T> clazz, String payload) throws AirParifException {
        String content = executeUri(uri, HttpMethod.POST, payload);
        return deserializer.deserialize(clazz, content);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("The AirParif bridge does not handle commands");
    }

    private void initiateConnexion() {
        Version version;
        try { // This is only intended to validate communication with the server
            version = executeUri(VERSION_URI, Version.class);
        } catch (AirParifException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }

        KeyInfo keyInfo;
        try { // This validates the api key value
            keyInfo = executeUri(KEY_INFO_URI, KeyInfo.class);
        } catch (AirParifException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        thing.setProperty("api-version", version.version());
        thing.setProperty("key-expiration", keyInfo.expiration().toString());
        thing.setProperty("scopes", keyInfo.scopes().stream().map(e -> e.name()).collect(Collectors.joining(",")));
        logger.debug("The api key is valid until {}", keyInfo.expiration().toString());
        updateStatus(ThingStatus.ONLINE);

        ThingUID thingUID = thing.getUID();

        schedule(POLLENS_JOB, () -> updatePollens(new ChannelGroupUID(thingUID, GROUP_POLLENS)), Duration.ofSeconds(1));
        schedule(AQ_JOB, () -> updateDailyAQBulletin(new ChannelGroupUID(thingUID, GROUP_AQ_BULLETIN),
                new ChannelGroupUID(thingUID, GROUP_AQ_BULLETIN_TOMORROW)), Duration.ofSeconds(2));
        schedule(EPISODE_JOB, () -> updateEpisode(new ChannelGroupUID(thingUID, GROUP_DAILY)), Duration.ofSeconds(3));
    }

    private void updatePollens(ChannelGroupUID pollensGroupUID) {
        PollensResponse localPollens;
        try {
            localPollens = executeUri(POLLENS_URI, PollensResponse.class);
        } catch (AirParifException e) {
            logger.warn("Error updating pollens data: {}", e.getMessage());
            return;
        }

        updateState(new ChannelUID(pollensGroupUID, CHANNEL_COMMENT), Objects.requireNonNull(
                localPollens.getComment().map(comment -> (State) new StringType(comment)).orElse(UnDefType.NULL)));
        updateState(new ChannelUID(pollensGroupUID, CHANNEL_BEGIN_VALIDITY), Objects.requireNonNull(
                localPollens.getBeginValidity().map(begin -> (State) new DateTimeType(begin)).orElse(UnDefType.NULL)));
        updateState(new ChannelUID(pollensGroupUID, CHANNEL_END_VALIDITY), Objects.requireNonNull(
                localPollens.getEndValidity().map(end -> (State) new DateTimeType(end)).orElse(UnDefType.NULL)));

        long delay = localPollens.getValidityDuration().getSeconds();
        // if delay is null, update in 3600 seconds
        delay += delay == 0 ? 3600 : 60;
        schedule(POLLENS_JOB, () -> updatePollens(pollensGroupUID), Duration.ofSeconds(delay));

        // Send pollens information to childs
        getThing().getThings().stream().map(Thing::getHandler).filter(LocationHandler.class::isInstance)
                .map(LocationHandler.class::cast).forEach(locHand -> locHand.setPollens(localPollens));
        pollens = localPollens;
    }

    private void updateDailyAQBulletin(ChannelGroupUID todayGroupUID, ChannelGroupUID tomorrowGroupUID) {
        Bulletin bulletin;
        try {
            bulletin = executeUri(PREV_BULLETIN_URI, Bulletin.class);
        } catch (AirParifException e) {
            logger.warn("Error updating Air Quality Bulletin: {}", e.getMessage());
            return;
        }

        Set.of(bulletin.today(), bulletin.tomorrow()).stream().forEach(aq -> {
            ChannelGroupUID groupUID = aq.isToday() ? todayGroupUID : tomorrowGroupUID;
            updateState(new ChannelUID(groupUID, CHANNEL_COMMENT),
                    !aq.available() ? UnDefType.NULL : new StringType(aq.bulletin().fr()));

            aq.concentrations().forEach(measure -> {
                Pollutant pollutant = measure.pollutant();
                String cName = pollutant.name().toLowerCase() + "-";
                updateState(new ChannelUID(groupUID, cName + "min"),
                        aq.available() ? measure.getMin() : UnDefType.NULL);
                updateState(new ChannelUID(groupUID, cName + "max"),
                        aq.available() ? measure.getMax() : UnDefType.NULL);
            });
        });

        logger.debug("Rescheduling daily air quality bulletin job tomorrow morning");
        schedule(AQ_JOB, () -> updateDailyAQBulletin(todayGroupUID, tomorrowGroupUID), untilTomorrowMorning());
    }

    private void updateEpisode(ChannelGroupUID dailyGroupUID) {
        Episode episode;
        try {
            episode = executeUri(EPISODES_URI, Episode.class);
        } catch (AirParifException e) {
            logger.warn("Error updating Episode: {}", e.getMessage());
            return;
        }

        logger.debug("The episode is {}", episode);

        updateState(new ChannelUID(dailyGroupUID, CHANNEL_MESSAGE), new StringType(episode.message().fr()));
        updateState(new ChannelUID(dailyGroupUID, CHANNEL_TOMORROW), new StringType(episode.message().fr()));

        schedule(EPISODE_JOB, () -> updateEpisode(dailyGroupUID), untilTomorrowMorning());
    }

    private Duration untilTomorrowMorning() {
        return Duration.between(ZonedDateTime.now(),
                ZonedDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS).plusMinutes(1));
    }

    public @Nullable Route getConcentrations(String location) {
        String[] elements = location.split(",");
        if (elements.length >= 2) {
            String req = "{\"itineraires\": [{\"date\": \"%s\",\"longlats\": [[%s,%s]]}],\"polluants\": [\"indice\",\"no2\",\"o3\",\"pm25\",\"pm10\"]}";
            req = req.formatted(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS), elements[1], elements[0]);
            try {
                ItineraireResponse result = executeUri(HORAIR_URI, ItineraireResponse.class, req);
                return result.routes()[0];
            } catch (AirParifException e) {
                logger.warn("Error getting detailed concentrations: {}", e.getMessage());
            }
        } else {
            logger.warn("Wrong localisation as input : {}", location);
        }
        return null;
    }

    public Map<Pollen, PollenAlertLevel> requestPollens(String department) {
        PollensResponse localPollens = pollens;
        return localPollens != null ? localPollens.getDepartment(department) : Map.of();
    }

    @Override
    public @Nullable Bridge getBridge() {
        return super.getBridge();
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Map<String, ScheduledFuture<?>> getJobs() {
        return jobs;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(AirParifDiscoveryService.class);
    }
}
