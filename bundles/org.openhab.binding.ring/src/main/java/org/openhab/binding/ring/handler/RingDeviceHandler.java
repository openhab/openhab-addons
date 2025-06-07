/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring.handler;

import static org.openhab.binding.ring.RingBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ring.internal.RingAccount;
import org.openhab.binding.ring.internal.RingDeviceRegistry;
import org.openhab.binding.ring.internal.data.RingDevice;
import org.openhab.binding.ring.internal.data.RingDeviceTO;
import org.openhab.binding.ring.internal.errors.DeviceNotFoundException;
import org.openhab.binding.ring.internal.errors.IllegalDeviceClassException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import com.google.gson.Gson;

/**
 * The {@link RingDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public abstract class RingDeviceHandler extends AbstractRingHandler {

    /**
     * The RingDevice instance linked to this thing.
     */
    protected @Nullable RingDevice device;

    public RingDeviceHandler(Thing thing, Gson gson) {
        super(thing, gson);
    }

    protected @Nullable RingDeviceRegistry getDeviceRegistry() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof RingAccount ringAccount) {
                return ringAccount.getDeviceRegistry();
            }
        }
        return null;
    }

    /**
     * Link the device, and update the device with the status CONFIGURED.
     *
     * @param id the device id
     * @param deviceClass the expected class
     * @throws DeviceNotFoundException when device is not found in the RingDeviceRegistry.
     * @throws IllegalDeviceClassException when the registered device is of the wrong type.
     */
    protected void linkDevice(String id, Class<?> deviceClass)
            throws DeviceNotFoundException, IllegalDeviceClassException {
        RingDeviceRegistry registry = getDeviceRegistry();
        if (registry != null) {
            device = registry.getRingDevice(id);
            RingDeviceTO deviceTO = device.getDeviceStatus();
            if (deviceClass.equals(device.getClass())) {
                device.setRegistrationStatus(RingDeviceRegistry.Status.CONFIGURED);
                thing.setProperty("Description", deviceTO.description);
                thing.setProperty("Kind", deviceTO.kind);
                thing.setProperty("Device ID", deviceTO.deviceId);
            } else {
                throw new IllegalDeviceClassException("Class '" + deviceClass.getName() + "' expected but '"
                        + device.getClass().getName() + "' found.");
            }
        }
    }

    /**
     * Handle generic commands, common to all Ring Devices.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof Number || command instanceof RefreshType || command instanceof IncreaseDecreaseType
                || command instanceof UpDownType) {
            switch (channelUID.getId()) {
                case CHANNEL_CONTROL_ENABLED:
                    updateState(channelUID, enabled);
                    break;
                case CHANNEL_STATUS_BATTERY:
                    RingDeviceTO deviceTO = device.getDeviceStatus();
                    updateState(channelUID, new DecimalType(deviceTO.health.batteryPercentage));
                    break;
                default:
                    logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                    break;
            }
            refreshState();
        } else if (command instanceof OnOffType xcommand) {
            switch (channelUID.getId()) {
                case CHANNEL_CONTROL_ENABLED:
                    if (!enabled.equals(xcommand)) {
                        enabled = xcommand;
                        updateState(channelUID, enabled);
                        if (enabled.equals(OnOffType.ON)) {
                            Configuration config = getThing().getConfiguration();
                            int refreshInterval = (int) config.get("refreshInterval");
                            startAutomaticRefresh(refreshInterval);
                        } else {
                            stopAutomaticRefresh();
                        }
                    }
                    break;
                default:
                    logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                    break;
            }
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }
}
