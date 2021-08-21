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
package org.openhab.binding.connectedcar.internal.handler;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.CarUtils.getString;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;

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
import org.eclipse.jetty.util.ssl.SslContextFactory.Client;
import org.openhab.binding.connectedcar.internal.CarUtils;
import org.openhab.binding.connectedcar.internal.TextResources;
import org.openhab.binding.connectedcar.internal.api.ApiBase;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.BrandNull;
import org.openhab.binding.connectedcar.internal.api.IdentityManager;
import org.openhab.binding.connectedcar.internal.api.carnet.BrandCarNetAudi;
import org.openhab.binding.connectedcar.internal.api.carnet.BrandCarNetSeat;
import org.openhab.binding.connectedcar.internal.api.carnet.BrandCarNetSkoda;
import org.openhab.binding.connectedcar.internal.api.carnet.BrandCarNetVW;
import org.openhab.binding.connectedcar.internal.api.fordpass.BrandFordPass;
import org.openhab.binding.connectedcar.internal.api.skodae.BrandSkodaE;
import org.openhab.binding.connectedcar.internal.api.wecharge.BrandWeCharge;
import org.openhab.binding.connectedcar.internal.api.weconnect.BrandWeConnect;
import org.openhab.binding.connectedcar.internal.config.AccountConfiguration;
import org.openhab.binding.connectedcar.internal.config.CombinedConfig;
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
 * {@link AccountHandler} implements access to the myAudi account API. It is implemented as a brdige device
 * and also dispatches events to the vehicle things.
 *
 * @author Markus Michels - Initial contribution
 * @author Lorenzo Bernardi - Additional contribution
 *
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);

    public final String thingId;
    private final CombinedConfig config = new CombinedConfig();
    private final TextResources messages;
    private final IdentityManager tokenManager;

    private ApiBase api = new BrandNull();
    private List<VehicleDetails> vehicleList = new CopyOnWriteArrayList<>();
    private List<AccountListener> vehicleInformationListeners = new CopyOnWriteArrayList<>();
    private @Nullable ScheduledFuture<?> refreshJob;

    private static Client sslStrongCipher = new Client();
    private static Client sslWeakCipher = new Client();
    static {
        String[] excludedCiphersWithoutTlsRsaExclusion = Arrays.stream(sslWeakCipher.getExcludeCipherSuites())
                .filter(cipher -> !"^TLS_RSA_.*$".equals(cipher)).toArray(String[]::new);
        sslWeakCipher.setExcludeCipherSuites(excludedCiphersWithoutTlsRsaExclusion);
    }

    private static Map<String, String> brandMap = new HashMap<>();
    static {
        brandMap.put(THING_MYAUDI, API_BRAND_AUDI);
        brandMap.put(THING_VOLKSWAGEN, API_BRAND_VW);
        brandMap.put(THING_VWID, API_BRAND_VWID);
        brandMap.put(THING_VWGO, API_BRAND_VWGO);
        brandMap.put(THING_SEAT, API_BRAND_SEAT);
        brandMap.put(THING_SKODA, API_BRAND_SKODA);
        brandMap.put(THING_SKODA_E, API_BRAND_SKODA_E);
        brandMap.put(THING_FORD, API_BRAND_FORD);
        brandMap.put(THING_WECHARGE, API_BRAND_WECHARGE);
    }

    /**
     * Constructor
     *
     * @param bridge Bridge object representing a FRITZ!Box
     */
    public AccountHandler(Bridge bridge, TextResources messages, IdentityManager tokenManager) {
        super(bridge);
        this.messages = messages;
        this.tokenManager = tokenManager;
        this.thingId = getThing().getUID().getId();

        // Generate a unique Id for all tokens of the new Account thing, but also of all depending Vehicle things. This
        // allows sharing the tokens across all things associated with the account.
        config.account = getConfigAs(AccountConfiguration.class);
        String ttype = getThing().getUID().toString();
        ttype = CarUtils.substringBetween(ttype, ":", ":");
        String brand = brandMap.get(ttype);
        if (brand == null) {
            throw new IllegalArgumentException("Unable to get brand for thing type " + ttype);
        }
        config.account.brand = brand;
        api = createApi(config, null);
        config.authenticator = api;
        config.api = api.getProperties();
        createTokenSet(config);
    }

    public void createTokenSet(CombinedConfig config) {
        config.tokenSetId = tokenManager.generateTokenSetId();
        tokenManager.setup(config.tokenSetId, api.getHttp());
        api.setConfig(config);
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
            } catch (ApiException e) {
                String detail = e.isSecurityException() ? messages.get("login-failed", getString(e.getMessage()))
                        : getString(e.getMessage());
                stateChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, detail);
                logger.debug("{}: Initialization failed: {}", config.getLogId(), detail);
            }
        });
    }

    /**
     * Retries the vehicle list from the the account and post this information to the listeners. New things are created
     * per vehicle under this account.
     * A background job is scheduled to check token status and trigger a refresh before they expire.
     *
     * @return
     * @throws ApiException
     */
    public boolean initializeThing() throws ApiException {
        Map<String, String> properties = new TreeMap<String, String>();

        if (!api.isInitialized()) {
            api.initialize(config);
            refreshProperties(properties);
        }

        vehicleList = new ArrayList<VehicleDetails>();
        for (String vin : api.getVehicles()) {
            CombinedConfig vconfig = config;
            vconfig.vehicle.vin = vin;
            api.setConfig(vconfig);
            vconfig.vstatus.apiUrlPrefix = api.getApiUrl();
            api.setConfig(vconfig);
            vehicleList.add(api.getVehicleDetails(vin));
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
            config.account = getConfigAs(AccountConfiguration.class);
            api.setConfig(config);
            initializeThing();
        } catch (ApiException e) {
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

    public ApiBase createApi(CombinedConfig config, @Nullable ApiEventListener apiListener) {
        ApiHttpClient httpClient = createHttpClient(apiListener);
        switch (config.account.brand) {
            case API_BRAND_AUDI:
                return new BrandCarNetAudi(httpClient, tokenManager, apiListener);
            case API_BRAND_VW:
                return new BrandCarNetVW(httpClient, tokenManager, apiListener);
            case API_BRAND_VWID:
                return new BrandWeConnect(httpClient, tokenManager, apiListener);
            case API_BRAND_WECHARGE:
                return new BrandWeCharge(httpClient, tokenManager, apiListener);
            case API_BRAND_SKODA:
                return new BrandCarNetSkoda(httpClient, tokenManager, apiListener);
            case API_BRAND_SEAT:
                return new BrandCarNetSeat(httpClient, tokenManager, apiListener);
            case API_BRAND_SKODA_E:
                return new BrandSkodaE(httpClient, tokenManager, apiListener);
            case API_BRAND_FORD:
                return new BrandFordPass(httpClient, tokenManager, apiListener);
            default:
                logger.warn("Unknown brand {}", config.account.brand);
            case API_BRAND_NULL:
                return api;

        }
    }

    private ApiHttpClient createHttpClient(@Nullable ApiEventListener apiListener) {
        // Each instance has it's own http client. Audi requires weaked SSL attributes, other may not
        HttpClient httpClient = new HttpClient();
        try {
            httpClient = new HttpClient(config.api.weakSsl ? sslWeakCipher : sslStrongCipher);
            httpClient.start();
            if (config.api.weakSsl && logger.isTraceEnabled()) {
                logger.trace("{}: WeakSSL enabled, HttpClient setup: {}", thingId, httpClient.dump());
            }
        } catch (Exception e) {
            logger.warn("{}: {}", messages.get("init-fialed", "Unable to start HttpClient!"), thingId, e);
        }
        ApiHttpClient client = new ApiHttpClient(httpClient, apiListener);
        client.setConfig(config);
        return client;
    }

    /**
     * Called by vehicle handler to register callback
     *
     * @param listener Listener interface provided by Vehicle Handler
     */
    public void registerListener(AccountListener listener) {
        vehicleInformationListeners.add(listener);
    }

    /**
     * Called by vehicle handler to unregister callback
     *
     * @param listener Listener interface provided by Vehicle Handler
     */
    public void unregisterListener(AccountListener listener) {
        vehicleInformationListeners.remove(listener);
    }

    /**
     * Forward discovery information to all listeners (Vehicle Handlers)
     *
     * @param vehicleInformationList
     */
    private void informVehicleInformationListeners(@Nullable List<VehicleDetails> vehicleInformationList) {
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
            ThingStatus status = getThing().getStatus();
            if (status == ThingStatus.OFFLINE) {
                logger.debug("{}: Re-initialize with account {}", thingId, config.account.user);
                initializeThing();
            } else if (status == ThingStatus.ONLINE) {
                api.refreshTokens();
            }
        } catch (ApiException e) {
            logger.debug("Unable to refresh tokens", e);
        }
    }

    public CombinedConfig getCombinedConfig() {
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
