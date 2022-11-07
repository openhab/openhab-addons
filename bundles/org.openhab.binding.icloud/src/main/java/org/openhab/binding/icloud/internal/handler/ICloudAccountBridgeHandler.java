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

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.icloud.internal.ICloudDeviceInformationListener;
import org.openhab.binding.icloud.internal.ICloudDeviceInformationParser;
import org.openhab.binding.icloud.internal.ICloudService;
import org.openhab.binding.icloud.internal.configuration.ICloudAccountThingConfiguration;
import org.openhab.binding.icloud.internal.json.response.ICloudAccountDataResponse;
import org.openhab.binding.icloud.internal.json.response.ICloudDeviceInformation;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.storage.Storage;
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

    @Nullable
    ServiceRegistration<?> service;

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

        this.logger.trace("Command '{}' received for channel '{}'", command, channelUID);

        if (command instanceof RefreshType) {
            refreshData();
        }
    }

    @Override
    public void initialize() {

        this.logger.debug("iCloud bridge handler initializing ...");
        this.iCloudDeviceInformationCache = new ExpiringCache<>(CACHE_EXPIRY, () -> {
            try {
                if (!this.iCloudService.isTrustedSession()) {
                    this.logger.debug("Trying to authenticate token...");
                    ICloudAccountThingConfiguration config = getConfigAs(ICloudAccountThingConfiguration.class);
                    if (config.code == null || config.code.isBlank()) {
                        throw new IOException("Provide code in thing config.");
                    }
                    boolean result = this.iCloudService.validate2faCode(config.code);
                    if (!result)
                        throw new IOException("Cannot authenticate token");

                }
                return this.iCloudService.getDevices().refreshClient();
            } catch (IOException | InterruptedException e) {
                this.logger.warn("Unable to refresh device data", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                return null;
            }
        });

        startHandler();
        this.logger.debug("iCloud bridge initialized.");
    }

    @Override
    public void handleRemoval() {

        super.handleRemoval();
    }

    @Override
    public void dispose() {

        if (this.refreshJob != null) {
            this.refreshJob.cancel(true);
        }
        super.dispose();
    }

    public void findMyDevice(String deviceId) throws IOException, InterruptedException {

        if (this.iCloudService == null) {
            this.logger.debug("Can't send Find My Device request, because connection is null!");
            return;
        }
        this.iCloudService.getDevices().playSound(deviceId);
    }

    public void registerListener(ICloudDeviceInformationListener listener) {

        this.deviceInformationListeners.add(listener);
    }

    public void unregisterListener(ICloudDeviceInformationListener listener) {

        this.deviceInformationListeners.remove(listener);
    }

    private void startHandler() {

        try {
            this.logger.debug("iCloud bridge starting handler ...");
            ICloudAccountThingConfiguration config = getConfigAs(ICloudAccountThingConfiguration.class);
            final String localAppleId = config.appleId;
            final String localPassword = config.password;
            if (localAppleId != null && localPassword != null) {
                this.iCloudService = new ICloudService(localAppleId, localPassword, this.storage);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Apple ID/Password is not set!");
                return;
            }
            this.refreshJob = this.scheduler.scheduleWithFixedDelay(this::refreshData, 0, config.refreshTimeInMinutes,
                    MINUTES);

            this.logger.debug("iCloud bridge handler started.");
        } catch (Exception e) {
            this.logger.debug("Something went wrong while constructing the connection object", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    public void refreshData() {

        synchronized (this.synchronizeRefresh) {
            this.logger.debug("iCloud bridge refreshing data ...");

            String json = this.iCloudDeviceInformationCache.getValue();
            this.logger.trace("json: {}", json);

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
                this.logger.debug("iCloud bridge data refresh complete.");
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
