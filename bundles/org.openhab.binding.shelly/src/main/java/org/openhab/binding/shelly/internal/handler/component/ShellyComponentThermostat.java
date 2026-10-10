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
package org.openhab.binding.shelly.internal.handler.component;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_THERMOSTAT_SETCONFIG;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusSys;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcRequest;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiRpc;
import org.openhab.binding.shelly.internal.api2.dto.ShellyThermostatJsonDTO.Shelly2DeviceStatusThermostat;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyComponentThermostat} implements the Gen2+ Thermostat component (e.g. Wall Display): status
 * mapping, channel creation/removal, channel updates and command handling.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyComponentThermostat {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellyComponentThermostat.class);

    private static final String ENABLE_CHANNEL_ID = CHANNEL_GROUP_CONTROL + ChannelUID.CHANNEL_GROUP_SEPARATOR
            + CHANNEL_THERMOSTAT_ENABLE;
    private static final String TARGET_TEMP_CHANNEL_ID = CHANNEL_GROUP_CONTROL + ChannelUID.CHANNEL_GROUP_SEPARATOR
            + CHANNEL_CONTROL_SETTEMP;

    /**
     * A NotifyStatus only carries the components it reports, so only a full GetStatus may clear the thermostat
     * component (thermostat disabled in the Shelly app), which in turn removes its channels.
     */
    public static void fillStatus(ShellySettingsStatus status, Shelly2DeviceStatusResult result, boolean fullStatus) {
        Shelly2DeviceStatusSys sys = result.sys;
        if (sys != null) {
            if (fullStatus || sys.relayInThermostat != null) {
                status.relayInThermostat = sys.relayInThermostat;
            }
            if (fullStatus || sys.sensorInThermostat != null) {
                status.sensorInThermostat = sys.sensorInThermostat;
            }
        }
        if (fullStatus || result.thermostat0 != null) {
            status.thermostat = result.thermostat0;
        }
    }

    /**
     * The thermostat can be enabled/disabled at any time, so create/remove its channels as soon as the component
     * (dis)appears. targetTemp is shared with the TRV and is never removed for those. current_C/output are already
     * covered by sensors#temperature / relay#output.
     */
    public static void updateChannels(ShellyThingInterface thingHandler, ShellySettingsStatus status) {
        if (status.relayInThermostat != null) {
            thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_RELAY_IN_THERMOSTAT,
                    getOnOff(status.relayInThermostat));
        }
        if (status.sensorInThermostat != null) {
            thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_SENSOR_IN_THERMOSTAT,
                    getOnOff(status.sensorInThermostat));
        }

        Shelly2DeviceStatusThermostat thermostat = status.thermostat;
        if (thermostat == null) {
            thingHandler.removeChannels(thingHandler.getProfile().isTRV ? Set.of(ENABLE_CHANNEL_ID)
                    : Set.of(ENABLE_CHANNEL_ID, TARGET_TEMP_CHANNEL_ID));
            return;
        }

        Map<String, Channel> channels = new LinkedHashMap<>();
        ShellyChannelDefinitions.addChannel(thingHandler.getThing(), channels, true, CHANNEL_GROUP_CONTROL,
                CHANNEL_THERMOSTAT_ENABLE);
        ShellyChannelDefinitions.addChannel(thingHandler.getThing(), channels, true, CHANNEL_GROUP_CONTROL,
                CHANNEL_CONTROL_SETTEMP);
        thingHandler.updateThingChannels(Map.of(), channels);

        if (thermostat.enable != null) {
            thingHandler.updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_THERMOSTAT_ENABLE, getOnOff(thermostat.enable));
        }
        if (thermostat.targetC != null) {
            thingHandler.updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_SETTEMP,
                    toQuantityType(thermostat.targetC, DIGITS_TEMP, SIUnits.CELSIUS));
        }
    }

    public static void handleCommand(ShellyThingInterface thingHandler, String channel, Command command)
            throws ShellyApiException {
        if (!(thingHandler.getApi() instanceof Shelly2ApiRpc api)) {
            throw new ShellyApiException("Thermostat commands require a Gen2+ device");
        }
        Shelly2RpcRequest request = new Shelly2RpcRequest().withMethod(SHELLYRPC_METHOD_THERMOSTAT_SETCONFIG).withId(0);
        request.params.withConfig();
        switch (channel) {
            case CHANNEL_THERMOSTAT_ENABLE:
                request.params.config.enable = command == OnOffType.ON;
                break;
            case CHANNEL_CONTROL_SETTEMP:
                request.params.config.targetC = getNumber(command).doubleValue();
                break;
            default:
                return;
        }
        LOGGER.debug("{}: Set thermostat {} to {}", thingHandler.getThingName(), channel, command);
        api.apiRequest(request);
    }
}
