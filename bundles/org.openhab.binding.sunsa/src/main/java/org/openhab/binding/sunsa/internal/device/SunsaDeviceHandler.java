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
package org.openhab.binding.sunsa.internal.device;

import static org.openhab.binding.sunsa.internal.SunsaBindingConstants.CHANNEL_BATTERY_LEVEL;
import static org.openhab.binding.sunsa.internal.SunsaBindingConstants.CHANNEL_CONFIGURABLE_POSITION;
import static org.openhab.binding.sunsa.internal.SunsaBindingConstants.CHANNEL_RAW_POSITION;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sunsa.internal.SunsaBindingConstants;
import org.openhab.binding.sunsa.internal.bridge.SunsaBridgeHandler;
import org.openhab.binding.sunsa.internal.client.SunsaService;
import org.openhab.binding.sunsa.internal.client.SunsaService.ClientException;
import org.openhab.binding.sunsa.internal.client.SunsaService.ServiceException;
import org.openhab.binding.sunsa.internal.client.SunsaService.SunsaException;
import org.openhab.binding.sunsa.internal.domain.Device;
import org.openhab.binding.sunsa.internal.util.PositionAdapters;
import org.openhab.binding.sunsa.internal.util.PositionAdapters.PositionAdapter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SunsaDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author jirom - Initial contribution
 */
@NonNullByDefault
public class SunsaDeviceHandler extends BaseThingHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SunsaDeviceHandler.class);

    private @Nullable SunsaService sunsaService;
    private @Nullable SunsaDeviceConfiguration config;

    public SunsaDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(SunsaDeviceConfiguration.class);
        this.sunsaService = (SunsaBridgeHandler) getBridge().getHandler();

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            if (isConnected()) {
                updateStatus(ThingStatus.ONLINE);
            } // getState already updates the status with more detail if it thing is not reachable
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState();
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_CONFIGURABLE_POSITION: {
                if (command instanceof StopMoveType) {
                    // We don't support this
                    LOGGER.warn("Received {} command which is not supported and is explicitly ignored.", command);
                    return;
                }

                final int rawPosition = getConfigurablePositionAdapter()
                        .getRawPosition(getPositionInterpolationFactor(command));
                updatePosition(rawPosition);
                break;
            }

            case CHANNEL_RAW_POSITION: {
                final int rawPosition = ((DecimalType) command).intValue();
                updatePosition(rawPosition);
                break;
            }
        }
    }

    public PositionAdapter getConfigurablePositionAdapter() {
        return PositionAdapters.configurablePositionAdapter(
                restrictConfigurablePosition(config.configurablePositionClosed),
                restrictConfigurablePosition(config.configurablePositionOpen));
    }

    private int restrictConfigurablePosition(int position) {
        return Math.max(SunsaBindingConstants.CONFIGURABLE_POSITION_MIN_VALUE,
                Math.min(SunsaBindingConstants.CONFIGURABLE_POSITION_MAX_VALUE, position));
    }

    /**
     * Return a value between [0.0, 1.0] for the value of the command.
     */
    private float getPositionInterpolationFactor(final Command command) {
        final float rawValue;
        if (command instanceof PercentType) {
            rawValue = ((PercentType) command).floatValue();
        } else if (command instanceof UpDownType) {
            rawValue = ((UpDownType) command).as(PercentType.class).floatValue();
        } else if (command instanceof OpenClosedType) {
            rawValue = ((OpenClosedType) command).as(PercentType.class).floatValue();
        } else if (command instanceof DecimalType) {
            rawValue = ((DecimalType) command).floatValue();
        } else {
            LOGGER.error("Unknown command: {}", command);
            rawValue = 0;
        }

        return rawValue / PercentType.HUNDRED.floatValue();
    }

    private void updatePosition(final int rawPosition) {
        try {
            final int updatedRawPosition = callAndSetStatus(
                    () -> sunsaService.setDevicePosition(config.id, rawPosition));
            updatePositionState(updatedRawPosition);
        } finally {
        }
    }

    private void updateState() {
        getDevice().ifPresent(this::updateState);
    }

    private void updateState(final Device newDeviceState) {
        updateState(CHANNEL_BATTERY_LEVEL, new DecimalType(new BigDecimal(newDeviceState.getBatteryLevel())));
        updatePositionState(newDeviceState.getRawPosition());
    }

    private void updatePositionState(final int rawPosition) {
        updateState(CHANNEL_RAW_POSITION, new DecimalType(new BigDecimal(rawPosition)));

        final PercentType newConfigurablePositionState = new PercentType(
                getConfigurablePositionAdapter().getLocalPosition(rawPosition));
        updateState(CHANNEL_CONFIGURABLE_POSITION, newConfigurablePositionState);
    }

    private boolean isConnected() {
        return getDevice().map(Device::isConnected).orElse(false);
    }

    private Optional<Device> getDevice() {
        try {
            return Optional.of(callAndSetStatus(() -> sunsaService.getDevice(config.id)));
        } catch (SunsaException e) {
            return Optional.empty();
        }
    }

    private <T> T callAndSetStatus(Supplier<T> apiCall) {
        try {
            final T result = apiCall.get();
            updateStatus(ThingStatus.ONLINE);
            return result;
        } catch (ClientException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw e;
        } catch (ServiceException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            throw e;
        } catch (SunsaException e) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, e.getMessage());
            throw e;
        }
    }
}
