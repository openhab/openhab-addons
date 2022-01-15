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
package org.openhab.binding.tado.internal.handler;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.tado.internal.TadoBindingConstants;
import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.model.MobileDevice;
import org.openhab.binding.tado.internal.config.TadoMobileDeviceConfig;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TadoMobileDeviceHandler} is responsible for handling commands of mobile devices and update their state.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class TadoMobileDeviceHandler extends BaseHomeThingHandler {

    private Logger logger = LoggerFactory.getLogger(TadoMobileDeviceHandler.class);

    private TadoMobileDeviceConfig configuration;
    private ScheduledFuture<?> refreshTimer;

    public TadoMobileDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateState();
        } else {
            logger.warn("This Thing is read-only and can only handle REFRESH command");
        }
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(TadoMobileDeviceConfig.class);

        if (configuration.refreshInterval <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Refresh interval of zone "
                    + configuration.id + " of home " + getHomeId() + " must be greater than zero");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeStatusChanged(bridge.getStatusInfo());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            try {
                MobileDevice device = getMobileDevice();
                updateProperty(TadoBindingConstants.PROPERTY_MOBILE_DEVICE_NAME, device.getName());

                if (!device.getSettings().isGeoTrackingEnabled()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Geotracking is disabled on mobile device " + device.getName());
                    return;
                }
            } catch (IOException | ApiException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not connect to server due to " + e.getMessage());
                cancelScheduledStateUpdate();
                return;
            }

            scheduleZoneStateUpdate();
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            cancelScheduledStateUpdate();
        }
    }

    private void updateState() {
        try {
            MobileDevice device = getMobileDevice();
            updateState(TadoBindingConstants.CHANNEL_MOBILE_DEVICE_AT_HOME,
                    device.getLocation().isAtHome() ? OnOffType.ON : OnOffType.OFF);
        } catch (IOException | ApiException e) {
            logger.debug("Status update of mobile device with id {} failed: {}", configuration.id, e.getMessage());
        }
    }

    private MobileDevice getMobileDevice() throws IOException, ApiException {
        MobileDevice device = null;

        try {
            device = getApi().listMobileDevices(getHomeId()).stream().filter(m -> m.getId() == configuration.id)
                    .findFirst().orElse(null);
        } catch (IOException | ApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not connect to server due to " + e.getMessage());
            throw e;
        }

        if (device == null) {
            String message = "Mobile device with id " + configuration.id + " unknown or does not belong to home "
                    + getHomeId();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
            throw new IOException(message);
        }

        onSuccessfulOperation();
        return device;
    }

    private void scheduleZoneStateUpdate() {
        if (refreshTimer == null || refreshTimer.isCancelled()) {
            refreshTimer = scheduler.scheduleWithFixedDelay(this::updateState, 5, configuration.refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    private void cancelScheduledStateUpdate() {
        if (refreshTimer != null) {
            refreshTimer.cancel(false);
        }
    }
}
