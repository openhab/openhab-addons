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
package org.openhab.binding.enphase.internal.handler;

import java.net.HttpCookie;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.enphase.internal.EnvoyConfiguration;
import org.openhab.binding.enphase.internal.dto.EnvoyEnergyDTO;
import org.openhab.binding.enphase.internal.dto.IvpMetersDTO;
import org.openhab.binding.enphase.internal.dto.IvpMetersReadingsDTO;
import org.openhab.binding.enphase.internal.dto.PdmEnergyDTO;
import org.openhab.binding.enphase.internal.dto.PdmEnergyDTO.PdmProductionDTO;
import org.openhab.binding.enphase.internal.exception.EnphaseException;
import org.openhab.binding.enphase.internal.exception.EntrezJwtInvalidException;
import org.openhab.binding.enphase.internal.exception.EnvoyConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Envoy connector using Entrez portal to obtain an JWT access token.
 *
 * @author Joe Inkenbrandt - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored entrez specific code in it's own sub connector class
 */
@NonNullByDefault
public class EnvoyEntrezConnector extends EnvoyConnector {

    // private static final String SESSION = "session";
    private static final String HTTPS = "https://";
    private static final String LOGIN_URL = "/auth/check_jwt";
    private static final String SESSION_COOKIE_NAME = "session";
    private static final String IVP_PDM_ENERGY_URL = "/ivp/pdm/energy";
    private static final String IVP_METERS_URL = "/ivp/meters";
    private static final String IVP_METERS_READINGS_URL = "/ivp/meters/readings";
    private static final String METER_STATE_ENABLED = "enabled";
    private static final String MEASUREMENT_TYPE_PRODUCTION = "production";
    private static final String MEASUREMENT_TYPE_NET_CONSUMPTION = "net-consumption";
    private static final String MEASUREMENT_TYPE_TOTAL_CONSUMPTION = "total-consumption";
    private static final EnvoyEnergyDTO NO_DATA = new EnvoyEnergyDTO();

    private final Logger logger = LoggerFactory.getLogger(EnvoyEntrezConnector.class);

    private final EntrezConnector entrezConnector;

    private @Nullable String sessionKey;
    private @Nullable String sessionId;
    private String jwt = "";
    private Instant jwtExpirationTime = Instant.now();

    public EnvoyEntrezConnector(final HttpClient httpClient) {
        super(httpClient, HTTPS);
        entrezConnector = new EntrezConnector(httpClient);
    }

    @Override
    public String setConfiguration(final EnvoyConfiguration configuration) {
        final String message;

        if (!configuration.autoJwt) {
            message = check(configuration.jwt, "No autoJWT enabled, but jwt parameter is empty.");

            if (message.isEmpty()) {
                jwt = configuration.jwt;
            }
        } else {
            message = Stream
                    .of(check(configuration.username, "Username parameter is empty"),
                            check(configuration.password, "Password parameter is empty"),
                            check(configuration.siteName, "siteName parameter is empty"))
                    .filter(s -> !s.isEmpty()).collect(Collectors.joining(", "));
        }
        if (!message.isEmpty()) {
            return message;
        }
        return super.setConfiguration(configuration);
    }

    private String check(final String property, final String message) {
        return property.isBlank() ? message : "";
    }

    @Override
    public EnvoyEnergyDTO getProduction() throws EnphaseException {
        final PdmProductionDTO production = retrieveData(IVP_PDM_ENERGY_URL, this::jsonToPdmEnergyDTO).production;

        return production == null || production.pcu == null ? NO_DATA : production.pcu;
    }

    private @Nullable PdmEnergyDTO jsonToPdmEnergyDTO(final String json) {
        return gson.fromJson(json, PdmEnergyDTO.class);
    }

    /**
     * Retrieves consumption data from the {@code /ivp/meters/readings} endpoint instead of the
     * {@code /api/v1/consumption} endpoint used by the base connector. On firmware version 7 and higher the
     * {@code /api/v1/consumption} (and {@code /ivp/pdm/energy}) aggregated consumption values can stop tracking and
     * report a constant, stale value after a firmware update, while the meter readings keep reporting live data.
     * <p>
     * When the gateway has a {@code total-consumption} meter its reading is used directly. Otherwise the household load
     * is derived from the {@code production} and {@code net-consumption} meters ({@code total = production + net}).
     * <p>
     * Only the instantaneous power (and the lifetime energy when a {@code total-consumption} meter is present) are
     * available from this endpoint; the daily and seven-day energy buckets are not provided and are reported as
     * undefined.
     *
     * @return the consumption data from the Envoy gateway
     */
    @Override
    public EnvoyEnergyDTO getConsumption() throws EnphaseException {
        final Map<Long, String> meters = retrieveData(IVP_METERS_URL, this::jsonToEnabledMeters);
        final Map<String, IvpMetersReadingsDTO> readings = retrieveData(IVP_METERS_READINGS_URL,
                json -> jsonToReadingsByType(json, meters));

        final IvpMetersReadingsDTO totalConsumption = readings.get(MEASUREMENT_TYPE_TOTAL_CONSUMPTION);
        if (totalConsumption != null) {
            return consumptionDTO((int) Math.round(totalConsumption.activePower),
                    (int) Math.round(totalConsumption.actEnergyDlvd));
        }
        final IvpMetersReadingsDTO production = readings.get(MEASUREMENT_TYPE_PRODUCTION);
        final IvpMetersReadingsDTO netConsumption = readings.get(MEASUREMENT_TYPE_NET_CONSUMPTION);
        if (production != null && netConsumption != null) {
            return consumptionDTO((int) Math.round(production.activePower + netConsumption.activePower), null);
        }
        throw new EnvoyConnectionException("No consumption meter data available from the Envoy.");
    }

