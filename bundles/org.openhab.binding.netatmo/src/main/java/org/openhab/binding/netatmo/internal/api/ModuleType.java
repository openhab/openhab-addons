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

import java.lang.reflect.Constructor;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.dto.NAModule;
import org.openhab.binding.netatmo.internal.api.dto.NAPlug;
import org.openhab.binding.netatmo.internal.api.dto.NARoom;
import org.openhab.binding.netatmo.internal.api.dto.NAThermostat;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.dto.NAWelcome;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.BatteryHelper;
import org.openhab.binding.netatmo.internal.channelhelper.CameraChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.Co2ChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.DeviceChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HomeCoachChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HomeEnergyChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HomeSecurityChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HumidityChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.LocationChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.MeasuresChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.NoiseChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PersonChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PlugChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PresenceChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PressureChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.RainChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.RoomChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.RoomSetpointChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.RoomTempChannelHelper;
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
import org.openhab.binding.netatmo.internal.handler.NRVHandler;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.handler.PersonHandler;
import org.openhab.binding.netatmo.internal.handler.PlugHandler;
import org.openhab.binding.netatmo.internal.handler.PresenceHandler;
import org.openhab.binding.netatmo.internal.handler.RoomHandler;
import org.openhab.binding.netatmo.internal.handler.Therm1Handler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;

