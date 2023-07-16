/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;
import static org.openhab.binding.connectedcar.internal.util.Helpers.getString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory.Client;
import org.openhab.binding.connectedcar.internal.api.ApiBase;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.connectedcar.internal.api.ApiErrorDTO;
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
import org.openhab.binding.connectedcar.internal.util.Helpers;
import org.openhab.binding.connectedcar.internal.util.TextResources;
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
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler implements ThingHandlerInterface {
    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);

    public final String thingId;
    private final CombinedConfig config = new CombinedConfig();
    private final TextResources messages;
    private final IdentityManager tokenManager;

    public IdentityManager getTokenManager() {
        return tokenManager;
    }

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

    private static Map<String, String> BRAND_MAP = new HashMap<>();
    static {
        BRAND_MAP.put(THING_MYAUDI, API_BRAND_AUDI);
        BRAND_MAP.put(THING_VOLKSWAGEN, API_BRAND_VW);
        BRAND_MAP.put(THING_VWID, API_BRAND_VWID);
        BRAND_MAP.put(THING_SEAT, API_BRAND_SEAT);
        BRAND_MAP.put(THING_SKODA, API_BRAND_SKODA);
        BRAND_MAP.put(THING_SKODA_E, API_BRAND_SKODA_E);
        BRAND_MAP.put(THING_FORD, API_BRAND_FORD);
        BRAND_MAP.put(THING_WECHARGE, API_BRAND_WECHARGE);
    }

    /**
     * Constructor
     *
     * @param bridge Bridge object representing a FRITZ!Box
     */
    public AccountHandler(Bridge bridge, TextResources messages, IdentityManager tokenManager) {
        super(bridge);

        try {
            this.messages = messages;
            this.tokenManager = tokenManager;
            this.thingId = getThing().getUID().getId();

            // Generate a unique Id for all tokens of the new Account thing, but also of all depending Vehicle things.
            // This
            // allows sharing the tokens across all things associated with the account.
            config.account = getConfigAs(AccountConfiguration.class);
            String ttype = getThing().getUID().toString();
            ttype = Helpers.substringBetween(ttype, ":", ":");
            String brand = BRAND_MAP.get(ttype);
            if (brand == null) {
                throw new IllegalArgumentException("Unable to get brand for thing type " + ttype);
            }
            config.account.brand = brand;
            api = createApi(config, null);
            config.authenticator = api;
            config.api = api.getProperties();
            createTokenSet(config);
        } catch (ApiException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void createTokenSet(CombinedConfig config) {
        config.tokenSetId = tokenManager.generateTokenSetId();
        logger.trace("{}: New TokenSetId {} for {}", thingId, config.tokenSetId, config.api.clientId);
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
                initializeThing("initialize");
            } catch (ApiException e) {
                String message = "";
                String detail = "";

                ThingStatusDetail subStatus = ThingStatusDetail.COMMUNICATION_ERROR;
                ApiErrorDTO err = e.getApiResult().getApiError();
                if (!err.description.isEmpty()) {
                    message = err.toString();
                }
                if (message.isEmpty()) {
                    message = getString(e.toString());
                }
                if (e.isConfigurationException()) {
                    subStatus = ThingStatusDetail.CONFIGURATION_PENDING;
                    detail = messages.get("config-pending", message);
                } else if (e.isSecurityException()) {
                    detail = messages.get("login-failed", message);
                } else {
                    detail = messages.get("init-failed", message);
                }
                stateChanged(ThingStatus.OFFLINE, subStatus, detail);
                logger.debug("{}: {}", config.getLogId(), detail);
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
    public boolean initializeThing(String caller) throws ApiException {
        logger.debug("{}: Initialize Thing (caller: {}()", thingId, caller);
        if (!api.isInitialized()) {
            api.initialize(config);
        } else {
            api.setConfig(config);
        }

        vehicleList = new ArrayList<VehicleDetails>();
        for (String vin : api.getVehicles()) {
            CombinedConfig vconfig = new CombinedConfig(config);
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
            stateChanged(ThingStatus.UNKNOWN, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING,
                    "Thing config updated, re-initialize");
            config.account = getConfigAs(AccountConfiguration.class);
            api.setConfig(config);
            initializeThing("handleConfigurationUpdate");
        } catch (Exception e) {
            logger.warn("{}: {}", messages.get("init-failed", e.toString()), thingId);
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
        api.setConfig(config);
        switch (config.account.brand) {
            case API_BRAND_AUDI:
                return new BrandCarNetAudi(this, httpClient, tokenManager, apiListener);
            case API_BRAND_VW:
                return new BrandCarNetVW(this, httpClient, tokenManager, apiListener);
            case API_BRAND_VWID:
                return new BrandWeConnect(this, httpClient, tokenManager, apiListener);
            case API_BRAND_WECHARGE:
                return new BrandWeCharge(this, httpClient, tokenManager, apiListener);
            case API_BRAND_SKODA:
                return new BrandCarNetSkoda(this, httpClient, tokenManager, apiListener);
            case API_BRAND_SEAT:
                return new BrandCarNetSeat(this, httpClient, tokenManager, apiListener);
            case API_BRAND_SKODA_E:
                return new BrandSkodaE(this, httpClient, tokenManager, apiListener);
            case API_BRAND_FORD:
                return new BrandFordPass(this, httpClient, tokenManager, apiListener);
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
            logger.warn("{}: {}", messages.get("init-failed", "Unable to start HttpClient!"), thingId, e);
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
                logger.debug("{}: Reconnect with account {}", thingId, config.account.user);
                initializeThing("refreshStatus");
            } else if (status == ThingStatus.ONLINE) {
                api.refreshTokens();
            }
        } catch (ApiException e) {
            logger.debug("Unable to refresh tokens", e);
        }
    }

    @Override
    public String getProperty(String key) {
        Map<String, String> properties = getThing().getProperties();
        return getString(properties.get(key));
    }

    @Override
    public void fillProperty(String key, String value) {
        updateProperties(Collections.singletonMap(key, value));
    }

    @Override
    public CombinedConfig getCombinedConfig() {
        return config;
    }

    public boolean equalsThingUID(String thingUID) {
        return getThing().getUID().getAsString().equals(thingUID);
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
