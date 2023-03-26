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
package org.openhab.binding.netatmo.internal.api.data;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.*;

import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.handler.capability.AirCareCapability;
import org.openhab.binding.netatmo.internal.handler.capability.AlarmEventCapability;
import org.openhab.binding.netatmo.internal.handler.capability.CameraCapability;
import org.openhab.binding.netatmo.internal.handler.capability.Capability;
import org.openhab.binding.netatmo.internal.handler.capability.ChannelHelperCapability;
import org.openhab.binding.netatmo.internal.handler.capability.DeviceCapability;
import org.openhab.binding.netatmo.internal.handler.capability.DoorbellCapability;
import org.openhab.binding.netatmo.internal.handler.capability.HomeCapability;
import org.openhab.binding.netatmo.internal.handler.capability.MeasureCapability;
import org.openhab.binding.netatmo.internal.handler.capability.PersonCapability;
import org.openhab.binding.netatmo.internal.handler.capability.PresenceCapability;
import org.openhab.binding.netatmo.internal.handler.capability.RoomCapability;
import org.openhab.binding.netatmo.internal.handler.capability.WeatherCapability;
import org.openhab.binding.netatmo.internal.handler.channelhelper.AirQualityChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ApiBridgeChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.CameraChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.DoorTagChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.EnergyChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.EventCameraChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.EventPersonChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.PersonChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.PresenceChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.PressureChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.RainChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.RoomChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.SecurityChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.SetpointChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.SirenChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.Therm1ChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.WindChannelHelper;
import org.openhab.core.thing.ThingTypeUID;

