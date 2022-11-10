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
package org.openhab.binding.icloud.internal.handler;

import static java.util.concurrent.TimeUnit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.icloud.internal.ICloudAPIResponseException;
import org.openhab.binding.icloud.internal.ICloudDeviceInformationListener;
import org.openhab.binding.icloud.internal.ICloudDeviceInformationParser;
import org.openhab.binding.icloud.internal.ICloudService;
import org.openhab.binding.icloud.internal.configuration.ICloudAccountThingConfiguration;
import org.openhab.binding.icloud.internal.json.response.ICloudAccountDataResponse;
import org.openhab.binding.icloud.internal.json.response.ICloudDeviceInformation;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.storage.Storage;
import org.openhab.core.test.storage.VolatileStorage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * Retrieves the data for a given account from iCloud and passes the information to
 * {@link org.openhab.binding.icloud.internal.discovery.ICloudDeviceDiscovery} and to the {@link ICloudDeviceHandler}s.
 *
 * @author Patrik Gfeller - Initial contribution
 * @author Hans-Jörg Merk - Extended support with initial Contribution
 */
@NonNullByDefault
public class ICloudAccountBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(ICloudAccountBridgeHandler.class);

    private static final int CACHE_EXPIRY = (int) SECONDS.toMillis(10);

    private final ICloudDeviceInformationParser deviceInformationParser = new ICloudDeviceInformationParser();

    private @Nullable ICloudService iCloudService;

    private @Nullable ExpiringCache<String> iCloudDeviceInformationCache;

    private @Nullable ScheduledFuture<?> checkLoginJob;

    private boolean validate2faCode = false;

    @Nullable
    private ServiceRegistration<?> service;

    private final Object synchronizeRefresh = new Object();

    private List<ICloudDeviceInformationListener> deviceInformationListeners = Collections
            .synchronizedList(new ArrayList<>());

    @Nullable
    ScheduledFuture<?> refreshJob;

    private Storage<String> storage;

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

    @Override
    public void initialize() {

        logger.debug("iCloud bridge handler initializing ...");
        synchronized (synchronizeRefresh) {
            ICloudAccountThingConfiguration config = getConfigAs(ICloudAccountThingConfiguration.class);
            final String localAppleId = config.appleId;
            final String localPassword = config.password;

            if (this.iCloudService == null) {
                if (localAppleId != null && localPassword != null) {
                    // this.iCloudService = new ICloudService(localAppleId, localPassword, this.storage);
                    this.iCloudService = new ICloudService(localAppleId, localPassword, new VolatileStorage<String>());
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Apple ID/Password is not set!");
                    return;

                }
            }

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Wait for login");

            this.checkLoginJob = this.scheduler.scheduleWithFixedDelay(this::checkLogin, 0, 60, TimeUnit.MINUTES);

            this.refreshJob = this.scheduler.scheduleWithFixedDelay(this::refreshData, 0, config.refreshTimeInMinutes,
                    MINUTES);

            this.iCloudDeviceInformationCache = new ExpiringCache<>(CACHE_EXPIRY, () -> {
                try {

                    return this.iCloudService.getDevices().refreshClient();
                } catch (IOException | ICloudAPIResponseException | InterruptedException e) {
                    logger.warn("Unable to refresh device data", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    return null;
                } catch (IllegalStateException ex) {
                    logger.warn("Need to authenticate first.", ex);
                    return null;
                }
            });
        }
        logger.debug("iCloud bridge initialized.");
    }

    @Override
    public void handleRemoval() {

        super.handleRemoval();
    }

    @Override
    public void dispose() {
        if (this.checkLoginJob != null) {
            this.checkLoginJob.cancel(true);
        }
        if (this.refreshJob != null) {
            this.refreshJob.cancel(true);
        }
        super.dispose();
    }

    public void findMyDevice(String deviceId) throws IOException, InterruptedException {

        this.iCloudService.getDevices().playSound(deviceId);
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
    private void checkLogin() {

        logger.debug("Starting iCloud authentication ...");
        synchronized (synchronizeRefresh) {
            try {
                ICloudAccountThingConfiguration config = getConfigAs(ICloudAccountThingConfiguration.class);
                boolean success = false;
                if (validate2faCode) {
                    // 2-FA-Code was requested in previous call of this method.
                    if (config.code == null || config.code.isBlank()) {
                        // Still waiting for user to update config.
                        logger.warn(
                                "ICloud authentication requires 2-FA code. Please provide code in in thing configuration.");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Please provide 2-FA code in thing configuration.");
                        return;
                    } else {
                        // User has provided code in config.
                        validate2faCode = false;
                        logger.debug("Trying to authenticate token...");
                        success = this.iCloudService.validate2faCode(config.code);
                        if (!success) {
                            logger.warn("ICloud token invalid.");
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid token.");
                            return;
                        }
                        org.openhab.core.config.core.Configuration config2 = editConfiguration();
                        config2.put("code", "");
                        updateConfiguration(config2);

                        logger.debug("Token is valid.");
                    }

                } else {
                    // No code requested yet or session is trusted (hopefully).
                    success = this.iCloudService.authenticate(false);
                    if (!success) {
                        logger.warn("ICloud authentication failed.");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Invalid credentials.");
                        return;
                    }
                    if (iCloudService.requires2fa()) {
                        // New code was requested. Wait for the user to update config.
                        String code = "999999";
                        success = this.iCloudService.validate2faCode(code);
                        logger.warn(
                                "ICloud authentication requires 2-FA code. Please provide code in in thing configuration.");
                        validate2faCode = true;
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Please provide 2-FA code in thing configuration.");
                        return;
                    }
                }

                if (!this.iCloudService.isTrustedSession()) {
                    logger.debug("Trying to establish session trust.");
                    success = this.iCloudService.trustSession();
                    if (!success) {
                        logger.warn("ICloud trust session failed.");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Session trust failed.");
                        return;
                    }
                }

                updateStatus(ThingStatus.ONLINE);
                logger.debug("iCloud bridge handler started.");

            } catch (IOException | InterruptedException e) {
                logger.warn("ICloud authentication caused exception.", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (Exception e) {
                logger.debug("Something went wrong while constructing the icloud session", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
        }
    }

    public void refreshData() {

        logger.debug("iCloud bridge refreshing data ...");
        synchronized (this.synchronizeRefresh) {

            String json = this.iCloudDeviceInformationCache.getValue();
            logger.trace("json: {}", json);

            if (json == null) {
                return;
            }

            try {
                ICloudAccountDataResponse iCloudData = this.deviceInformationParser.parse(json);
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
