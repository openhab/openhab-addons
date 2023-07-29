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
package org.openhab.binding.windcentrale.internal.handler;

import static java.util.function.Predicate.not;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.windcentrale.internal.WindcentraleDiscoveryService;
import org.openhab.binding.windcentrale.internal.api.RequestListener;
import org.openhab.binding.windcentrale.internal.api.TokenProvider;
import org.openhab.binding.windcentrale.internal.api.WindcentraleAPI;
import org.openhab.binding.windcentrale.internal.config.AccountConfiguration;
import org.openhab.binding.windcentrale.internal.exception.FailedGettingDataException;
import org.openhab.binding.windcentrale.internal.exception.InvalidAccessTokenException;
import org.openhab.binding.windcentrale.internal.listener.ThingStatusListener;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WindcentraleAccountHandler} provides the {@link WindcentraleAPI} instance used by the windmill handlers.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class WindcentraleAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(WindcentraleAccountHandler.class);

    private final HttpClientFactory httpClientFactory;
    private final List<ThingStatusListener> thingStatusListeners = new CopyOnWriteArrayList<>();

    private @Nullable WindcentraleAPI api;
    private @Nullable Exception apiException;
    private @Nullable Future<?> initializeFuture;

    private final RequestListener requestListener = new RequestListener() {
        @Override
        public void onError(Exception exception) {
            apiException = exception;
            logger.debug("API exception occurred");
            updateThingStatus();
        }

        @Override
        public void onSuccess() {
            if (apiException != null) {
                apiException = null;
                logger.debug("API exception cleared");
                updateThingStatus();
            }
        }
    };

    public WindcentraleAccountHandler(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
    }

    public void addThingStatusListener(ThingStatusListener listener) {
        thingStatusListeners.add(listener);
        listener.thingStatusChanged(thing, thing.getStatus());
    }

    @Override
    public void dispose() {
        Future<?> localFuture = initializeFuture;
        if (localFuture != null) {
            localFuture.cancel(true);
            initializeFuture = null;
        }

        WindcentraleAPI localAPI = api;
        if (localAPI != null) {
            localAPI.dispose();
            api = null;
        }
    }

    public @Nullable WindcentraleAPI getAPI() {
        return api;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        initializeFuture = scheduler.submit(() -> {
            api = initializeAPI();
            updateThingStatus();
        });
    }

    private WindcentraleAPI initializeAPI() {
        AccountConfiguration config = getConfigAs(AccountConfiguration.class);
        TokenProvider tokenProvider = new TokenProvider(httpClientFactory, config.username, config.password);

        WindcentraleAPI api = new WindcentraleAPI(httpClientFactory, tokenProvider);
        api.addRequestListener(requestListener);
        apiException = null;

        try {
            api.getProjects();
            api.getLiveData();
        } catch (FailedGettingDataException | InvalidAccessTokenException e) {
            apiException = e;
        }
        return api;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(WindcentraleDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void removeThingStatusListener(ThingStatusListener listener) {
        thingStatusListeners.remove(listener);
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String comment) {
        ThingStatus oldStatus = thing.getStatus();
        super.updateStatus(status, detail, comment);
        ThingStatus newStatus = thing.getStatus();

        if (!oldStatus.equals(newStatus)) {
            logger.debug("Updating listeners with status {}", status);
            for (ThingStatusListener listener : thingStatusListeners) {
                listener.thingStatusChanged(thing, status);
            }
        }
    }

    private void updateThingStatus() {
        Exception e = apiException;
        if (e != null) {
            if (e instanceof InvalidAccessTokenException) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            } else {
                Throwable cause = e.getCause();
                String description = Stream
                        .of(Objects.requireNonNullElse(e.getMessage(), ""),
                                cause == null ? "" : Objects.requireNonNullElse(cause.getMessage(), ""))
                        .filter(not(String::isBlank)) //
                        .collect(Collectors.joining(": "));
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, description);
            }
        } else {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        }
    }
}