/**
 * This enum all handled Netatmo modules and devices along with their capabilities
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum ModuleType {
    UNKNOWN(null, RefreshPolicy.NONE, null, List.of(), List.of(), List.of(), null),

    // Security Group
    NAHomeSecurity(HomeSecurityHandler.class, RefreshPolicy.CONFIG, null, List.of(),
            List.of(HomeSecurityChannelHelper.class), List.of(GROUP_HOME_SECURITY), null),
    NAPerson(PersonHandler.class, RefreshPolicy.PARENT, NAHomeSecurity, List.of(), List.of(PersonChannelHelper.class),
            List.of(GROUP_PERSON, GROUP_PERSON_EVENT), null),
    NACamera(CameraHandler.class, RefreshPolicy.PARENT, NAHomeSecurity, List.of(), List.of(CameraChannelHelper.class),
            List.of(GROUP_WELCOME, GROUP_WELCOME_EVENT), NAWelcome.class),
    NOC(PresenceHandler.class, RefreshPolicy.PARENT, NAHomeSecurity, List.of(),
            List.of(CameraChannelHelper.class, PresenceChannelHelper.class),
            List.of(GROUP_WELCOME, GROUP_WELCOME_EVENT, GROUP_PRESENCE), NAWelcome.class),

    // Weather group
    NAHomeWeather(null, RefreshPolicy.NONE, null, List.of(), List.of(), List.of(), null),
    NAMain(MainHandler.class, RefreshPolicy.AUTO, null, List.of("measure", "measure-timestamp"),
            List.of(PressureChannelHelper.class, NoiseChannelHelper.class, HumidityChannelHelper.class,
                    TemperatureChannelHelper.class, Co2ChannelHelper.class, DeviceChannelHelper.class,
                    MeasuresChannelHelper.class, LocationChannelHelper.class),
            List.of(GROUP_TEMPERATURE, GROUP_HUMIDITY, GROUP_CO2, GROUP_NOISE, GROUP_PRESSURE, GROUP_DEVICE,
                    GROUP_SIGNAL, GROUP_LOCATION),
            NAThing.class),
    NAModule1(NetatmoDeviceHandler.class, RefreshPolicy.PARENT, NAMain, List.of("measure", "measure-timestamp"),
            List.of(HumidityChannelHelper.class, TemperatureChannelHelper.class, BatteryHelper.class,
                    MeasuresChannelHelper.class),
            List.of(GROUP_TEMPERATURE, GROUP_HUMIDITY, GROUP_SIGNAL, GROUP_BATTERY), NAModule.class),
    NAModule2(NetatmoDeviceHandler.class, RefreshPolicy.PARENT, NAMain, List.of(),
            List.of(WindChannelHelper.class, BatteryHelper.class), List.of(GROUP_WIND, GROUP_SIGNAL, GROUP_BATTERY),
            NAModule.class),
    NAModule3(NetatmoDeviceHandler.class, RefreshPolicy.PARENT, NAMain, List.of("sum-rain"),
            List.of(RainChannelHelper.class, BatteryHelper.class, MeasuresChannelHelper.class),
            List.of(GROUP_RAIN, GROUP_SIGNAL, GROUP_BATTERY), NAModule.class),
    NAModule4(NetatmoDeviceHandler.class, RefreshPolicy.PARENT, NAMain, List.of("measure", "measure-timestamp"),
            List.of(HumidityChannelHelper.class, TemperatureChannelHelper.class, Co2ChannelHelper.class,
                    BatteryHelper.class, MeasuresChannelHelper.class),
            List.of(GROUP_TEMPERATURE, GROUP_HUMIDITY, GROUP_CO2, GROUP_SIGNAL, GROUP_BATTERY), NAModule.class),

    // Aircare group
    NHC(HomeCoachHandler.class, RefreshPolicy.AUTO, null, List.of("measure", "measure-timestamp"),
            List.of(NoiseChannelHelper.class, HumidityChannelHelper.class, PressureChannelHelper.class,
                    TemperatureChannelHelper.class, Co2ChannelHelper.class, HomeCoachChannelHelper.class,
                    DeviceChannelHelper.class, MeasuresChannelHelper.class),
            List.of(GROUP_HEALTH, GROUP_TEMPERATURE, GROUP_HUMIDITY, GROUP_PRESSURE, GROUP_CO2, GROUP_NOISE,
                    GROUP_DEVICE, GROUP_SIGNAL),
            NAThing.class),

    // Energy group
    NAHomeEnergy(HomeEnergyHandler.class, RefreshPolicy.AUTO, null, List.of(), List.of(HomeEnergyChannelHelper.class),
            List.of(GROUP_HOME_ENERGY), null),
    NAPlug(PlugHandler.class, RefreshPolicy.CONFIG, NAHomeEnergy, List.of(),
            List.of(PlugChannelHelper.class, DeviceChannelHelper.class),
            List.of(GROUP_PLUG, GROUP_DEVICE, GROUP_SIGNAL), NAPlug.class),
    NATherm1(Therm1Handler.class, RefreshPolicy.PARENT, NAPlug, List.of(),
            List.of(Therm1PropsChannelHelper.class, Therm1SetpointChannelHelper.class, Therm1TempChannelHelper.class,
                    BatteryHelper.class),
            List.of(GROUP_TH_PROPERTIES, GROUP_TH_SETPOINT, GROUP_TH_TEMPERATURE, GROUP_SIGNAL, GROUP_BATTERY),
            NAThermostat.class),
    NARoom(RoomHandler.class, RefreshPolicy.CONFIG, NAHomeEnergy, List.of(),
            List.of(RoomChannelHelper.class, RoomTempChannelHelper.class, RoomSetpointChannelHelper.class),
            List.of(GROUP_ROOM_PROPERTIES, GROUP_TH_SETPOINT, GROUP_ROOM_TEMPERATURE), NARoom.class),
    NRV(NRVHandler.class, RefreshPolicy.CONFIG, NAHomeEnergy, List.of(), List.of(BatteryHelper.class),
            List.of(GROUP_ENERGY_BATTERY, GROUP_SIGNAL), NAPlug.class),
    // Left for future implementation
    // NACamDoorTag : self explaining
    // NSD : smoke detector
    // NIS : indoor siren
    // NDB : doobell
    ;

    public enum RefreshPolicy {
        AUTO,
        PARENT,
        CONFIG,
        NONE;
    }

    public static final EnumSet<ModuleType> asSet = EnumSet.allOf(ModuleType.class);

    private final List<String> groups;
    private final List<String> extensions;
    private final RefreshPolicy refreshPeriod;
    private final @Nullable ThingTypeUID bridgeThingType;
    private final @Nullable Class<?> handlerClass;
    private final @Nullable Class<?> dto;
    private final ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, this.name());
    private final List<Class<? extends AbstractChannelHelper>> channelHelpers;

    ModuleType(@Nullable Class<?> handlerClass, RefreshPolicy refreshPeriod, @Nullable ModuleType bridge,
            List<String> extensions, List<Class<? extends AbstractChannelHelper>> setOfHelpers, List<String> groups,
            @Nullable Class<?> dto) {
        this.handlerClass = handlerClass;
        this.groups = groups;
        this.refreshPeriod = refreshPeriod;
        this.extensions = extensions;
        this.channelHelpers = setOfHelpers;
        this.bridgeThingType = bridge != null ? bridge.thingTypeUID : null;
        this.dto = dto;
    }

    public boolean matches(ThingTypeUID otherThingTypeUID) {
        return thingTypeUID.equals(otherThingTypeUID);
    }

    public int[] getSignalLevels() {
        return groups.contains(GROUP_SIGNAL)
                ? (groups.contains(GROUP_BATTERY) ? RADIO_SIGNAL_LEVELS : WIFI_SIGNAL_LEVELS)
                : NO_RADIO;
    }

    public List<String> getGroups() {
        return groups;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public RefreshPolicy getRefreshPeriod() {
        return refreshPeriod;
    }

    public @Nullable ThingTypeUID getBridgeThingType() {
        return bridgeThingType;
    }

    public @Nullable Constructor<?> getHandlerConstructor() throws NoSuchMethodException, SecurityException {
        return handlerClass != null
                ? handlerClass.getConstructor(Bridge.class, List.class, ApiBridge.class,
                        NetatmoDescriptionProvider.class)
                : null;
    }

    public List<Class<? extends AbstractChannelHelper>> getChannelHelpers() {
        return channelHelpers;
    }

    public @Nullable Class<?> getDto() {
        return dto;
    }

    public ThingTypeUID getThingTypeUID() {
        return thingTypeUID;
    }

    public static Boolean isModuleTypeImplemented(String name) {
        return Stream.of(values()).anyMatch(mt -> mt.toString().equals(name));
        // if (name.isBlank()) {
        // return false;
        // }
        // for (ModuleType item : values()) {
        // if (name.equals(item.toString())) {
        // return true;
        // }
        // }
        // return false;
    }

    // public static Stream<ModuleType> asStream() {
    // return Stream.of(values());
    // }
}
