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

import static org.openhab.binding.airparif.internal.api.AirParifApi.*;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.binding.airparif.internal.AirParifException;
import org.openhab.binding.airparif.internal.api.AirParifDto.Bulletin;
import org.openhab.binding.airparif.internal.api.AirParifDto.Episode;
import org.openhab.binding.airparif.internal.api.AirParifDto.KeyInfo;
import org.openhab.binding.airparif.internal.api.AirParifDto.Pollens;
import org.openhab.binding.airparif.internal.api.AirParifDto.PollensResponse;
import org.openhab.binding.airparif.internal.api.AirParifDto.Version;
import org.openhab.binding.airparif.internal.api.ColorMap;
import org.openhab.binding.airparif.internal.config.BridgeConfiguration;
import org.openhab.binding.airparif.internal.deserialization.AirParifDeserializer;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
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
    private final HttpClient httpClient;

    private BridgeConfiguration config = new BridgeConfiguration();

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
        initiateConnexion();
    }

    public synchronized String executeUri(URI uri) throws AirParifException {
        logger.debug("executeUrl: {} ", uri);

        Request request = httpClient.newRequest(uri).method(HttpMethod.GET)
                .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .header(HttpHeader.ACCEPT, MediaType.APPLICATION_JSON).header("X-Api-Key", config.apikey);

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
            throw new AirParifException(e, "Exception while calling %s", request.getURI());
        } catch (InterruptedException e) {
            throw new AirParifException(e, "Execution interrupted: %s", e.getMessage());
        }
    }

    public synchronized <T> T executeUri(URI uri, Class<T> clazz) throws AirParifException {
        String content = executeUri(uri);
        return deserializer.deserialize(clazz, content);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("The AirParif bridge does not handles commands");
    }

    private void initiateConnexion() {
        Version version;
        KeyInfo keyInfo;

        try { // This does validate communication with the server
            version = executeUri(VERSION_URI, Version.class);
        } catch (AirParifException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }

        try { // This validates the api key value
            keyInfo = executeUri(KEY_INFO_URI, KeyInfo.class);
        } catch (AirParifException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        getThing().setProperty("api-version", version.version());
        getThing().setProperty("key-expiration", keyInfo.expiration().toString());
        logger.info("The api key is valid until {}", keyInfo.expiration().toString());
        getThing().setProperty("scopes", keyInfo.scopes().stream().map(e -> e.name()).collect(Collectors.joining(",")));
        updateStatus(ThingStatus.ONLINE);

        try {
            ColorMap map = executeUri(PREV_COLORS_URI, ColorMap.class);
            logger.info("The color map is {}", map.toString());

            Bulletin bulletin = executeUri(PREV_BULLETIN_URI, Bulletin.class);
            logger.info("The bulletin is {}", bulletin.today().dayDescription());

            Episode episode = executeUri(EPISODES_URI, Episode.class);
            logger.info("The bulletin is {}", episode);

            Pollens pollens = executeUri(POLLENS_URI, PollensResponse.class).data().get(0);
            logger.info("The pollens are {}", pollens);
            LocalDate begin = pollens.beginValidity();
            LocalDate end = pollens.endValidity();

            String response = executeUri(POLLENS_DEPT_BUILDER.path("78").build());
            logger.info("The pollens 78 {}", response);
        } catch (AirParifException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }
    }

}
