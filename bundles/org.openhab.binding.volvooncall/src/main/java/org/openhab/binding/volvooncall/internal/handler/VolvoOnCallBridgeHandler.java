/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.volvooncall.internal.handler;

import static org.openhab.binding.volvooncall.internal.VolvoOnCallBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.volvooncall.internal.config.VolvoOnCallBridgeConfiguration;
import org.openhab.binding.volvooncall.internal.dto.CustomerAccounts;
import org.openhab.binding.volvooncall.internal.dto.PostResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link VolvoOnCallBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VolvoOnCallBridgeHandler extends BaseBridgeHandler {
    private static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(20);
    private final Logger logger = LoggerFactory.getLogger(VolvoOnCallBridgeHandler.class);
    private final Properties httpHeader = new Properties();
    private final List<ScheduledFuture<?>> pendingActions = new Stack<>();
    private final Gson gson;

    private @NonNullByDefault({}) CustomerAccounts customerAccount;

    public VolvoOnCallBridgeHandler(Bridge bridge) {
        super(bridge);

        httpHeader.put("cache-control", "no-cache");
        httpHeader.put("X-OS-Type", "Android");
        httpHeader.put("X-Originator-Type", "App");
        httpHeader.put("Content-Type", JSON_CONTENT_TYPE);

        gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class,
                        (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime
                                .parse(json.getAsJsonPrimitive().getAsString().replaceAll("\\+0000", "Z")))
                .registerTypeAdapter(OpenClosedType.class,
                        (JsonDeserializer<OpenClosedType>) (json, type,
                                jsonDeserializationContext) -> json.getAsBoolean() ? OpenClosedType.OPEN
                                        : OpenClosedType.CLOSED)
                .registerTypeAdapter(OnOffType.class,
                        (JsonDeserializer<OnOffType>) (json, type,
                                jsonDeserializationContext) -> json.getAsBoolean() ? OnOffType.ON : OnOffType.OFF)
                .create();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing VolvoOnCall API bridge handler.");
        VolvoOnCallBridgeConfiguration configuration = getConfigAs(VolvoOnCallBridgeConfiguration.class);

        httpHeader.setProperty("Authorization", configuration.getAuthorization());
        try {
            customerAccount = getURL(SERVICE_URL + "customeraccounts/", CustomerAccounts.class);
            if (customerAccount.username != null) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Incorrect username or password");
            }
        } catch (IOException | JsonSyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("VolvoOnCall Bridge is read-only and does not handle commands");
    }

    public String[] getVehiclesRelationsURL() {
        if (customerAccount != null) {
            return customerAccount.accountVehicleRelationsURL;
        } else {
            return new String[0];
        }
    }

    public <T> T getURL(Class<T> objectClass, String vin) throws IOException, JsonSyntaxException {
        String url = SERVICE_URL + "vehicles/" + vin + "/" + objectClass.getSimpleName().toLowerCase();
        return getURL(url, objectClass);
    }

    public <T> T getURL(String url, Class<T> objectClass) throws IOException, JsonSyntaxException {
        String jsonResponse = HttpUtil.executeUrl("GET", url, httpHeader, null, JSON_CONTENT_TYPE, REQUEST_TIMEOUT);
        return gson.fromJson(jsonResponse, objectClass);
    }

    public class ActionResultControler implements Runnable {
        PostResponse postResponse;

        public ActionResultControler(PostResponse postResponse) {
            this.postResponse = postResponse;
        }

        @Override
        public void run() {
            switch (postResponse.status) {
                case SUCCESSFULL:
                case FAILED:
                    logger.debug("Action status : {} for vehicle : {}.", postResponse.status.toString(),
                            postResponse.vehicleId);
                    getThing().getThings().stream().filter(VehicleHandler.class::isInstance)
                            .map(VehicleHandler.class::cast)
                            .forEach(handler -> handler.updateIfMatches(postResponse.vehicleId));
                    break;
                default:
                    try {
                        postResponse = getURL(postResponse.serviceURL, PostResponse.class);
                        scheduler.schedule(new ActionResultControler(postResponse), 1000, TimeUnit.MILLISECONDS);
                    } catch (JsonSyntaxException | IOException e) {
                        logger.warn("Error calling : {} : {}", postResponse.serviceURL, e.getMessage());
                    }
            }
        }
    }

    public void postURL(String URL, @Nullable String body) throws IOException, JsonSyntaxException {
        InputStream inputStream = body != null ? new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)) : null;
        String jsonString = HttpUtil.executeUrl("POST", URL, httpHeader, inputStream, null, REQUEST_TIMEOUT);
        PostResponse postResponse = gson.fromJson(jsonString, PostResponse.class);
        if (postResponse.errorLabel == null) {
            pendingActions
                    .add(scheduler.schedule(new ActionResultControler(postResponse), 1000, TimeUnit.MILLISECONDS));
        } else {
            logger.warn(postResponse.errorDescription);
        }
        pendingActions.removeIf(ScheduledFuture<?>::isDone);
    }

    @Override
    public void dispose() {
        super.dispose();
        pendingActions.stream().filter(f -> !f.isCancelled()).forEach(f -> f.cancel(true));
    }
}
