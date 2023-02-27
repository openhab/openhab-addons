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
package org.openhab.binding.netatmo.internal.api;

import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.*;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FloodLightMode;
import org.openhab.binding.netatmo.internal.api.dto.Home;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent.NAEventsDataResponse;
import org.openhab.binding.netatmo.internal.api.dto.Ping;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all Security related endpoints
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SecurityApi extends RestManager {
    private final Logger logger = LoggerFactory.getLogger(SecurityApi.class);

    public SecurityApi(ApiBridgeHandler apiClient) {
        super(apiClient, FeatureArea.SECURITY);
    }

    /**
     * Dissociates a webhook from a user.
     *
     * @throws NetatmoException If fail to call the API, e.g. server error or deserializing
     */
    public void dropWebhook() throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_DROP_WEBHOOK);
        post(uriBuilder, ApiResponse.Ok.class, null);
    }

    /**
     * Links a callback url to a user.
     *
     * @param uri Your webhook callback url (required)
     * @throws NetatmoException If fail to call the API, e.g. server error or deserializing
     */
    public boolean addwebhook(URI uri) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_ADD_WEBHOOK, PARAM_URL, uri.toString());
        post(uriBuilder, ApiResponse.Ok.class, null);
        return true;
    }

    private List<HomeEvent> getEvents(@Nullable Object... params) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(SUB_PATH_GET_EVENTS, params);
        BodyResponse<Home> body = get(uriBuilder, NAEventsDataResponse.class).getBody();
        if (body != null) {
            Home home = body.getElement();
            if (home != null) {
                return home.getEvents();
            }
        }
        throw new NetatmoException("home should not be null");
    }

    public List<HomeEvent> getHomeEvents(String homeId, @Nullable ZonedDateTime freshestEventTime)
            throws NetatmoException {
        List<HomeEvent> events = getEvents(PARAM_HOME_ID, homeId);

        // we have to rewind to the latest event just after oldestKnown
        HomeEvent oldestRetrieved = events.get(events.size() - 1);
        while (freshestEventTime != null && oldestRetrieved.getTime().isAfter(freshestEventTime)) {
            events.addAll(getEvents(PARAM_HOME_ID, homeId, PARAM_EVENT_ID, oldestRetrieved.getId()));
            oldestRetrieved = events.get(events.size() - 1);
        }

        // Remove unneeded events being before oldestKnown
        return events.stream().filter(event -> freshestEventTime == null || event.getTime().isAfter(freshestEventTime))
                .sorted(Comparator.comparing(HomeEvent::getTime).reversed()).collect(Collectors.toList());
    }

    public List<HomeEvent> getPersonEvents(String homeId, String personId) throws NetatmoException {
        return getEvents(PARAM_HOME_ID, homeId, PARAM_PERSON_ID, personId, PARAM_OFFSET, 1);
    }

    public List<HomeEvent> getDeviceEvents(String homeId, String deviceId, String deviceType) throws NetatmoException {
        return getEvents(PARAM_HOME_ID, homeId, PARAM_DEVICE_ID, deviceId, PARAM_DEVICES_TYPE, deviceType);
    }

    public @Nullable String ping(String vpnUrl) {
        UriBuilder uriBuilder = UriBuilder.fromUri(vpnUrl).path(PATH_COMMAND).path(SUB_PATH_PING);
        try {
            return get(uriBuilder, Ping.class).getStatus();
        } catch (NetatmoException e) {
            logger.debug("Pinging {} failed : {}", vpnUrl, e.getMessage());
            return null;
        }
    }

    public void changeStatus(String localCameraURL, boolean setOn) throws NetatmoException {
        UriBuilder uriBuilder = UriBuilder.fromUri(localCameraURL).path(PATH_COMMAND).path(SUB_PATH_CHANGESTATUS);
        uriBuilder.queryParam(PARAM_STATUS, setOn ? "on" : "off");
        post(uriBuilder, ApiResponse.Ok.class, null);
    }

    public void changeFloodLightMode(String homeId, String cameraId, FloodLightMode mode) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(PATH_STATE);
        String payload = PAYLOAD_FLOODLIGHT.formatted(homeId, cameraId, mode.name().toLowerCase());
        post(uriBuilder, ApiResponse.Ok.class, payload);
    }

    public void setPersonAwayStatus(String homeId, String personId, boolean away) throws NetatmoException {
        UriBuilder uriBuilder = getApiUriBuilder(away ? SUB_PATH_PERSON_AWAY : SUB_PATH_PERSON_HOME);
        String payload = String.format(away ? PAYLOAD_PERSON_AWAY : PAYLOAD_PERSON_HOME, homeId, personId);
        post(uriBuilder, ApiResponse.Ok.class, payload);
    }
}
