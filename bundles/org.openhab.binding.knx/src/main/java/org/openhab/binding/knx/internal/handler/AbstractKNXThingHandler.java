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
package org.openhab.binding.knx.internal.handler;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.client.DeviceInspector;
import org.openhab.binding.knx.internal.client.DeviceInspector.Result;
import org.openhab.binding.knx.internal.client.KNXClient;
import org.openhab.binding.knx.internal.config.DeviceConfig;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.KNXFormatException;

/**
 * Base class for KNX thing handlers.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@NonNullByDefault
public abstract class AbstractKNXThingHandler extends BaseThingHandler implements GroupAddressListener {

    private static final int INITIAL_PING_DELAY = 5;
    private final Logger logger = LoggerFactory.getLogger(AbstractKNXThingHandler.class);

    protected @Nullable IndividualAddress address;
    private @Nullable Future<?> descriptionJob;
    private boolean filledDescription = false;
    private final Random random = new Random();

    private @Nullable ScheduledFuture<?> pollingJob;

    public AbstractKNXThingHandler(Thing thing) {
        super(thing);
    }

    protected final ScheduledExecutorService getScheduler() {
        return getBridgeHandler().getScheduler();
    }

    protected final ScheduledExecutorService getBackgroundScheduler() {
        return getBridgeHandler().getBackgroundScheduler();
    }

    protected final KNXBridgeBaseThingHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            KNXBridgeBaseThingHandler handler = (KNXBridgeBaseThingHandler) bridge.getHandler();
            if (handler != null) {
                return handler;
            }
        }
        throw new IllegalStateException("The bridge must not be null and must be initialized");
    }

    protected final KNXClient getClient() {
        return getBridgeHandler().getClient();
    }

    protected final boolean describeDevice(@Nullable IndividualAddress address) {
        if (address == null) {
            return false;
        }
        DeviceInspector inspector = new DeviceInspector(getClient().getDeviceInfoClient(), address);
        Result result = inspector.readDeviceInfo();
        if (result != null) {
            Map<String, String> properties = editProperties();
            properties.putAll(result.getProperties());
            updateProperties(properties);
            return true;
        }
        return false;
    }

    protected final String asduToHex(byte[] asdu) {
        final char[] hexCode = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder(2 + asdu.length * 2);
        sb.append("0x");
        for (byte b : asdu) {
            sb.append(hexCode[(b >> 4) & 0xF]);
            sb.append(hexCode[(b & 0xF)]);
        }
        return sb.toString();
    }

    protected final void restart() {
        if (address != null) {
            getClient().restartNetworkDevice(address);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            attachToClient();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            detachFromClient();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void initialize() {
        attachToClient();
    }

    @Override
    public void dispose() {
        detachFromClient();
    }

    protected abstract void scheduleReadJobs();

    protected abstract void cancelReadFutures();

    private void pollDeviceStatus() {
        try {
            if (address != null && getClient().isConnected()) {
                logger.debug("Polling individual address '{}'", address);
                boolean isReachable = getClient().isReachable(address);
                if (isReachable) {
                    updateStatus(ThingStatus.ONLINE);
                    DeviceConfig config = getConfigAs(DeviceConfig.class);
                    if (!filledDescription && config.getFetch()) {
                        Future<?> descriptionJob = this.descriptionJob;
                        if (descriptionJob == null || descriptionJob.isCancelled()) {
                            long initialDelay = Math.round(config.getPingInterval().longValue() * random.nextFloat());
                            this.descriptionJob = getBackgroundScheduler().schedule(() -> {
                                filledDescription = describeDevice(address);
                            }, initialDelay, TimeUnit.SECONDS);
                        }
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        } catch (KNXException e) {
            logger.debug("An error occurred while testing the reachability of a thing '{}': {}", getThing().getUID(),
                    e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    protected void attachToClient() {
        if (!getClient().isConnected()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        DeviceConfig config = getConfigAs(DeviceConfig.class);
        try {
            if (!config.getAddress().isEmpty()) {
                updateStatus(ThingStatus.UNKNOWN);
                address = new IndividualAddress(config.getAddress());

                long pingInterval = config.getPingInterval().longValue();
                long initialPingDelay = Math.round(INITIAL_PING_DELAY * random.nextFloat());

                ScheduledFuture<?> pollingJob = this.pollingJob;
                if ((pollingJob == null || pollingJob.isCancelled())) {
                    logger.debug("'{}' will be polled every {}s", getThing().getUID(), pingInterval);
                    this.pollingJob = getBackgroundScheduler().scheduleWithFixedDelay(() -> pollDeviceStatus(),
                            initialPingDelay, pingInterval, TimeUnit.SECONDS);
                }
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (KNXFormatException e) {
            logger.debug("An exception occurred while setting the individual address '{}': {}", config.getAddress(),
                    e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
        }
        getClient().registerGroupAddressListener(this);
        scheduleReadJobs();
    }

    protected void detachFromClient() {
        final var pollingJobSynced = pollingJob;
        if (pollingJobSynced != null) {
            pollingJobSynced.cancel(true);
            pollingJob = null;
        }
        final var descriptionJobSynced = descriptionJob;
        if (descriptionJobSynced != null) {
            descriptionJobSynced.cancel(true);
            descriptionJob = null;
        }
        cancelReadFutures();
        Bridge bridge = getBridge();
        if (bridge != null) {
            KNXBridgeBaseThingHandler handler = (KNXBridgeBaseThingHandler) bridge.getHandler();
            if (handler != null) {
                handler.getClient().unregisterGroupAddressListener(this);
            }
        }
    }
}
