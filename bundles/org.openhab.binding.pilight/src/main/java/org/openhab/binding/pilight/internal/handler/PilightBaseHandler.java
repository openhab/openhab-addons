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
package org.openhab.binding.pilight.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pilight.internal.PilightDeviceConfiguration;
import org.openhab.binding.pilight.internal.dto.Action;
import org.openhab.binding.pilight.internal.dto.Config;
import org.openhab.binding.pilight.internal.dto.Device;
import org.openhab.binding.pilight.internal.dto.Status;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PilightBaseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stefan Röllin - Initial contribution
 * @author Niklas Dörfler - Port pilight binding to openHAB 3 + add device discovery
 */
@NonNullByDefault
public abstract class PilightBaseHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PilightBaseHandler.class);

    private String name = "";

    public PilightBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshConfigAndStatus();
            return;
        }

        @Nullable
        Action action = createUpdateCommand(channelUID, command);
        if (action != null) {
            sendAction(action);
        }
    }

    @Override
    public void initialize() {
        PilightDeviceConfiguration config = getConfigAs(PilightDeviceConfiguration.class);
        name = config.getName();

        refreshConfigAndStatus();
    }

    public void updateFromStatusIfMatches(Status status) {
        if (status.getDevices() != null && !status.getDevices().isEmpty()) {
            if (name.equals(status.getDevices().get(0))) {
                if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
                    updateStatus(ThingStatus.ONLINE);
                }
                updateFromStatus(status);
            }
        }
    }

    public void updateFromConfigIfMatches(Config config) {
        Device device = config.getDevices().get(getName());
        if (device != null) {
            updateFromConfigDevice(device);
        }
    }

    abstract void updateFromStatus(Status status);

    abstract void updateFromConfigDevice(Device device);

    abstract @Nullable Action createUpdateCommand(ChannelUID channelUID, Command command);

    protected String getName() {
        return name;
    }

    private void sendAction(Action action) {
        final @Nullable PilightBridgeHandler handler = getPilightBridgeHandler();
        if (handler != null) {
            handler.sendAction(action);
        } else {
            logger.warn("No pilight bridge handler found to send action.");
        }
    }

    private void refreshConfigAndStatus() {
        final @Nullable PilightBridgeHandler handler = getPilightBridgeHandler();
        if (handler != null) {
            handler.refreshConfigAndStatus();
        } else {
            logger.warn("No pilight bridge handler found to refresh config and status.");
        }
    }

    private @Nullable PilightBridgeHandler getPilightBridgeHandler() {
        final @Nullable Bridge bridge = getBridge();
        if (bridge != null) {
            @Nullable
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof PilightBridgeHandler) {
                return (PilightBridgeHandler) handler;
            }
        }
        return null;
    }
}