    private Map<Long, String> jsonToEnabledMeters(final String json) {
        final IvpMetersDTO @Nullable [] meters = gson.fromJson(json, IvpMetersDTO[].class);
        final Map<Long, String> map = new HashMap<>();

        if (meters != null) {
            for (final IvpMetersDTO meter : meters) {
                if (METER_STATE_ENABLED.equals(meter.state) && meter.measurementType != null) {
                    map.put(meter.eid, meter.measurementType);
                }
            }
        }
        return map;
    }

    private Map<String, IvpMetersReadingsDTO> jsonToReadingsByType(final String json, final Map<Long, String> meters) {
        final IvpMetersReadingsDTO @Nullable [] readings = gson.fromJson(json, IvpMetersReadingsDTO[].class);
        final Map<String, IvpMetersReadingsDTO> map = new HashMap<>();

        if (readings != null) {
            for (final IvpMetersReadingsDTO reading : readings) {
                final String measurementType = meters.get(reading.eid);
                if (measurementType != null) {
                    map.put(measurementType, reading);
                }
            }
        }
        return map;
    }

    private EnvoyEnergyDTO consumptionDTO(final int wattsNow, final @Nullable Integer wattHoursLifetime) {
        final EnvoyEnergyDTO dto = new EnvoyEnergyDTO();

        dto.wattsNow = wattsNow;
        dto.wattHoursLifetime = wattHoursLifetime;
        // The /ivp/meters/readings endpoint does not provide daily or seven-day energy buckets.
        dto.wattHoursToday = null;
        dto.wattHoursSevenDays = null;
        return dto;
    }

    @Override
    protected void constructRequest(final Request request) throws EnphaseException {
        // Check if we need a new session ID
        if (!checkSessionId()) {
            sessionId = getNewSessionId();
        }
        logger.trace("Retrieving data from '{}' with sessionID '{}'", request.getURI(), sessionId);
        request.cookie(new HttpCookie(sessionKey, sessionId));
    }

    private boolean checkSessionId() {
        if (this.sessionId == null) {
            return false;
        }
        final URI uri = URI.create(HTTPS + configuration.hostname + LOGIN_URL);

        final Request request = httpClient.newRequest(uri).method(HttpMethod.GET)
                .cookie(new HttpCookie(sessionKey, this.sessionId)).timeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        final ContentResponse response;

        try {
            response = send(request);
        } catch (final EnvoyConnectionException e) {
            logger.debug("Session ID ({}) Check TimeoutException: {}", sessionId, e.getMessage());
            return false;
        }
        if (response.getStatus() != 200) {
            logger.debug("Session ID ({}) Home Response: {}", sessionId, response.getStatus());
            return false;
        }

        logger.debug("Home Response: {}", response.getContentAsString());
        return true;
    }

    private @Nullable String getNewSessionId() throws EnphaseException {
        if (jwt.isEmpty() || isExpired()) {
            if (configuration.autoJwt) {
                jwt = entrezConnector.retrieveJwt(configuration.username, configuration.password,
                        configuration.siteName, configuration.serialNumber);
                jwtExpirationTime = Instant.ofEpochSecond(entrezConnector.processJwt(jwt).getBody().getExp());
            } else {
                new EntrezJwtInvalidException("Accesstoken expired. Configure new token or configure autoJwt.");
            }
        }
        return loginWithJWT(jwt);
    }

    private boolean isExpired() {
        return jwtExpirationTime.isBefore(Instant.now());
    }

    /**
     * This function attempts to get a sessionId from the local gateway by submitting the JWT given.
     *
     * @return the sessionId or null of no session id could be retrieved.
     */
    private @Nullable String loginWithJWT(final String jwt) throws EnvoyConnectionException, EntrezJwtInvalidException {
        final URI uri = URI.create(HTTPS + configuration.hostname + LOGIN_URL);

        // Authorization: Bearer
        final Request request = httpClient.newRequest(uri).method(HttpMethod.GET).accept("application/json")
                .header("Authorization", "Bearer " + jwt).timeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        final ContentResponse response = send(request);

        if (response.getStatus() == 200 && response.getHeaders().contains(HttpHeader.SET_COOKIE)) {
            final List<HttpCookie> cookies = HttpCookie.parse(response.getHeaders().get(HttpHeader.SET_COOKIE));

            for (final HttpCookie c : cookies) {
                final String cookieKey = String.valueOf(c.getName()).toLowerCase(Locale.ROOT);

                if (cookieKey.startsWith(SESSION_COOKIE_NAME)) {
                    logger.debug("Got SessionID: {}", c.getValue());
                    sessionKey = cookieKey;
                    return c.getValue();
                }
            }
            logger.debug(
                    "Failed to find cookie with the JWT token from the Enphase portal. Maybe Enphase changed the website.");
            throw new EntrezJwtInvalidException(
                    "Unable to obtain jwt key from Ephase website. Manully configuring the JWT might make it work. Please report this issue.");
        }
        logger.debug("Failed to login to Envoy. Evoy returned status: {}. Response from Envoy: {}",
                response.getStatus(), response.getContentAsString());
        throw new EntrezJwtInvalidException(
                "Could not login to Envoy with the access token. Envoy returned status:" + response.getStatus());
    }
}
