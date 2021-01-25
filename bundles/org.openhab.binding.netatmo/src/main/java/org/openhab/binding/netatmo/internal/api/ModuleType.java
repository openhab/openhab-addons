/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.api.NetatmoConstants.*;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.BatteryHelper;
import org.openhab.binding.netatmo.internal.channelhelper.CameraChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.Co2ChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.DeviceChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HomeCoachChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HomeEnergyChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HomeSecurityChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HumidityChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.MeasuresChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.ModuleChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.NoiseChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PersonChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PlugChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PresenceChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PressureChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.RainChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.TemperatureChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.Therm1PropsChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.Therm1SetpointChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.Therm1TempChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.WindChannelHelper;
import org.openhab.binding.netatmo.internal.handler.CameraHandler;
import org.openhab.binding.netatmo.internal.handler.HomeCoachHandler;
import org.openhab.binding.netatmo.internal.handler.HomeEnergyHandler;
import org.openhab.binding.netatmo.internal.handler.HomeSecurityHandler;
import org.openhab.binding.netatmo.internal.handler.MainHandler;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.handler.PersonHandler;
import org.openhab.binding.netatmo.internal.handler.PlugHandler;
import org.openhab.binding.netatmo.internal.handler.PresenceHandler;
import org.openhab.binding.netatmo.internal.handler.Therm1Handler;
import org.openhab.core.thing.ThingTypeUID;

