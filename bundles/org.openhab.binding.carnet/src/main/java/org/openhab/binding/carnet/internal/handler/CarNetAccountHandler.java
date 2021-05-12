/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.handler;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;
import static org.openhab.binding.carnet.internal.CarNetUtils.getString;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.CarNetTextResources;
import org.openhab.binding.carnet.internal.CarNetUtils;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNVehicleDetails.CarNetVehicleDetails;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetVehicleList;
import org.openhab.binding.carnet.internal.api.CarNetHttpClient;
import org.openhab.binding.carnet.internal.api.CarNetTokenManager;
import org.openhab.binding.carnet.internal.api.brand.CarNetBrandApiAudi;
import org.openhab.binding.carnet.internal.api.brand.CarNetBrandApiSkoda;
import org.openhab.binding.carnet.internal.api.brand.CarNetBrandApiVW;
import org.openhab.binding.carnet.internal.api.brand.CarNetBrandSeat;
import org.openhab.binding.carnet.internal.config.CarNetAccountConfiguration;
import org.openhab.binding.carnet.internal.config.CarNetCombinedConfig;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetAccountHandler} implements access to the myAudi account API. It is implemented as a brdige device
 * and also dispatches events to the vehicle things.
 *
 * @author Markus Michels - Initial contribution
 * @author Lorenzo Bernardi - Additional contribution
 *
 */
