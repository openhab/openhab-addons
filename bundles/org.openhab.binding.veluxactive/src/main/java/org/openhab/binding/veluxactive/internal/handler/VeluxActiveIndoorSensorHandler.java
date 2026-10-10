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
package org.openhab.binding.veluxactive.internal.handler;

import static org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.veluxactive.internal.ModuleRequestContext;
import org.openhab.binding.veluxactive.internal.api.ModuleInfo;
import org.openhab.binding.veluxactive.internal.api.ModuleType;
import org.openhab.binding.veluxactive.internal.api.dto.Home;
import org.openhab.binding.veluxactive.internal.api.dto.HomeStatus;
import org.openhab.binding.veluxactive.internal.api.dto.Module;
import org.openhab.binding.veluxactive.internal.api.dto.Room;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.UnDefType;

/**
 * Combines physical NXS diagnostics with measurements from its assigned room.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class VeluxActiveIndoorSensorHandler extends VeluxActiveBatteryModuleHandler {
    private @Nullable String roomId;

    public VeluxActiveIndoorSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        synchronized (this) {
            roomId = null;
        }
        super.initialize();
    }

    synchronized void updateTopology(ModuleRequestContext requestContext, List<ModuleInfo> modules) {
        if (!isRequestContextCurrent(requestContext)) {
            return;
        }
        ModuleInfo moduleInfo = modules.stream()
                .filter(module -> requestContext.homeId().equals(module.homeId())
                        && requestContext.moduleId().equals(module.moduleId())
                        && module.type() == ModuleType.INDOOR_SENSOR)
                .findFirst().orElse(null);
        roomId = moduleInfo == null ? null : moduleInfo.roomId();
        String roomName = moduleInfo == null ? null : moduleInfo.roomName();
        Map<String, String> properties = editProperties();
        if (roomName != null && !roomName.isBlank()) {
            properties.put("roomName", roomName);
        } else {
            properties.remove("roomName");
        }
        updateProperties(properties);
    }

    @Override
    synchronized void update(ModuleRequestContext requestContext, HomeStatus status) {
        if (!isRequestContextCurrent(requestContext)) {
            return;
        }
        Module module = status.module(requestContext.moduleId());
        if (module != null && !checkGateway(requestContext, module)) {
            return;
        }
        if (status.error(requestContext.moduleId()) != null
                || module != null && Boolean.FALSE.equals(module.reachable)) {
            failed(requestContext, "@text/offline.comm-error.indoor-sensor.unreachable");
            return;
        }
        if (module == null) {
            missingStatus("@text/status.indoor-sensor.missing");
            return;
        }
        updateDiagnostics(module);
        Room room = room(status.home);
        if (room == null) {
            clearClimate();
        } else {
            Integer temperature = room.temperature;
            Integer humidity = room.humidity;
            Integer co2 = room.co2;
            Integer light = room.lux;
            updateState(CHANNEL_TEMPERATURE,
                    temperature == null ? UnDefType.UNDEF : new QuantityType<>(temperature / 10.0, SIUnits.CELSIUS));
            updateState(CHANNEL_HUMIDITY,
                    humidity != null && humidity >= 0 && humidity <= 100 ? new QuantityType<>(humidity, Units.PERCENT)
                            : UnDefType.UNDEF);
            updateState(CHANNEL_CO2,
                    co2 != null && co2 >= 0 ? new QuantityType<>(co2, Units.PARTS_PER_MILLION) : UnDefType.UNDEF);
            updateState(CHANNEL_ILLUMINANCE,
                    light != null && light >= 0 ? new QuantityType<>(light, Units.LUX) : UnDefType.UNDEF);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private @Nullable Room room(@Nullable Home home) {
        String id = roomId;
        List<@Nullable Room> rooms = home == null ? null : home.rooms;
        if (id != null && rooms != null) {
            for (Room room : rooms) {
                if (room != null && id.equals(room.id)) {
                    return room;
                }
            }
        }
        return null;
    }

    private void clearClimate() {
        updateState(CHANNEL_TEMPERATURE, UnDefType.UNDEF);
        updateState(CHANNEL_HUMIDITY, UnDefType.UNDEF);
        updateState(CHANNEL_CO2, UnDefType.UNDEF);
        updateState(CHANNEL_ILLUMINANCE, UnDefType.UNDEF);
    }

    @Override
    protected void clearState() {
        clearClimate();
        super.clearState();
    }
}
