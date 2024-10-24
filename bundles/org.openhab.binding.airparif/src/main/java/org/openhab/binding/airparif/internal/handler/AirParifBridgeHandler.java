/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.ExecutionException;
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
import org.openhab.binding.airparif.internal.AirParifIconProvider;
import org.openhab.binding.airparif.internal.api.AirParifDto.Bulletin;
import org.openhab.binding.airparif.internal.api.AirParifDto.Episode;
import org.openhab.binding.airparif.internal.api.AirParifDto.ItineraireResponse;
import org.openhab.binding.airparif.internal.api.AirParifDto.KeyInfo;
import org.openhab.binding.airparif.internal.api.AirParifDto.PollensResponse;
import org.openhab.binding.airparif.internal.api.AirParifDto.Route;
import org.openhab.binding.airparif.internal.api.AirParifDto.Version;
import org.openhab.binding.airparif.internal.api.ColorMap;
import org.openhab.binding.airparif.internal.config.BridgeConfiguration;
import org.openhab.binding.airparif.internal.deserialization.AirParifDeserializer;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
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
public class AirParifBridgeHandler extends BaseBridgeHandler {
    private static final int REQUEST_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(30);
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final Logger logger = LoggerFactory.getLogger(AirParifBridgeHandler.class);
    private final AirParifDeserializer deserializer;
    private final AirParifIconProvider iconProvider;
    private final HttpClient httpClient;

    private BridgeConfiguration config = new BridgeConfiguration();

    private @Nullable ScheduledFuture<?> pollensJob;
    private @Nullable ScheduledFuture<?> dailyJob;

    public AirParifBridgeHandler(Bridge bridge, HttpClient httpClient, AirParifDeserializer deserializer,
            AirParifIconProvider iconProvider) {
        super(bridge);
        this.deserializer = deserializer;
        this.iconProvider = iconProvider;
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
        scheduler.execute(this::initiateConnexion);
    }

    private @Nullable ScheduledFuture<?> cancelFuture(@Nullable ScheduledFuture<?> job) {
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
        return null;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the AirParif bridge handler.");

        pollensJob = cancelFuture(pollensJob);
        dailyJob = cancelFuture(dailyJob);
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
            String content = new String(response.getContent(), DEFAULT_CHARSET);
            throw new AirParifException("Error '%s' requesting: %s", statusCode.getMessage(), uri.toString());
        } catch (TimeoutException | ExecutionException e) {
            throw new AirParifException(e, "Exception while calling %s", request.getURI());
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
        logger.debug("The AirParif bridge does not handles commands");
    }

    private void initiateConnexion() {
        Version version;
        try { // This does validate communication with the server
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

        getThing().setProperty("api-version", version.version());
        getThing().setProperty("key-expiration", keyInfo.expiration().toString());
        getThing().setProperty("scopes", keyInfo.scopes().stream().map(e -> e.name()).collect(Collectors.joining(",")));
        logger.info("The api key is valid until {}", keyInfo.expiration().toString());
        updateStatus(ThingStatus.ONLINE);

        try {
            ColorMap map = executeUri(PREV_COLORS_URI, ColorMap.class);
            logger.debug("The color map is {}", map.toString());
            iconProvider.setColorMap(map);
        } catch (AirParifException e) {
            logger.warn("Error reading ColorMap: {]", e.getMessage());
        }

        pollensJob = scheduler.schedule(this::updatePollens, 1, TimeUnit.SECONDS);
        dailyJob = scheduler.schedule(this::updateDaily, 2, TimeUnit.SECONDS);
    }

    private void updateDaily() {
        try {
            Bulletin bulletin = executeUri(PREV_BULLETIN_URI, Bulletin.class);
            logger.debug("The bulletin is {}", bulletin.today().dayDescription());

            Set.of(bulletin.today(), bulletin.tomorrow()).stream().forEach(aq -> {
                String groupName = aq.previsionDate().equals(LocalDate.now()) ? GROUP_AQ_BULLETIN
                        : GROUP_AQ_BULLETIN_TOMORROW + "#";
                updateState(groupName + CHANNEL_COMMENT,
                        !aq.available() ? UnDefType.UNDEF : new StringType(aq.bulletin().fr()));
                aq.concentrations().forEach(measure -> {
                    String cName = groupName + measure.pollutant().name().toLowerCase();
                    updateState(cName + "-min", !aq.available() ? UnDefType.UNDEF
                            : new QuantityType<>(measure.min(), measure.pollutant().unit));
                    updateState(cName + "-max", !aq.available() ? UnDefType.UNDEF
                            : new QuantityType<>(measure.max(), measure.pollutant().unit));
                });
            });

            Episode episode = executeUri(EPISODES_URI, Episode.class);
            logger.debug("The episode is {}", episode);

            // if (episode.active()) {
            // updateState(GROUP_DAILY + "#" + CHANNEL_MESSAGE, new StringType(episode.message().fr()));
            // updateState(GROUP_DAILY + "#" + CHANNEL_TOMORROW, new StringType(episode.message().fr()));
            // }

            ZonedDateTime tomorrowMorning = ZonedDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS).plusMinutes(1);
            long delay = Duration.between(ZonedDateTime.now(), tomorrowMorning).getSeconds();
            logger.debug("Rescheduling daily job tomorrow morning");
            dailyJob = scheduler.schedule(this::updateDaily, delay, TimeUnit.SECONDS);
        } catch (AirParifException e) {
            logger.warn("Error update pollens data: {}", e.getMessage());
        }
    }

    private void updatePollens() {
        try {
            PollensResponse pollens = executeUri(POLLENS_URI, PollensResponse.class);

            pollens.getComment()
                    .ifPresent(comment -> updateState(GROUP_POLLENS + "#" + CHANNEL_COMMENT, new StringType(comment)));
            pollens.getBeginValidity().ifPresent(
                    begin -> updateState(GROUP_POLLENS + "#" + CHANNEL_BEGIN_VALIDITY, new DateTimeType(begin)));
            pollens.getEndValidity().ifPresent(end -> {
                updateState(GROUP_POLLENS + "#" + CHANNEL_END_VALIDITY, new DateTimeType(end));
                logger.info("Pollens bulletin valid until {}", end);
                long delay = Duration.between(ZonedDateTime.now(), end).getSeconds();
                if (delay < 0) {
                    // what if the bulletin was not updated and the delay is passed ?
                    delay = 3600;
                    logger.debug("Update time of the bulletin is in the past - will retry in one hour");
                } else {
                    delay += 60;
                }

                pollensJob = scheduler.schedule(this::updatePollens, delay, TimeUnit.SECONDS);
            });
            getThing().getThings().stream().map(Thing::getHandler).filter(LocationHandler.class::isInstance)
                    .map(LocationHandler.class::cast).forEach(locHand -> locHand.setPollens(pollens));
        } catch (AirParifException e) {
            logger.warn("Error updating pollens data: {}", e.getMessage());
        }
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
}
