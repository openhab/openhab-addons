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
import org.openhab.binding.icloud.internal.RetryException;
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

import com.google.gson.JsonSyntaxException;

/**
 * Retrieves the data for a given account from iCloud and passes the information to
 * {@link org.openhab.binding.icloud.internal.discovery.ICloudDeviceDiscovery} and to the {@link ICloudDeviceHandler}s.
 *
 * @author Patrik Gfeller - Initial contribution
 * @author Hans-Jörg Merk - Extended support with initial Contribution
 * @author Simon Spielmann - Rework for new iCloud API
 */
@NonNullByDefault
public class ICloudAccountBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(ICloudAccountBridgeHandler.class);

    private static final int CACHE_EXPIRY = (int) SECONDS.toMillis(10);

    private @Nullable ICloudService iCloudService;

    private @Nullable ExpiringCache<String> iCloudDeviceInformationCache;

    private AuthState authState = AuthState.INITIAL;

    private final Object synchronizeRefresh = new Object();

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

        this.iCloudDeviceInformationCache = new ExpiringCache<>(CACHE_EXPIRY, () -> {
            return callApiWithRetryAndExceptionHandling(() -> {
                // callApiWithRetryAndExceptionHanlding ensures that iCloudService is not null when the following is
                // called. Cannot use method local iCloudService instance here, because instance may be replaced with a
                // new
                // one during retry.
                return iCloudService.getDevices().refreshClient();
            });

        });

        updateStatus(ThingStatus.UNKNOWN);

        // Init has to be done async becaue it requires sync network calls, which are not allowed in init.
        Callable<?> asyncInit = () -> {
            callApiWithRetryAndExceptionHandling(() -> {
                logger.debug("Dummy call for initial authentication.");
                return null;
            });
            if (authState == AuthState.AUTHENTICATED) {
                ICloudAccountThingConfiguration config = getConfigAs(ICloudAccountThingConfiguration.class);
                this.refreshJob = this.scheduler.scheduleWithFixedDelay(this::refreshData, 0,
                        config.refreshTimeInMinutes, MINUTES);
            } else {
                cancelRefresh();
            }
            return null;
        };
        initTask = this.scheduler.schedule(asyncInit, 0, TimeUnit.SECONDS);
        logger.debug("iCloud bridge handler initialized.");
    }

    private <@Nullable T> T callApiWithRetryAndExceptionHandling(Callable<T> wrapped) {
        int retryCount = 1;
        boolean success = false;
        Throwable lastException = null;
        synchronized (synchronizeRefresh) {
            if (this.iCloudService == null) {
                ICloudAccountThingConfiguration config = getConfigAs(ICloudAccountThingConfiguration.class);
                final String localAppleId = config.appleId;
                final String localPassword = config.password;

                if (localAppleId != null && localPassword != null) {
                    this.iCloudService = new ICloudService(localAppleId, localPassword, this.storage);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Apple ID or password is not set!");
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
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Error while validating 2-FA code.");
                    return null;
                }
            }
            if (authState != AuthState.AUTHENTICATED && !success) {
                return null;
            }

            do {
                try {
                    if (authState == AuthState.AUTHENTICATED) {
                        return wrapped.call();
                    } else {
                        checkLogin();
                    }
                } catch (ICloudApiResponseException e) {
                    logger.debug("ICloudApiResponseException with status code {}", e.getStatusCode());
                    lastException = e;
                    if (e.getStatusCode() == 450) {
                        checkLogin();
                    }
                } catch (IllegalStateException e) {
                    logger.debug("Need to authenticate first.", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Wait for login");
                    return null;
                } catch (IOException e) {
                    logger.warn("Unable to refresh device data", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    return null;
                } catch (Exception e) {
                    logger.debug("Unexpected exception occured", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    return null;
                }

                retryCount++;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            } while (!success && retryCount < 3);
            throw new RetryException(lastException);
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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Please provide 2-FA code in thing configuration.");
            return false;
        } else {
            // 2-FA-Code was requested in previous call of this method.
            // User has provided code in config.
            logger.debug("Code is given in thing configuration '{}'. Trying to validate code...",
                    getThing().getUID().getAsString());
            storage.put(AUTH_CODE_KEY, lastTriedCode);
            success = localICloudService.validate2faCode(code);
            if (!success) {
                authState = AuthState.INITIAL;
                logger.warn("ICloud token invalid.");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid 2-FA-code.");
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
        cancelRefresh();

        final ScheduledFuture<?> localInitTask = this.initTask;
        if (localInitTask != null) {
            localInitTask.cancel(true);
            this.initTask = null;
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
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid credentials.");
                this.iCloudService = null;
                return false;
            }
            if (localICloudService.requires2fa()) {
                // New code was requested. Wait for the user to update config.
                logger.warn(
                        "iCloud authentication requires 2-FA code. Please provide code configuration for thing '{}'.",
                        getThing().getUID().getAsString());
                authState = AuthState.WAIT_FOR_CODE;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Please provide 2-FA code in thing configuration.");
                return false;
            }

            if (!localICloudService.isTrustedSession()) {
                logger.debug("Trying to establish session trust.");
                success = localICloudService.trustSession();
                if (!success) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Session trust failed.");
                    return false;
                }
            }

            authState = AuthState.AUTHENTICATED;
            updateStatus(ThingStatus.ONLINE);
            logger.debug("iCloud bridge handler authenticated.");
            return true;
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return false;
        }
    }

    /**
     * Refresh iCloud device data.
     */
    public void refreshData() {
        logger.debug("iCloud bridge refreshing data ...");
        synchronized (this.synchronizeRefresh) {
            ExpiringCache<String> localCache = this.iCloudDeviceInformationCache;
            if (localCache == null) {
                return;
            }
            String json = localCache.getValue();
            logger.trace("json: {}", json);

            if (json == null) {
                return;
            }

            try {
                ICloudAccountDataResponse iCloudData = JsonUtils.fromJson(json, ICloudAccountDataResponse.class);
                if (iCloudData == null) {
                    return;
                }
                int statusCode = Integer.parseUnsignedInt(iCloudData.getICloudAccountStatusCode());
                if (statusCode == 200) {
                    updateStatus(ThingStatus.ONLINE);
                    informDeviceInformationListeners(iCloudData.getICloudDeviceInformationList());
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Status = " + statusCode + ", Response = " + json);
                }
                logger.debug("iCloud bridge data refresh complete.");
            } catch (NumberFormatException | JsonSyntaxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "iCloud response invalid: " + e.getMessage());
            }
        }
    }

    private void informDeviceInformationListeners(List<ICloudDeviceInformation> deviceInformationList) {
        this.deviceInformationListeners.forEach(discovery -> discovery.deviceInformationUpdate(deviceInformationList));
    }
}
