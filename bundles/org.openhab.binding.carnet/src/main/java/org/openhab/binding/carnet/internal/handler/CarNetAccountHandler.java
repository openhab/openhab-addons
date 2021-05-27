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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
import org.openhab.binding.carnet.internal.api.brand.CarNetBrandApiID;
import org.openhab.binding.carnet.internal.api.brand.CarNetBrandApiNull;
import org.openhab.binding.carnet.internal.api.brand.CarNetBrandApiSkoda;
import org.openhab.binding.carnet.internal.api.brand.CarNetBrandApiVW;
import org.openhab.binding.carnet.internal.api.brand.CarNetBrandSeat;
import org.openhab.binding.carnet.internal.config.CarNetAccountConfiguration;
import org.openhab.binding.carnet.internal.config.CarNetCombinedConfig;
import org.openhab.core.io.net.http.HttpClientFactory;
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

    public final String thingId;
    private final CarNetCombinedConfig config = new CarNetCombinedConfig();
    private final CarNetTextResources messages;
    private final CarNetTokenManager tokenManager;

    private CarNetApiBase api = new CarNetBrandApiNull();
    private List<CarNetVehicleInformation> vehicleList = new CopyOnWriteArrayList<>();
    private List<CarNetDeviceListener> vehicleInformationListeners = new CopyOnWriteArrayList<>();
    // Collections.synchronizedList(new ArrayList<CarNetDeviceListener>());
    private @Nullable ScheduledFuture<?> refreshJob;

    private static SslContextFactory sslStrongCipher = new SslContextFactory();
    private static SslContextFactory sslWeakCipher = new SslContextFactory();
    static {
        String[] excludedCiphersWithoutTlsRsaExclusion = Arrays.stream(sslWeakCipher.getExcludeCipherSuites())
                .filter(cipher -> !"^TLS_RSA_.*$".equals(cipher)).toArray(String[]::new);
        sslWeakCipher.setExcludeCipherSuites(excludedCiphersWithoutTlsRsaExclusion);
    }

    private static Map<String, String> brandMap = new HashMap<>();
    static {
        brandMap.put(THING_MYAUDI, CNAPI_BRAND_AUDI);
        brandMap.put(THING_VOLKSWAGEN, CNAPI_BRAND_VW);
        brandMap.put(THING_VWID, CNAPI_BRAND_VWID);
        brandMap.put(THING_SKODA, CNAPI_BRAND_SKODA);
        brandMap.put(THING_SEAT, CNAPI_BRAND_SEAT);
    }

    /**
     * Constructor
     *
     * @param bridge Bridge object representing a FRITZ!Box
     */
    public CarNetAccountHandler(Bridge bridge, CarNetTextResources messages, CarNetTokenManager tokenManager,
            HttpClientFactory httpFactory) {
        super(bridge);
        this.messages = messages;
        this.tokenManager = tokenManager;
        this.thingId = getThing().getUID().getId();

        // Generate a unique Id for all tokens of the new Account thing, but also of all depending Vehicle things. This
        // allows sharing the tokens across all things associated with the account.
        config.account = getConfigAs(CarNetAccountConfiguration.class);
        String ttype = getThing().getUID().toString();
        ttype = CarNetUtils.substringBetween(ttype, ":", ":");
        String brand = brandMap.get(ttype);
        if (brand != null) {
            config.account.brand = brand;
        }
        config.tokenSetId = tokenManager.generateTokenSetId();
        config.api = api.getProperties();
        api.setConfig(config);
        api = createApi(config, httpFactory);
        config.authenticator = api;
        tokenManager.setup(config.tokenSetId, api.getHttp());
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
            api.initialize(config);
            refreshProperties(properties);
        }

        CarNetVehicleList vehices = api.getVehicles();
        vehicleList = new ArrayList<CarNetVehicleInformation>();
        for (String vin : vehices.userVehicles.vehicle) {
            config.vehicle.vin = vin;
            api.setConfig(config);
            config.vehicle.apiUrlPrefix = api.getApiUrl();
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
     * This routine is called every time the Thing configuration has been changed
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        try {
            stateChanged(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Thing config updated, re-initialize");
            config.account = getConfigAs(CarNetAccountConfiguration.class);
            api.setConfig(config);
            initializeThing();
        } catch (CarNetException e) {
            logger.warn("{}: {}", messages.get("init-fialed", "Re-initialization failed!"), thingId, e);
        }
    }

    /**
     * Empty handleCommand for Account Thing
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            return;
        }
        String channelId = channelUID.getIdWithoutGroup();
        logger.debug("{}: Undefined command '{}' for channel {}", thingId, command, channelId);
    }

    public CarNetApiBase createApi(CarNetCombinedConfig config, HttpClientFactory httpFactory) {
        CarNetHttpClient httpClient = createHttpClient(httpFactory);
        switch (config.account.brand) {
            case CNAPI_BRAND_AUDI:
                return new CarNetBrandApiAudi(httpClient, tokenManager);
            case CNAPI_BRAND_VW:
                return new CarNetBrandApiVW(httpClient, tokenManager);
            case CNAPI_BRAND_VWID:
                return new CarNetBrandApiID(httpClient, tokenManager);
            case CNAPI_BRAND_VWGO:
                return new CarNetBrandApiVW(httpClient, tokenManager);
            case CNAPI_BRAND_SKODA:
                return new CarNetBrandApiSkoda(httpClient, tokenManager);
            case CNAPI_BRAND_SEAT:
                return new CarNetBrandSeat(httpClient, tokenManager);
            case CNAPI_BRAND_NULL:
            default:
                return api;

        }
    }

    private CarNetHttpClient createHttpClient(HttpClientFactory httpFactory) {
        // Each instance has it's own http client. Audi requires weaked SSL attributes, other may not
        HttpClient httpClient = new HttpClient(); // httpFactory.getCommonHttpClient();
        try {
            httpClient = new HttpClient(config.api.weakSsl ? sslWeakCipher : sslStrongCipher);
            httpClient.start();
            if (logger.isTraceEnabled()) {
                logger.trace("{}: HttpClient setup: {}", thingId, httpClient.dump());
            }
        } catch (Exception e) {
            logger.warn("{}: {}", messages.get("init-fialed", "Unable to start HttpClient!"), thingId, e);
        }
        CarNetHttpClient client = new CarNetHttpClient(httpClient);
        client.setConfig(config);
        return client;
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
                logger.debug("{}: Re-initialize with account {}", thingId, config.account.user);
                initializeThing();
            } else {
                api.refreshTokens();
            }
        } catch (CarNetException e) {
            logger.debug("Unable to refresh tokens", e);
        }
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
        logger.debug("{}: Handler disposed.", thingId);
        cancelRefreshJob();
    }
}