@NonNullByDefault
public class CarNetAccountHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(CarNetAccountHandler.class);
    private final CarNetCombinedConfig config = new CarNetCombinedConfig();
    private final CarNetTextResources messages;
    private final CarNetTokenManager tokenManager;
    private final CarNetApiBase api;
    private final CarNetHttpClient http;

    private List<CarNetVehicleInformation> vehicleList = new ArrayList<>();
    private List<CarNetDeviceListener> vehicleInformationListeners = Collections
            .synchronizedList(new ArrayList<CarNetDeviceListener>());
    private @Nullable ScheduledFuture<?> refreshJob;

    /**
     * Constructor
     *
     * @param bridge Bridge object representing a FRITZ!Box
     */
    public CarNetAccountHandler(Bridge bridge, CarNetTextResources messages, CarNetTokenManager tokenManager) {
        super(bridge);
        this.messages = messages;
        this.tokenManager = tokenManager;

        // Each instance has it's own http client. Audi requires weaked SSL attributes, other may not
        HttpClient httpClient = new HttpClient();
        try {
            SslContextFactory ssl = new SslContextFactory();
            String[] excludedCiphersWithoutTlsRsaExclusion = Arrays.stream(ssl.getExcludeCipherSuites())
                    .filter(cipher -> !"^TLS_RSA_.*$".equals(cipher)).toArray(String[]::new);
            ssl.setExcludeCipherSuites(excludedCiphersWithoutTlsRsaExclusion);
            httpClient = new HttpClient(ssl);
            httpClient.start();
            if (logger.isDebugEnabled()) {
                logger.debug("{}", httpClient.dump());
            }
        } catch (Exception e) {
            logger.warn("{}", messages.get("init-fialed", "Unable to start HttpClient!"), e);
        }
        http = new CarNetHttpClient(httpClient);

        // Generate a unique Id for all tokens of the new Account thing, but also of all depending Vehicle things. This
        // allows sharing the tokens across all things associated with the account.
        config.account = getConfigAs(CarNetAccountConfiguration.class);
        config.tokenSetId = tokenManager.generateTokenSetId();
        api = createApi(config);
        tokenManager.setup(http, api);
    }

    public CarNetApiBase createApi(CarNetCombinedConfig config) {
        String type = getThing().getUID().getAsString();
        type = CarNetUtils.substringBetween(type, ":", ":");
        switch (type) {
            case THING_MYAUDI:
            default:
                config.account.brand = CNAPI_BRAND_AUDI;
                return new CarNetBrandApiAudi(http, tokenManager);
            case THING_VOLKSWAGEN:
                config.account.brand = CNAPI_BRAND_VW;
                return new CarNetBrandApiVW(http, tokenManager);
            case THING_VWID:
                config.account.brand = CNAPI_BRAND_VWID;
                return new CarNetBrandApiVW(http, tokenManager);
            case THING_VWGO:
                config.account.brand = CNAPI_BRAND_VWGO;
                return new CarNetBrandApiVW(http, tokenManager);
            case THING_SKODA:
                config.account.brand = CNAPI_BRAND_SKODA;
                return new CarNetBrandApiSkoda(http, tokenManager);
            case THING_SEAT:
                config.account.brand = CNAPI_BRAND_SEAT;
                return new CarNetBrandSeat(http, tokenManager);
        }
    }

    /**
     * Initializes the bridge.
     */
    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            try {
                initializeThing();
            } catch (CarNetException e) {
                String detail = e.toString();
                if (e.isSecurityException()) {
                    detail = messages.get("login-failed", getString(e.getMessage()));
                }
                stateChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, detail);
            }
        });
    }

    /**
     * Retries the vehicle list from the the account and post this information to the listeners. New things are created
     * per vehicle under this account.
     * A background job is scheduled to check token status and trigger a refresh before they expire.
     *
     * @return
     * @throws CarNetException
     */
    public boolean initializeThing() throws CarNetException {
        Map<String, String> properties = new TreeMap<String, String>();

        if (!api.isInitialized()) {
            api.setConfig(config);
            api.initialize();
            refreshProperties(properties);
        }

        CarNetVehicleList vehices = api.getVehicles();
        vehicleList = new ArrayList<CarNetVehicleInformation>();
        for (String vin : vehices.userVehicles.vehicle) {
            config.vehicle.vin = vin;
            api.setConfig(config);
            config.apiUrlPrefix = api.getApiUrl();
            api.setConfig(config);
            CarNetVehicleDetails details = api.getVehicleDetails(vin);
            vehicleList.add(new CarNetVehicleInformation(details));
        }
        informVehicleInformationListeners(vehicleList);

        setupRefreshJob(5);
        stateChanged(ThingStatus.ONLINE, ThingStatusDetail.NONE, "");
        return true;
    }

    /**
     * Called by vehicle handler to register callback
     *
     * @param listener Listener interface provided by Vehicle Handler
     */
    public void registerListener(CarNetDeviceListener listener) {
        vehicleInformationListeners.add(listener);
    }

    /**
     * Called by vehicle handler to unregister callback
     *
     * @param listener Listener interface provided by Vehicle Handler
     */
    public void unregisterListener(CarNetDeviceListener listener) {
        vehicleInformationListeners.remove(listener);
    }

    /**
     * Forward discovery information to all listeners (Vehicle Handlers)
     *
     * @param vehicleInformationList
     */
    private void informVehicleInformationListeners(@Nullable List<CarNetVehicleInformation> vehicleInformationList) {
        this.vehicleInformationListeners.forEach(discovery -> discovery.informationUpdate(vehicleInformationList));
    }

    /**
     * Notify all listeners about status changes
     *
     * @param status New status
     * @param detail Status details
     * @param message Message
     */
    void stateChanged(ThingStatus status, ThingStatusDetail detail, String message) {
        updateStatus(status, detail, message);
        this.vehicleInformationListeners.forEach(discovery -> discovery.stateChanged(status, detail, message));
    }

    /**
     * Empty handleCommand for Account Thing
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        logger.debug("Handle command '{}' for channel {}", command, channelId);
        if (command == RefreshType.REFRESH) {
            return;
        }
    }

    /**
     * Sets up a polling job (using the scheduler) with the given interval.
     *
     * @param initialWaitTime The delay before the first refresh. Maybe 0 to immediately
     *            initiate a refresh.
     */
    private void setupRefreshJob(int initialWaitTime) {
        cancelRefreshJob();
        refreshJob = scheduler.scheduleWithFixedDelay(this::refreshStatus, initialWaitTime,
                API_TOKEN_REFRESH_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    /**
     * Cancels the polling job (if one was setup).
     */
    private void cancelRefreshJob() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(false);
        }
    }

    private void refreshStatus() {
        try {
            if (getThing().getStatus() == ThingStatus.OFFLINE) {
                logger.debug("CarNet: Re-initialize with account {}", config.account.user);
                initializeThing();
            } else {
                api.refreshTokens();
            }
        } catch (CarNetException e) {
            logger.debug("Unable to refresh tokens", e);
        }
    }

    public CarNetHttpClient getHttpClient() {
        // Account and Vehicle Handlers are sharing the same httpClient
        return http;
    }

    public CarNetCombinedConfig getCombinedConfig() {
        return config;
    }

    /**
     * Add one property to the Thing Properties
     *
     * @param key Name of the property
     * @param value Value of the property
     */
    public void updateProperties(String key, String value) {
        Map<String, String> property = new TreeMap<String, String>();
        property.put(key, value);
        updateProperties(property);
    }

    public void refreshProperties(Map<String, String> newProperties) {
        Map<String, String> thingProperties = editProperties();
        for (Map.Entry<String, String> prop : newProperties.entrySet()) {
            if (thingProperties.containsKey(prop.getKey())) {
                thingProperties.replace(prop.getKey(), prop.getValue());
            } else {
                thingProperties.put(prop.getKey(), prop.getValue());
            }
        }
        updateProperties(thingProperties);
    }

    /**
     * Disposes the bridge.
     */
    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        cancelRefreshJob();
    }
}
