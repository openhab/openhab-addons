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
package org.openhab.binding.icloud.internal.handler;

import static java.util.concurrent.TimeUnit.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.icloud.internal.ICloudApiResponseException;
import org.openhab.binding.icloud.internal.ICloudDeviceInformationListener;
import org.openhab.binding.icloud.internal.ICloudService;
import org.openhab.binding.icloud.internal.configuration.ICloudAccountThingConfiguration;
import org.openhab.binding.icloud.internal.handler.dto.json.response.ICloudAccountDataResponse;
import org.openhab.binding.icloud.internal.handler.dto.json.response.ICloudDeviceInformation;
import org.openhab.binding.icloud.internal.utilities.JsonUtils;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.storage.Storage;
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
 * Retrieves the data for a given account from iCloud and passes the information to
 * {@link org.openhab.binding.icloud.internal.discovery.ICloudDeviceDiscovery} and to the {@link ICloudDeviceHandler}s.
 *
 * @author Patrik Gfeller - Initial contribution
 * @author Hans-Jörg Merk - Extended support with initial Contribution
 * @author Simon Spielmann - Rework for new iCloud API
 * @author Simon Spielmann - Recover status after bridge and communication failures
 */
@NonNullByDefault
public class ICloudAccountBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(ICloudAccountBridgeHandler.class);

    private static final int CACHE_EXPIRY = (int) SECONDS.toMillis(10);

    private @Nullable ICloudService iCloudService;

    private @Nullable ExpiringCache<String> iCloudDeviceInformationCache;

    private volatile AuthState authState = AuthState.INITIAL;

    private final Object synchronizeRefresh = new Object();

    private final Object lifecycleLock = new Object();

    /**
     * Incremented on every {@link #dispose()}, so that an init task of a previous lifecycle, which may still be
     * running (e.g. when the 2-FA code handling re-initializes the handler), cannot schedule a refresh job.
     */
    private int lifecycleGeneration;

    private Set<ICloudDeviceInformationListener> deviceInformationListeners = Collections
            .synchronizedSet(new HashSet<>());

    @Nullable
    ScheduledFuture<?> refreshJob;

    @Nullable
    ScheduledFuture<?> initTask;

    private Storage<String> storage;

    private static final String AUTH_CODE_KEY = "AUTH_CODE";

    /**
     * The constructor.
     *
     * @param bridge The bridge to set
     * @param storage The storage service to set.
     */
    public ICloudAccountBridgeHandler(Bridge bridge, Storage<String> storage) {
        super(bridge);
        this.storage = storage;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);

        if (command instanceof RefreshType) {
            refreshData();
        }
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        logger.debug("iCloud bridge handler initializing ...");

        if (authState != AuthState.WAIT_FOR_CODE) {
            authState = AuthState.INITIAL;
        }

        this.iCloudDeviceInformationCache = new ExpiringCache<>(CACHE_EXPIRY,
                () -> callApiWithRetryAndExceptionHandling(() ->
                // callApiWithRetryAndExceptionHanlding ensures that iCloudService is not null when the following is
                // called. Cannot use method local iCloudService instance here, because instance may be replaced with a
                // new
                // one during retry.
                iCloudService.getDevices().refreshClient()));

        updateStatus(ThingStatus.UNKNOWN);

        synchronized (lifecycleLock) {
            final int generation = lifecycleGeneration;
            // Init has to be done async becaue it requires sync network calls, which are not allowed in init.
            Callable<?> asyncInit = () -> {
                callApiWithRetryAndExceptionHandling(() -> {
                    logger.debug("Dummy call for initial authentication.");
                    return null;
                });
                scheduleRefresh(generation);
                return null;
            };
            initTask = this.scheduler.schedule(asyncInit, 0, TimeUnit.SECONDS);
        }
        logger.debug("iCloud bridge handler initialized.");
    }

    /**
     * Schedules the periodic refresh. It is also scheduled if the initial login failed, so that transient
     * failures are retried with the next refresh instead of leaving the bridge offline until it is re-initialized.
     */
    private void scheduleRefresh(int generation) {
        ICloudAccountThingConfiguration config = getConfigAs(ICloudAccountThingConfiguration.class);
        long initialDelay = authState == AuthState.AUTHENTICATED ? 0 : config.refreshTimeInMinutes;
        synchronized (lifecycleLock) {
            if (generation != lifecycleGeneration) {
                return;
            }
            cancelRefresh();
            this.refreshJob = this.scheduler.scheduleWithFixedDelay(this::refreshData, initialDelay,
                    config.refreshTimeInMinutes, MINUTES);
        }
    }

    // Package-private to allow replacing the service in unit tests.
    ICloudService createICloudService(String appleId, String password) {
        return new ICloudService(appleId, password, storage);
    }

    private <@Nullable T> T callApiWithRetryAndExceptionHandling(Callable<T> wrapped) {
        int retryCount = 1;
        boolean success = false;
        synchronized (synchronizeRefresh) {
            if (this.iCloudService == null) {
                ICloudAccountThingConfiguration config = getConfigAs(ICloudAccountThingConfiguration.class);
                final String localAppleId = config.appleId;
                final String localPassword = config.password;

                if (localAppleId != null && localPassword != null) {
                    this.iCloudService = createICloudService(localAppleId, localPassword);
                } else {
                    updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Apple ID or password is not set!");
                    return null;
                }
            }

            if (authState == AuthState.INITIAL) {
                success = checkLogin();
            } else if (authState == AuthState.WAIT_FOR_CODE) {
                try {
                    success = handle2FAAuthentication();
                } catch (IOException | InterruptedException | ICloudApiResponseException ex) {
                    logger.debug("Error while validating 2-FA code.", ex);
                    updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Error while validating 2-FA code.");
                    return null;
                }
            }
            if (authState != AuthState.AUTHENTICATED && !success) {
                return null;
            }

            // Intermediate results are not reported, so a single call sets the bridge status only once.
            String failure = "iCloud request failed";
            do {
                if (authState != AuthState.AUTHENTICATED && !checkLogin()) {
                    // checkLogin() has reported the status.
                    return null;
                }
                try {
                    return wrapped.call();
                } catch (ICloudApiResponseException e) {
                    logger.debug("ICloudApiResponseException with status code {}", e.getStatusCode());
                    failure = "iCloud request failed with HTTP status " + e.getStatusCode();
                    if (e.getStatusCode() == 421 || e.getStatusCode() == 450 || e.getStatusCode() == 500) {
                        logger.debug("Authentication error detected (code {}), triggering re-authentication",
                                e.getStatusCode());
                        authState = AuthState.INITIAL;
                    }
                } catch (IllegalStateException e) {
                    // Thrown if the session lacks the web service URLs. Only a new login provides them.
                    logger.debug("iCloud session is not usable, triggering re-authentication.", e);
                    failure = "iCloud session is not usable";
                    authState = AuthState.INITIAL;
                } catch (IOException e) {
                    logger.debug("Unable to refresh device data", e);
                    updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    return null;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                } catch (Exception e) {
                    logger.debug("Unexpected exception occurred", e);
                    updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    return null;
                }

                retryCount++;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            } while (retryCount < 3);

            // Transient server errors such as HTTP 503 end up here. They are reported as communication error, the
            // next refresh retries the request.
            logger.debug("iCloud request failed after retries: {}", failure);
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, failure);
            return null;
        }
    }

    private boolean handle2FAAuthentication() throws IOException, InterruptedException, ICloudApiResponseException {
        logger.debug("Starting iCloud 2-FA authentication  AuthState={}, Thing={})...", authState,
                getThing().getUID().getAsString());
        final ICloudService localICloudService = this.iCloudService;
        if (authState != AuthState.WAIT_FOR_CODE || localICloudService == null) {
            throw new IllegalStateException("2-FA authentication not initialized.");
        }
        ICloudAccountThingConfiguration config = getConfigAs(ICloudAccountThingConfiguration.class);
        String lastTriedCode = storage.get(AUTH_CODE_KEY);
        String code = config.code;
        boolean success = false;
        if (code == null || code.isBlank() || code.equals(lastTriedCode)) {
            // Still waiting for user to update config.
            logger.warn("ICloud authentication requires 2-FA code. Please provide code configuration for thing '{}'.",
                    getThing().getUID().getAsString());
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Please provide 2-FA code in thing configuration.");
            return false;
        } else {
            // 2-FA-Code was requested in previous call of this method.
            // User has provided code in config.
            logger.debug("Code is given in thing configuration '{}'. Trying to validate code...",
                    getThing().getUID().getAsString());
            success = localICloudService.validate2faCode(code);
            // Remember the code only after Apple answered, so a failed validation request can be retried with it.
            storage.put(AUTH_CODE_KEY, code);
            if (!success) {
                authState = AuthState.INITIAL;
                logger.warn("ICloud token invalid.");
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Invalid 2-FA-code.");
                return false;
            }
            org.openhab.core.config.core.Configuration config2 = editConfiguration();
            config2.put("code", "");
            updateConfiguration(config2);

            logger.debug("Code is valid.");
        }
        authState = AuthState.AUTHENTICATED;
        updateStatus(ThingStatus.ONLINE);
        logger.debug("iCloud bridge handler '{}' authenticated with 2-FA code.", getThing().getUID().getAsString());
        return success;
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        synchronized (lifecycleLock) {
            lifecycleGeneration++;
            final ScheduledFuture<?> localInitTask = this.initTask;
            if (localInitTask != null) {
                localInitTask.cancel(true);
                this.initTask = null;
            }
            cancelRefresh();
        }
        super.dispose();
    }

    private void cancelRefresh() {
        final ScheduledFuture<?> localrefreshJob = this.refreshJob;
        if (localrefreshJob != null) {
            localrefreshJob.cancel(true);
            this.refreshJob = null;
        }
    }

    @SuppressWarnings("null")
    public void findMyDevice(String deviceId) throws IOException, InterruptedException {
        callApiWithRetryAndExceptionHandling(() -> {
            // callApiWithRetryAndExceptionHanlding ensures that iCloudService is not null when the following is
            // called. Cannot use method local iCloudService instance here, because instance may be replaced with a new
            // one during retry.
            iCloudService.getDevices().playSound(deviceId);
            return null;
        });
    }

    public void registerListener(ICloudDeviceInformationListener listener) {
        this.deviceInformationListeners.add(listener);
    }

    public void unregisterListener(ICloudDeviceInformationListener listener) {
        this.deviceInformationListeners.remove(listener);
    }

    /**
     * Checks login to iCloud account. The flow is a bit complicated due to 2-FA authentication.
     * The normal flow would be:
     *
     *
     * <pre>
        ICloudService service = new ICloudService(...);
        service.authenticate(false);
        if (service.requires2fa()) {
            String code = ... // Request code from user!
            System.out.println(service.validate2faCode(code));
            if (!service.isTrustedSession()) {
                service.trustSession();
            }
            if (!service.isTrustedSession()) {
                System.err.println("Trust failed!!!");
            }
     * </pre>
     *
     * The call to {@link ICloudService#authenticate(boolean)} request a token from the user.
     * This should be done only once. Afterwards the user has to update the configuration.
     * In openhab this method here is called for several reason (e.g. config change). So we track if we already
     * requested a code {@link #validate2faCode}.
     */
    private boolean checkLogin() {
        logger.debug("Starting iCloud authentication (AuthState={}, Thing={})...", authState,
                getThing().getUID().getAsString());
        final ICloudService localICloudService = this.iCloudService;
        if (authState == AuthState.WAIT_FOR_CODE || localICloudService == null) {
            throw new IllegalStateException("2-FA authentication not completed.");
        }

        try {
            // No code requested yet or session is trusted (hopefully).
            boolean success = localICloudService.authenticate(false);
            if (!success) {
                authState = AuthState.USER_PW_INVALID;
                logger.warn("iCloud authentication failed. Invalid credentials.");
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Invalid credentials.");
                this.iCloudService = null;
                return false;
            }
            if (localICloudService.requires2fa()) {
                // New code was requested. Wait for the user to update config.
                logger.warn(
                        "iCloud authentication requires 2-FA code. Please provide code configuration for thing '{}'.",
                        getThing().getUID().getAsString());
                authState = AuthState.WAIT_FOR_CODE;
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Please provide 2-FA code in thing configuration.");
                return false;
            }

            if (!localICloudService.isTrustedSession()) {
                logger.debug("Trying to establish session trust.");
                success = localICloudService.trustSession();
                if (!success) {
                    updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Session trust failed.");
                    return false;
                }
            }

            // ONLINE is reported by the caller once the actual request succeeded.
            authState = AuthState.AUTHENTICATED;
            logger.debug("iCloud bridge handler authenticated.");
            return true;
        } catch (IOException e) {
            // authState is left unchanged, so the login is retried with the next refresh.
            logger.debug("iCloud authentication failed due to a communication error.", e);
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return false;
        } catch (ICloudApiResponseException e) {
            int statusCode = e.getStatusCode();
            if (statusCode == 502 || statusCode == 503 || statusCode == 504) {
                logger.debug("iCloud authentication failed due to a temporary server error.", e);
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                        "iCloud login failed with HTTP status " + statusCode);
            } else {
                // Like pyicloud, any other HTTP error of the login is treated as a rejected login (e.g. invalid
                // credentials or a locked account). Retrying it automatically could get the account locked.
                authState = AuthState.USER_PW_INVALID;
                logger.warn("iCloud login was rejected with HTTP status {}.", statusCode);
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR,
                        "iCloud login rejected with HTTP status " + statusCode);
                this.iCloudService = null;
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return false;
        }
    }

    /**
     * Refresh iCloud device data.
     *
     * Never throws: this method runs as a periodic scheduled job, which would be cancelled silently by an uncaught
     * exception, and is also called from device handlers.
     */
    public void refreshData() {
        // These states need a user action (new 2-FA code or credentials), which re-initializes the handler.
        AuthState localAuthState = authState;
        if (localAuthState == AuthState.WAIT_FOR_CODE || localAuthState == AuthState.USER_PW_INVALID) {
            logger.debug("Skipping iCloud refresh, waiting for configuration change (AuthState={}).", localAuthState);
            return;
        }
        logger.debug("iCloud bridge refreshing data ...");
        synchronized (this.synchronizeRefresh) {
            ExpiringCache<String> localCache = this.iCloudDeviceInformationCache;
            if (localCache == null) {
                return;
            }
            try {
                String json = localCache.getValue();
                logger.trace("json: {}", json);

                if (json == null) {
                    // The failure has already been reported. Do not keep it cached, so the next refresh retries.
                    localCache.invalidateValue();
                    return;
                }

                ICloudAccountDataResponse iCloudData = JsonUtils.fromJson(json, ICloudAccountDataResponse.class);
                if (iCloudData == null) {
                    return;
                }
                int statusCode = Integer.parseUnsignedInt(iCloudData.getICloudAccountStatusCode());
                if (statusCode == 200) {
                    updateStatus(ThingStatus.ONLINE);
                    informDeviceInformationListeners(iCloudData.getICloudDeviceInformationList());
                } else {
                    updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "iCloud reported status " + statusCode);
                }
                logger.debug("iCloud bridge data refresh complete.");
            } catch (RuntimeException e) {
                logger.debug("iCloud refresh failed.", e);
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "iCloud response invalid: " + e.getMessage());
            }
        }
    }

    private void informDeviceInformationListeners(List<ICloudDeviceInformation> deviceInformationList) {
        this.deviceInformationListeners.forEach(discovery -> discovery.deviceInformationUpdate(deviceInformationList));
    }
}