/**
 * This enum describes all Netatmo modules and devices along with their capabilities.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum ModuleType {
    UNKNOWN(FeatureArea.NONE, "", null, Set.of()),

    ACCOUNT(FeatureArea.NONE, "", null, Set.of(), new ChannelGroup(ApiBridgeChannelHelper.class, GROUP_MONITORING)),

    HOME(FeatureArea.NONE, "NAHome", ACCOUNT,
            Set.of(DeviceCapability.class, HomeCapability.class, ChannelHelperCapability.class),
            new ChannelGroup(SecurityChannelHelper.class, GROUP_SECURITY_EVENT, GROUP_SECURITY),
            new ChannelGroup(EnergyChannelHelper.class, GROUP_ENERGY)),

    PERSON(FeatureArea.SECURITY, "NAPerson", HOME, Set.of(PersonCapability.class, ChannelHelperCapability.class),
            new ChannelGroup(PersonChannelHelper.class, GROUP_PERSON),
            new ChannelGroup(EventPersonChannelHelper.class, GROUP_PERSON_LAST_EVENT)),

    WELCOME(FeatureArea.SECURITY, "NACamera", HOME, Set.of(CameraCapability.class, ChannelHelperCapability.class),
            ChannelGroup.SIGNAL, ChannelGroup.EVENT,
            new ChannelGroup(CameraChannelHelper.class, GROUP_SECURITY_EVENT, GROUP_CAM_STATUS, GROUP_CAM_LIVE)),

    TAG(FeatureArea.SECURITY, "NACamDoorTag", WELCOME, Set.of(ChannelHelperCapability.class), ChannelGroup.SIGNAL,
            ChannelGroup.BATTERY, ChannelGroup.TIMESTAMP, new ChannelGroup(DoorTagChannelHelper.class, GROUP_TAG)),

    SIREN(FeatureArea.SECURITY, "NIS", WELCOME, Set.of(ChannelHelperCapability.class), ChannelGroup.SIGNAL,
            ChannelGroup.BATTERY, ChannelGroup.TIMESTAMP, new ChannelGroup(SirenChannelHelper.class, GROUP_SIREN)),

    PRESENCE(FeatureArea.SECURITY, "NOC", HOME, Set.of(PresenceCapability.class, ChannelHelperCapability.class),
            ChannelGroup.SIGNAL, ChannelGroup.EVENT,
            new ChannelGroup(PresenceChannelHelper.class, GROUP_SECURITY_EVENT, GROUP_CAM_STATUS, GROUP_CAM_LIVE,
                    GROUP_PRESENCE),
            new ChannelGroup(EventCameraChannelHelper.class, GROUP_SUB_EVENT)),

    DOORBELL(FeatureArea.SECURITY, "NDB", HOME, Set.of(DoorbellCapability.class, ChannelHelperCapability.class),
            ChannelGroup.SIGNAL,
            new ChannelGroup(CameraChannelHelper.class, GROUP_SECURITY_EVENT, GROUP_DOORBELL_STATUS,
                    GROUP_DOORBELL_LIVE),
            new ChannelGroup(EventCameraChannelHelper.class, GROUP_DOORBELL_LAST_EVENT, GROUP_DOORBELL_SUB_EVENT)),

    WEATHER_STATION(FeatureArea.WEATHER, "NAMain", ACCOUNT,
            Set.of(DeviceCapability.class, WeatherCapability.class, MeasureCapability.class,
                    ChannelHelperCapability.class),
            ChannelGroup.SIGNAL, ChannelGroup.HUMIDITY, ChannelGroup.TSTAMP_EXT, ChannelGroup.MEASURE,
            ChannelGroup.AIR_QUALITY, ChannelGroup.LOCATION, ChannelGroup.NOISE, ChannelGroup.TEMP_INSIDE_EXT,
            new ChannelGroup(PressureChannelHelper.class, MeasureClass.PRESSURE, GROUP_TYPE_PRESSURE_EXTENDED)),

    OUTDOOR(FeatureArea.WEATHER, "NAModule1", WEATHER_STATION,
            Set.of(MeasureCapability.class, ChannelHelperCapability.class), ChannelGroup.SIGNAL, ChannelGroup.HUMIDITY,
            ChannelGroup.TSTAMP_EXT, ChannelGroup.MEASURE, ChannelGroup.BATTERY, ChannelGroup.TEMP_OUTSIDE_EXT),

    WIND(FeatureArea.WEATHER, "NAModule2", WEATHER_STATION, Set.of(ChannelHelperCapability.class), ChannelGroup.SIGNAL,
            ChannelGroup.TSTAMP_EXT, ChannelGroup.BATTERY, new ChannelGroup(WindChannelHelper.class, GROUP_WIND)),

    RAIN(FeatureArea.WEATHER, "NAModule3", WEATHER_STATION,
            Set.of(MeasureCapability.class, ChannelHelperCapability.class), ChannelGroup.SIGNAL,
            ChannelGroup.TSTAMP_EXT, ChannelGroup.MEASURE, ChannelGroup.BATTERY,
            new ChannelGroup(RainChannelHelper.class, MeasureClass.RAIN_QUANTITY, GROUP_RAIN)),

    INDOOR(FeatureArea.WEATHER, "NAModule4", WEATHER_STATION,
            Set.of(MeasureCapability.class, ChannelHelperCapability.class), ChannelGroup.SIGNAL,
            ChannelGroup.TSTAMP_EXT, ChannelGroup.MEASURE, ChannelGroup.BATTERY, ChannelGroup.HUMIDITY,
            ChannelGroup.TEMP_INSIDE_EXT, ChannelGroup.AIR_QUALITY),

    HOME_COACH(FeatureArea.AIR_CARE, "NHC", ACCOUNT,
            Set.of(DeviceCapability.class, AirCareCapability.class, MeasureCapability.class,
                    ChannelHelperCapability.class),
            ChannelGroup.LOCATION, ChannelGroup.SIGNAL, ChannelGroup.NOISE, ChannelGroup.HUMIDITY,
            ChannelGroup.TEMP_INSIDE, ChannelGroup.MEASURE, ChannelGroup.TSTAMP_EXT,
            new ChannelGroup(AirQualityChannelHelper.class, GROUP_TYPE_AIR_QUALITY_EXTENDED),
            new ChannelGroup(PressureChannelHelper.class, MeasureClass.PRESSURE, GROUP_PRESSURE)),

    PLUG(FeatureArea.ENERGY, "NAPlug", HOME, Set.of(ChannelHelperCapability.class), ChannelGroup.SIGNAL),

    VALVE(FeatureArea.ENERGY, "NRV", PLUG, Set.of(ChannelHelperCapability.class), ChannelGroup.SIGNAL,
            ChannelGroup.BATTERY_EXT),

    THERMOSTAT(FeatureArea.ENERGY, "NATherm1", PLUG, Set.of(ChannelHelperCapability.class), ChannelGroup.SIGNAL,
            ChannelGroup.BATTERY_EXT, new ChannelGroup(Therm1ChannelHelper.class, GROUP_TYPE_TH_PROPERTIES)),

    ROOM(FeatureArea.ENERGY, "NARoom", HOME, Set.of(RoomCapability.class, ChannelHelperCapability.class),
            new ChannelGroup(RoomChannelHelper.class, GROUP_TYPE_ROOM_PROPERTIES, GROUP_TYPE_ROOM_TEMPERATURE),
            new ChannelGroup(SetpointChannelHelper.class, GROUP_SETPOINT)),

    SMOKE_DETECTOR(FeatureArea.SECURITY, "NSD", HOME, Set.of(AlarmEventCapability.class, ChannelHelperCapability.class),
            ChannelGroup.SIGNAL, ChannelGroup.TIMESTAMP, ChannelGroup.ALARM_LAST_EVENT),

    CO_DETECTOR(FeatureArea.SECURITY, "NCO", HOME, Set.of(AlarmEventCapability.class, ChannelHelperCapability.class),
            ChannelGroup.SIGNAL, ChannelGroup.TIMESTAMP, ChannelGroup.ALARM_LAST_EVENT);

    public static final EnumSet<ModuleType> AS_SET = EnumSet.allOf(ModuleType.class);

    private final Optional<ModuleType> bridgeType;
    public final Set<ChannelGroup> channelGroups;
    public final Set<Class<? extends Capability>> capabilities;
    public final ThingTypeUID thingTypeUID;
    public final FeatureArea feature;
    public final String apiName;

    ModuleType(FeatureArea feature, String apiName, @Nullable ModuleType bridge,
            Set<Class<? extends Capability>> capabilities, ChannelGroup... channelGroups) {
        this.bridgeType = Optional.ofNullable(bridge);
        this.feature = feature;
        this.capabilities = capabilities;
        this.apiName = apiName;
        this.channelGroups = Set.of(channelGroups);
        this.thingTypeUID = new ThingTypeUID(BINDING_ID, name().toLowerCase().replace("_", "-"));
    }

    public boolean isLogical() {
        return !channelGroups.contains(ChannelGroup.SIGNAL);
    }

    public boolean isABridge() { // I am a bridge if any module references me as being so
        return AS_SET.stream().anyMatch(mt -> this.equals(mt.getBridge()));
    }

    public List<String> getExtensions() {
        return channelGroups.stream().map(cg -> cg.extensions).flatMap(Set::stream).toList();
    }

    public List<String> getGroupTypes() {
        return channelGroups.stream().map(cg -> cg.groupTypes).flatMap(Set::stream).toList();
    }

    public int[] getSignalLevels() {
        if (!isLogical()) {
            return (channelGroups.contains(ChannelGroup.BATTERY) || channelGroups.contains(ChannelGroup.BATTERY_EXT))
                    ? RADIO_SIGNAL_LEVELS
                    : WIFI_SIGNAL_LEVELS;
        }
        throw new IllegalArgumentException(
                "getSignalLevels should not be called for module type : '%s', please file a bug report."
                        .formatted(name()));
    }

    public ModuleType getBridge() {
        return bridgeType.orElse(UNKNOWN);
    }

    public URI getConfigDescription() {
        return URI.create(BINDING_ID + ":"
                + (equals(ACCOUNT) ? "api_bridge"
                        : equals(HOME) ? "home"
                                : (isLogical() ? "virtual" : UNKNOWN.equals(getBridge()) ? "configurable" : "device")));
    }

    public int getDepth() {
        ModuleType parent = getBridge();
        return parent == UNKNOWN ? 1 : parent.getDepth() + 1;
    }

    public static ModuleType from(ThingTypeUID thingTypeUID) {
        return AS_SET.stream().filter(mt -> mt.thingTypeUID.equals(thingTypeUID)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No known ModuleType matched '%s'".formatted(thingTypeUID.toString())));
    }
}
