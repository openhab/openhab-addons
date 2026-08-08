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
package org.openhab.binding.ecovacs.internal.handler;

import static org.openhab.binding.ecovacs.internal.EcovacsBindingConstants.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.EcovacsApi;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiException;
import org.openhab.binding.ecovacs.internal.api.EcovacsDevice;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.util.SchedulerTask;
import org.openhab.binding.ecovacs.internal.config.EcovacsVacuumConfiguration;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractEcovacsDeviceHandler} provides shared functionality for Ecovacs device handlers.
 *
 * @author Danny Baumann - Initial contribution
 * @author Stefan Höhn - Refactored from vacuum and mower handlers
 */
@NonNullByDefault
public abstract class AbstractEcovacsDeviceHandler extends BaseThingHandler {

    @FunctionalInterface
    protected interface DeviceAction {
        void run(EcovacsDevice device) throws EcovacsApiException, InterruptedException;
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final TranslationProvider i18Provider;
    protected final LocaleProvider localeProvider;
    protected final Bundle bundle;

    protected final SchedulerTask initTask;
    protected final SchedulerTask reconnectTask;
    protected final SchedulerTask pollTask;

    protected @Nullable EcovacsDevice device;
    protected volatile @Nullable Boolean lastWasCharging;
    protected volatile @Nullable CleanMode lastCleanMode;
    protected long lastSuccessfulPollTimestamp;
    protected String serialNumber = "<unset>";

    protected AbstractEcovacsDeviceHandler(Thing thing, TranslationProvider i18Provider,
            LocaleProvider localeProvider) {
        super(thing);
        this.i18Provider = i18Provider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(getClass());

        initTask = new SchedulerTask(scheduler, logger, "Init", this::initDevice);
        reconnectTask = new SchedulerTask(scheduler, logger, "Connection", this::connectToDevice);
        pollTask = new SchedulerTask(scheduler, logger, "Poll", this::pollData);
    }

    @Override
    public void initialize() {
        serialNumber = getConfigAs(EcovacsVacuumConfiguration.class).serialNumber;
        if (serialNumber.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.config-error-no-serial");
        } else {
            logger.debug("{}: Initializing {} handler", serialNumber, getLogPrefix());
            updateStatus(ThingStatus.UNKNOWN);
            initTask.setNamePrefix(serialNumber);
            reconnectTask.setNamePrefix(serialNumber);
            pollTask.setNamePrefix(serialNumber);
            initTask.submit();
        }
    }

    @Override
    public void dispose() {
        logger.debug("{}: Disposing {} handler", serialNumber, getLogPrefix());
        teardown(false);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("{}: Bridge status changed to {}", serialNumber, bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            initTask.submit();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            teardown(false);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    public void onFirmwareVersionChanged(EcovacsDevice device, String fwVersion) {
        updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, fwVersion);
    }

    public void onBatteryLevelUpdated(EcovacsDevice device, int newLevelPercent) {
        // Some devices report weird values (> 100%), so better clamp to supported range
        int actualPercent = Math.max(0, Math.min(newLevelPercent, 100));
        updateState(CHANNEL_ID_BATTERY_LEVEL, new DecimalType(actualPercent));
    }

    public void onEventStreamFailure(final EcovacsDevice device, Throwable error) {
        logger.debug("{}: Device connection failed, reconnecting", serialNumber, error);
        teardownAndScheduleReconnection();
    }

    protected void teardownAndScheduleReconnection() {
        teardown(true);
    }

    protected synchronized void teardown(boolean scheduleReconnection) {
        EcovacsDevice device = this.device;
        if (device != null) {
            device.disconnect(scheduler);
        }

        pollTask.cancel();
        reconnectTask.cancel();
        initTask.cancel();

        if (scheduleReconnection) {
            SchedulerTask connectTask = device != null ? reconnectTask : initTask;
            connectTask.schedule(5);
        }
    }

    protected synchronized void scheduleNextPoll(long initialDelaySeconds) {
        final EcovacsVacuumConfiguration config = getConfigAs(EcovacsVacuumConfiguration.class);
        final long delayUntilNextPoll;
        if (initialDelaySeconds < 0) {
            long intervalSeconds = config.refresh * 60;
            long secondsSinceLastPoll = (System.currentTimeMillis() - lastSuccessfulPollTimestamp) / 1000;
            long deltaRemaining = intervalSeconds - secondsSinceLastPoll;
            delayUntilNextPoll = Math.max(0, deltaRemaining);
        } else {
            delayUntilNextPoll = initialDelaySeconds;
        }
        logger.debug("{}: Scheduling next poll in {}s, refresh interval {}min", serialNumber, delayUntilNextPoll,
                config.refresh);
        pollTask.cancel();
        pollTask.schedule(delayUntilNextPoll);
    }

    protected @Nullable EcovacsApiHandler getApiHandler() {
        final Bridge bridge = getBridge();
        return bridge != null ? (EcovacsApiHandler) bridge.getHandler() : null;
    }

    protected boolean removeChannel(ThingBuilder builder, String channelId) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId);
        if (getThing().getChannel(channelUID) == null) {
            return false;
        }
        logger.debug("{}: Removing unsupported channel {}", serialNumber, channelId);
        builder.withoutChannel(channelUID);
        return true;
    }

    protected void doWithDevice(DeviceAction action) {
        final EcovacsDevice device = this.device;
        if (device == null) {
            return;
        }
        try {
            action.run(device);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (EcovacsApiException e) {
            logger.debug("{}: Device action failed: {}", serialNumber, e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            teardownAndScheduleReconnection();
        }
    }

    private void initDevice() {
        final EcovacsApiHandler handler = getApiHandler();
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }

        try {
            final EcovacsApi api = handler.createApiForDevice(serialNumber);
            api.loginAndGetAccessToken();
            Optional<EcovacsDevice> deviceOpt = api.getDevices().stream()
                    .filter(d -> serialNumber.equals(d.getSerialNumber())).findFirst();
            if (deviceOpt.isPresent()) {
                EcovacsDevice device = deviceOpt.get();
                this.device = device;
                updateProperty(Thing.PROPERTY_MODEL_ID, device.getModelName());
                updateProperty(Thing.PROPERTY_SERIAL_NUMBER, device.getSerialNumber());
                afterDeviceFound(device);
                connectToDevice();
            } else {
                logger.info("{}: Device not found in device list, setting offline", serialNumber);
                updateStatus(ThingStatus.OFFLINE);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getRawMessage());
        } catch (EcovacsApiException e) {
            logger.debug("API Exception: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Called after the device has been found during initialization, before connecting.
     * Subclasses use this to set up state options and remove unsupported channels.
     */
    protected abstract void afterDeviceFound(EcovacsDevice device);

    /**
     * Connect to the device and fetch initial state.
     */
    protected abstract void connectToDevice();

    /**
     * Poll device data.
     */
    protected abstract void pollData();

    /**
     * Returns a log prefix for this handler type (e.g. "mower" or "vacuum").
     */
    protected abstract String getLogPrefix();
}