/**
 * This enum all handled Netatmo modules and devices along with their capabilities
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum ModuleType {
    // Security Group
    NAHomeSecurity(HomeSecurityHandler.class, RefreshPolicy.CONFIG, null, null, Set.of(HomeSecurityChannelHelper.class),
            List.of(GROUP_HOME_SECURITY)),
    NAPerson(PersonHandler.class, RefreshPolicy.PARENT, NAHomeSecurity, null, Set.of(PersonChannelHelper.class),
            List.of(GROUP_PERSON, GROUP_PERSON_EVENT)),
    NACamera(CameraHandler.class, RefreshPolicy.PARENT, NAHomeSecurity, null, Set.of(CameraChannelHelper.class),
            List.of(GROUP_WELCOME, GROUP_WELCOME_EVENT)),
    NOC(PresenceHandler.class, RefreshPolicy.PARENT, NAHomeSecurity, null,
            Set.of(CameraChannelHelper.class, PresenceChannelHelper.class),
            List.of(GROUP_WELCOME, GROUP_WELCOME_EVENT, GROUP_PRESENCE)),

    // Weather group
    NAMain(MainHandler.class, RefreshPolicy.AUTO, null, List.of("measure", "measure-timestamp"),
            Set.of(PressureChannelHelper.class, NoiseChannelHelper.class, HumidityChannelHelper.class,
                    TemperatureChannelHelper.class, Co2ChannelHelper.class, DeviceChannelHelper.class,
                    MeasuresChannelHelper.class),
            List.of(GROUP_TEMPERATURE, GROUP_HUMIDITY, GROUP_CO2, GROUP_NOISE, GROUP_PRESSURE, GROUP_DEVICE,
                    GROUP_SIGNAL)),
    NAModule1(NetatmoDeviceHandler.class, RefreshPolicy.PARENT, NAMain, List.of("measure", "measure-timestamp"),
            Set.of(HumidityChannelHelper.class, TemperatureChannelHelper.class, BatteryHelper.class,
                    ModuleChannelHelper.class, MeasuresChannelHelper.class),
            List.of(GROUP_TEMPERATURE, GROUP_HUMIDITY, GROUP_MODULE, GROUP_SIGNAL, GROUP_BATTERY)),
    NAModule2(NetatmoDeviceHandler.class, RefreshPolicy.PARENT, NAMain, null,
            Set.of(WindChannelHelper.class, BatteryHelper.class, ModuleChannelHelper.class),
            List.of(GROUP_WIND, GROUP_MODULE, GROUP_SIGNAL, GROUP_BATTERY)),
    NAModule3(NetatmoDeviceHandler.class, RefreshPolicy.PARENT, NAMain, List.of("sum-rain"),
            Set.of(RainChannelHelper.class, BatteryHelper.class, ModuleChannelHelper.class,
                    MeasuresChannelHelper.class),
            List.of(GROUP_RAIN, GROUP_MODULE, GROUP_SIGNAL, GROUP_BATTERY)),
    NAModule4(NetatmoDeviceHandler.class, RefreshPolicy.PARENT, NAMain, List.of("measure", "measure-timestamp"),
            Set.of(HumidityChannelHelper.class, TemperatureChannelHelper.class, Co2ChannelHelper.class,
                    BatteryHelper.class, ModuleChannelHelper.class, MeasuresChannelHelper.class),
            List.of(GROUP_TEMPERATURE, GROUP_HUMIDITY, GROUP_CO2, GROUP_MODULE, GROUP_SIGNAL, GROUP_BATTERY)),

    // Aircare group
    NHC(HomeCoachHandler.class, RefreshPolicy.AUTO, null, List.of("measure", "measure-timestamp"),
            Set.of(NoiseChannelHelper.class, HumidityChannelHelper.class, PressureChannelHelper.class,
                    TemperatureChannelHelper.class, Co2ChannelHelper.class, HomeCoachChannelHelper.class,
                    DeviceChannelHelper.class, MeasuresChannelHelper.class),
            List.of(GROUP_HEALTH, GROUP_TEMPERATURE, GROUP_HUMIDITY, GROUP_PRESSURE, GROUP_CO2, GROUP_NOISE,
                    GROUP_DEVICE, GROUP_SIGNAL)),

    // Energy group
    NAHomeEnergy(HomeEnergyHandler.class, RefreshPolicy.CONFIG, null, null, Set.of(HomeEnergyChannelHelper.class),
            List.of(GROUP_HOME_ENERGY)),
    NAPlug(PlugHandler.class, RefreshPolicy.CONFIG, NAHomeEnergy, null,
            Set.of(PlugChannelHelper.class, DeviceChannelHelper.class),
            List.of(GROUP_PLUG, GROUP_DEVICE, GROUP_SIGNAL)),
    NATherm1(Therm1Handler.class, RefreshPolicy.PARENT, NAPlug, null,
            Set.of(Therm1PropsChannelHelper.class, Therm1SetpointChannelHelper.class, Therm1TempChannelHelper.class,
                    BatteryHelper.class, ModuleChannelHelper.class),
            List.of(GROUP_TH_PROPERTIES, GROUP_TH_SETPOINT, GROUP_TH_TEMPERATURE, GROUP_MODULE, GROUP_SIGNAL,
                    GROUP_BATTERY)),

    // Left for future implementation
    // NACamDoorTag,
    // NSD,
    // NIS,
    // NDB
    ;

    public enum RefreshPolicy {
        AUTO,
        PARENT,
        CONFIG;
    }

    public final List<String> groups;
    public final @Nullable List<String> extensions;
    public RefreshPolicy refreshPeriod;
    public final @Nullable ThingTypeUID bridgeThingType;
    public final ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, this.name());
    public final Class<?> handlerClass;
    public final Set<Class<? extends AbstractChannelHelper>> channelHelpers;

    ModuleType(Class<?> handlerClass, RefreshPolicy refreshPeriod, @Nullable ModuleType bridge,
            @Nullable List<String> extensions, Set<Class<? extends AbstractChannelHelper>> setOfHelpers,
            List<String> groups) {
        this.handlerClass = handlerClass;
        this.groups = groups;
        this.refreshPeriod = refreshPeriod;
        this.extensions = extensions;
        this.refreshPeriod = refreshPeriod;
        this.channelHelpers = setOfHelpers;
        this.bridgeThingType = bridge != null ? bridge.thingTypeUID : null;
    }

    public boolean matches(ThingTypeUID otherThingTypeUID) {
        return thingTypeUID.equals(otherThingTypeUID);
    }

    public int[] getSignalLevels() {
        return groups.contains(GROUP_SIGNAL)
                ? (groups.contains(GROUP_BATTERY) ? RADIO_SIGNAL_LEVELS : WIFI_SIGNAL_LEVELS)
                : NO_RADIO;
    }
}
