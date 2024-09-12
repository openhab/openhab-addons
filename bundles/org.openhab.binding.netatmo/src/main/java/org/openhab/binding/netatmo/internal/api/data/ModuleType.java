/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.Objects;
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
import org.openhab.binding.netatmo.internal.handler.capability.ParentUpdateCapability;
import org.openhab.binding.netatmo.internal.handler.capability.PersonCapability;
import org.openhab.binding.netatmo.internal.handler.capability.PresenceCapability;
import org.openhab.binding.netatmo.internal.handler.capability.RefreshAutoCapability;
import org.openhab.binding.netatmo.internal.handler.capability.RefreshCapability;
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
    UNKNOWN(FeatureArea.NONE, "", 1, "virtual", null, Set.of()),

    ACCOUNT(FeatureArea.NONE, "", 1, "api_bridge", null, Set.of(),
            new ChannelGroup(ApiBridgeChannelHelper.class, GROUP_MONITORING)),

    HOME(FeatureArea.NONE, "NAHome", 1, "home", ACCOUNT,
            Set.of(DeviceCapability.class, HomeCapability.class, ChannelHelperCapability.class,
                    RefreshCapability.class),
            new ChannelGroup(SecurityChannelHelper.class, GROUP_SECURITY_EVENT, GROUP_SECURITY),
            new ChannelGroup(EnergyChannelHelper.class, GROUP_ENERGY)),

    PERSON(FeatureArea.SECURITY, "NAPerson", 1, "virtual", HOME,
            Set.of(PersonCapability.class, ChannelHelperCapability.class, ParentUpdateCapability.class),
            new ChannelGroup(PersonChannelHelper.class, GROUP_PERSON),
            new ChannelGroup(EventPersonChannelHelper.class, GROUP_PERSON_LAST_EVENT)),

    WELCOME(FeatureArea.SECURITY, "NACamera", 1, "camera", HOME,
            Set.of(CameraCapability.class, ChannelHelperCapability.class, ParentUpdateCapability.class),
            ChannelGroup.SIGNAL, ChannelGroup.EVENT,
            new ChannelGroup(CameraChannelHelper.class, GROUP_SECURITY_EVENT, GROUP_CAM_STATUS, GROUP_CAM_LIVE)),

    TAG(FeatureArea.SECURITY, "NACamDoorTag", 1, "device", WELCOME,
            Set.of(ChannelHelperCapability.class, ParentUpdateCapability.class), ChannelGroup.SIGNAL,
            ChannelGroup.BATTERY, ChannelGroup.TIMESTAMP, new ChannelGroup(DoorTagChannelHelper.class, GROUP_TAG)),

    SIREN(FeatureArea.SECURITY, "NIS", 1, "device", WELCOME,
            Set.of(ChannelHelperCapability.class, ParentUpdateCapability.class), ChannelGroup.SIGNAL,
            ChannelGroup.BATTERY, ChannelGroup.TIMESTAMP, new ChannelGroup(SirenChannelHelper.class, GROUP_SIREN)),

    PRESENCE(FeatureArea.SECURITY, "NOC", 2, "camera", HOME,
            Set.of(PresenceCapability.class, ChannelHelperCapability.class, ParentUpdateCapability.class),
            ChannelGroup.SIGNAL, ChannelGroup.EVENT,
            new ChannelGroup(PresenceChannelHelper.class, GROUP_SECURITY_EVENT, GROUP_CAM_STATUS, GROUP_CAM_LIVE,
                    GROUP_PRESENCE),
            new ChannelGroup(EventCameraChannelHelper.class, GROUP_SUB_EVENT)),

    DOORBELL(FeatureArea.SECURITY, "NDB", 1, "camera", HOME,
            Set.of(DoorbellCapability.class, ChannelHelperCapability.class, ParentUpdateCapability.class),
            ChannelGroup.SIGNAL,
            new ChannelGroup(CameraChannelHelper.class, GROUP_SECURITY_EVENT, GROUP_DOORBELL_STATUS,
                    GROUP_DOORBELL_LIVE),
            new ChannelGroup(EventCameraChannelHelper.class, GROUP_DOORBELL_LAST_EVENT, GROUP_DOORBELL_SUB_EVENT)),

    WEATHER_STATION(FeatureArea.WEATHER, "NAMain", 1, "weather", ACCOUNT,
            Set.of(DeviceCapability.class, WeatherCapability.class, MeasureCapability.class,
                    ChannelHelperCapability.class, RefreshAutoCapability.class),
            ChannelGroup.SIGNAL, ChannelGroup.HUMIDITY, ChannelGroup.TSTAMP_EXT, ChannelGroup.MEASURE,
            ChannelGroup.AIR_QUALITY, ChannelGroup.LOCATION, ChannelGroup.NOISE, ChannelGroup.TEMP_INSIDE_EXT,
            new ChannelGroup(PressureChannelHelper.class, MeasureClass.PRESSURE, GROUP_TYPE_PRESSURE_EXTENDED)),

    OUTDOOR(FeatureArea.WEATHER, "NAModule1", 1, "device", WEATHER_STATION,
            Set.of(MeasureCapability.class, ChannelHelperCapability.class, ParentUpdateCapability.class),
            ChannelGroup.SIGNAL, ChannelGroup.HUMIDITY, ChannelGroup.TSTAMP_EXT, ChannelGroup.MEASURE,
            ChannelGroup.BATTERY, ChannelGroup.TEMP_OUTSIDE_EXT),

    WIND(FeatureArea.WEATHER, "NAModule2", 1, "device", WEATHER_STATION,
            Set.of(ChannelHelperCapability.class, ParentUpdateCapability.class), ChannelGroup.SIGNAL,
            ChannelGroup.TSTAMP_EXT, ChannelGroup.BATTERY, new ChannelGroup(WindChannelHelper.class, GROUP_WIND)),

    RAIN(FeatureArea.WEATHER, "NAModule3", 1, "device", WEATHER_STATION,
            Set.of(MeasureCapability.class, ChannelHelperCapability.class, ParentUpdateCapability.class),
            ChannelGroup.SIGNAL, ChannelGroup.TSTAMP_EXT, ChannelGroup.MEASURE, ChannelGroup.BATTERY,
            new ChannelGroup(RainChannelHelper.class, MeasureClass.RAIN_QUANTITY, GROUP_RAIN)),

    INDOOR(FeatureArea.WEATHER, "NAModule4", 1, "device", WEATHER_STATION,
            Set.of(MeasureCapability.class, ChannelHelperCapability.class, ParentUpdateCapability.class),
            ChannelGroup.SIGNAL, ChannelGroup.TSTAMP_EXT, ChannelGroup.MEASURE, ChannelGroup.BATTERY,
            ChannelGroup.HUMIDITY, ChannelGroup.TEMP_INSIDE_EXT, ChannelGroup.AIR_QUALITY),

    HOME_COACH(FeatureArea.AIR_CARE, "NHC", 1, "weather", ACCOUNT,
            Set.of(DeviceCapability.class, AirCareCapability.class, MeasureCapability.class,
                    ChannelHelperCapability.class, RefreshAutoCapability.class),
            ChannelGroup.LOCATION, ChannelGroup.SIGNAL, ChannelGroup.NOISE, ChannelGroup.HUMIDITY,
            ChannelGroup.TEMP_INSIDE, ChannelGroup.MEASURE, ChannelGroup.TSTAMP_EXT,
            new ChannelGroup(AirQualityChannelHelper.class, GROUP_TYPE_AIR_QUALITY_EXTENDED),
            new ChannelGroup(PressureChannelHelper.class, MeasureClass.PRESSURE, GROUP_PRESSURE)),

    PLUG(FeatureArea.ENERGY, "NAPlug", 1, "device", HOME,
            Set.of(ChannelHelperCapability.class, ParentUpdateCapability.class), ChannelGroup.SIGNAL),

    VALVE(FeatureArea.ENERGY, "NRV", 1, "device", PLUG,
            Set.of(ChannelHelperCapability.class, ParentUpdateCapability.class), ChannelGroup.SIGNAL,
            ChannelGroup.BATTERY_EXT),

    THERMOSTAT(FeatureArea.ENERGY, "NATherm1", 1, "device", PLUG,
            Set.of(ChannelHelperCapability.class, ParentUpdateCapability.class), ChannelGroup.SIGNAL,
            ChannelGroup.BATTERY_EXT, new ChannelGroup(Therm1ChannelHelper.class, GROUP_TYPE_TH_PROPERTIES)),

    ROOM(FeatureArea.ENERGY, "NARoom", 1, "virtual", HOME,
            Set.of(RoomCapability.class, ChannelHelperCapability.class, ParentUpdateCapability.class),
            new ChannelGroup(RoomChannelHelper.class, GROUP_TYPE_ROOM_PROPERTIES, GROUP_TYPE_ROOM_TEMPERATURE),
            new ChannelGroup(SetpointChannelHelper.class, GROUP_SETPOINT)),

    SMOKE_DETECTOR(FeatureArea.SECURITY, "NSD", 1, "device", HOME,
            Set.of(AlarmEventCapability.class, ChannelHelperCapability.class, ParentUpdateCapability.class),
            ChannelGroup.SIGNAL, ChannelGroup.TIMESTAMP, ChannelGroup.ALARM_LAST_EVENT),

    CO_DETECTOR(FeatureArea.SECURITY, "NCO", 1, "device", HOME,
            Set.of(AlarmEventCapability.class, ChannelHelperCapability.class, ParentUpdateCapability.class),
            ChannelGroup.SIGNAL, ChannelGroup.TIMESTAMP, ChannelGroup.ALARM_LAST_EVENT);

    public static final EnumSet<ModuleType> AS_SET = EnumSet.allOf(ModuleType.class);

    private final @Nullable ModuleType bridgeType;
    public final Set<ChannelGroup> channelGroups;
    public final Set<Class<? extends Capability>> capabilities;
    public final ThingTypeUID thingTypeUID;
    public final FeatureArea feature;
    public final String apiName;
    public final String thingTypeVersion;
    public final URI configDescription;

    ModuleType(FeatureArea feature, String apiName, int thingTypeVersion, String config, @Nullable ModuleType bridge,
            Set<Class<? extends Capability>> capabilities, ChannelGroup... channelGroups) {
        this.bridgeType = bridge;
        this.feature = feature;
        this.capabilities = capabilities;
        this.apiName = apiName;
        this.channelGroups = Set.of(channelGroups);
        this.thingTypeUID = new ThingTypeUID(BINDING_ID, name().toLowerCase().replace("_", "-"));
        this.thingTypeVersion = Integer.toString(thingTypeVersion);
        this.configDescription = URI.create(BINDING_ID + ":" + config);
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
                "getSignalLevels should not be called for module type: '%s', please file a bug report."
                        .formatted(name()));
    }

    public ModuleType getBridge() {
        return Objects.requireNonNullElse(this.bridgeType, UNKNOWN);
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
