/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.warmup.internal.handler;

import static org.openhab.binding.warmup.internal.WarmupBindingConstants.*;

import java.math.BigDecimal;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.warmup.internal.model.query.LocationDTO;
import org.openhab.binding.warmup.internal.model.query.QueryResponseDTO;
import org.openhab.binding.warmup.internal.model.query.RoomDTO;

/**
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class RoomHandler extends BaseThingHandler {

    private String serialNumber = "";
    private Integer overrideDuration = 60;

    private @Nullable MyWarmupAccountHandler bridgeHandler;

    public RoomHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        serialNumber = (String) getConfig().get("serialNumber");
        overrideDuration = ((BigDecimal) getConfig().get("overrideDuration")).intValue();
        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeHandler = (MyWarmupAccountHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                bridgeHandler.refreshFromCache();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_TARGET_TEMPERATURE.equals(channelUID.getId()) && command instanceof QuantityType<?>) {
            setSetPoint((QuantityType<Temperature>) command);
        }
        if (command instanceof RefreshType && bridgeHandler != null) {
            bridgeHandler.refreshFromCache();
        }
    }

    /**
     * Process device list and populate room properties, status and state
     *
     * @param domain Data model representing all devices
     */
    public void onRefresh(@Nullable QueryResponseDTO domain) {
        if (domain != null) {
            for (LocationDTO location : domain.getData().getUser().getLocations()) {
                for (RoomDTO room : location.getRooms()) {
                    if (room.getThermostat4ies().get(0).getDeviceSN().equals(serialNumber)) {
                        updateStatus(ThingStatus.ONLINE);

                        updateProperty("Id", room.getId().toString());
                        updateProperty("Serial Number", serialNumber);
                        updateProperty("Name", room.getName());
                        updateProperty("Location", location.getName());

                        updateState(CHANNEL_CURRENT_TEMPERATURE,
                                room.getCurrentTemperature() != null
                                        ? new QuantityType<>(room.getCurrentTemperature() / 10.0, SIUnits.CELSIUS)
                                        : UnDefType.UNDEF);
                        updateState(CHANNEL_TARGET_TEMPERATURE,
                                room.getTargetTemperature() != null
                                        ? new QuantityType<>(room.getTargetTemperature() / 10.0, SIUnits.CELSIUS)
                                        : UnDefType.UNDEF);
                        updateState(CHANNEL_OVERRIDE_DURATION,
                                room.getOverrideDuration() != null
                                        ? new QuantityType<>(room.getOverrideDuration(), SmartHomeUnits.MINUTE)
                                        : UnDefType.UNDEF);
                        updateState(CHANNEL_RUN_MODE,
                                room.getRunMode() != null ? new StringType(room.getRunMode()) : UnDefType.UNDEF);
                        return;
                    }
                }
            }
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Room not found");
    }

    private void setSetPoint(final QuantityType<Temperature> command) {
        final QuantityType<Temperature> value = command.toUnit(SIUnits.CELSIUS);

        if (value != null && bridgeHandler != null) {
            bridgeHandler.getApi().setTargetTemperature(Integer.valueOf(getThing().getProperties().get("Id")), value,
                    overrideDuration);
        }
    }
}
