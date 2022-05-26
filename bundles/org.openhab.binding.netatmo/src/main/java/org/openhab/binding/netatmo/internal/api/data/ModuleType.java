/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.BINDING_ID;
import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.*;

import java.net.URI;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.handler.capability.AirCareCapability;
import org.openhab.binding.netatmo.internal.handler.capability.CameraCapability;
import org.openhab.binding.netatmo.internal.handler.capability.Capability;
import org.openhab.binding.netatmo.internal.handler.capability.ChannelHelperCapability;
import org.openhab.binding.netatmo.internal.handler.capability.DeviceCapability;
import org.openhab.binding.netatmo.internal.handler.capability.EventCapability;
import org.openhab.binding.netatmo.internal.handler.capability.HomeCapability;
import org.openhab.binding.netatmo.internal.handler.capability.MeasureCapability;
import org.openhab.binding.netatmo.internal.handler.capability.PersonCapability;
import org.openhab.binding.netatmo.internal.handler.capability.PresenceCapability;
import org.openhab.binding.netatmo.internal.handler.capability.RoomCapability;
import org.openhab.binding.netatmo.internal.handler.capability.WeatherCapability;
import org.openhab.binding.netatmo.internal.handler.channelhelper.AirQualityChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.AirQualityExtChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.BatteryChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.BatteryExtChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.CameraChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.DoorbellChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.EventChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.EventDoorbellChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.EventPersonChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.HomeEnergyChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.HomeSecurityChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.HumidityChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.LocationChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.MeasuresChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.NoiseChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.PersonChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.PresenceChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.PressureChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.PressureExtChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.RainChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.RoomChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.SetpointChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.SignalChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.SirenChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.TemperatureChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.TemperatureExtChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.TemperatureOutChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.Therm1ChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.TimestampChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.TimestampExtChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.WindChannelHelper;
import org.openhab.core.thing.ThingTypeUID;

/**
 * This enum all handled Netatmo modules and devices along with their capabilities
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum ModuleType {
    UNKNOWN(FeatureArea.NONE, "", null, List.of(), List.of()),
    ACCOUNT(FeatureArea.NONE, "", null, List.of(), List.of()),

    HOME(FeatureArea.NONE, "NAHome", ACCOUNT,
            List.of(DeviceCapability.class, EventCapability.class, HomeCapability.class, ChannelHelperCapability.class),
            List.of(HomeSecurityChannelHelper.class, HomeEnergyChannelHelper.class)),

    PERSON(FeatureArea.SECURITY, "NAPerson", HOME,
            List.of(EventCapability.class, PersonCapability.class, ChannelHelperCapability.class),
            List.of(PersonChannelHelper.class, EventPersonChannelHelper.class)),

    WELCOME(FeatureArea.SECURITY, "NACamera", HOME,
            List.of(EventCapability.class, CameraCapability.class, ChannelHelperCapability.class),
            List.of(CameraChannelHelper.class, SignalChannelHelper.class, EventChannelHelper.class)),

    SIREN(FeatureArea.SECURITY, "NIS", WELCOME, List.of(ChannelHelperCapability.class),
            List.of(SirenChannelHelper.class, BatteryChannelHelper.class, TimestampChannelHelper.class,
                    SignalChannelHelper.class)),

    PRESENCE(FeatureArea.SECURITY, "NOC", HOME,
            List.of(EventCapability.class, PresenceCapability.class, ChannelHelperCapability.class),
            List.of(PresenceChannelHelper.class, SignalChannelHelper.class, EventChannelHelper.class)),

    DOORBELL(FeatureArea.SECURITY, "NDB", HOME,
            List.of(EventCapability.class, CameraCapability.class, ChannelHelperCapability.class),
            List.of(DoorbellChannelHelper.class, SignalChannelHelper.class, EventDoorbellChannelHelper.class)),

    WEATHER_STATION(FeatureArea.WEATHER, "NAMain", ACCOUNT,
            List.of(DeviceCapability.class, WeatherCapability.class, MeasureCapability.class,
                    ChannelHelperCapability.class),
            List.of(PressureExtChannelHelper.class, NoiseChannelHelper.class, HumidityChannelHelper.class,
                    TemperatureExtChannelHelper.class, AirQualityChannelHelper.class, LocationChannelHelper.class,
                    TimestampExtChannelHelper.class, MeasuresChannelHelper.class, SignalChannelHelper.class)),

    OUTDOOR(FeatureArea.WEATHER, "NAModule1", WEATHER_STATION,
            List.of(MeasureCapability.class, ChannelHelperCapability.class),
            List.of(HumidityChannelHelper.class, TemperatureOutChannelHelper.class, BatteryChannelHelper.class,
                    MeasuresChannelHelper.class, TimestampExtChannelHelper.class, SignalChannelHelper.class)),

    WIND(FeatureArea.WEATHER, "NAModule2", WEATHER_STATION, List.of(ChannelHelperCapability.class),
            List.of(WindChannelHelper.class, BatteryChannelHelper.class, TimestampExtChannelHelper.class,
                    SignalChannelHelper.class)),

    RAIN(FeatureArea.WEATHER, "NAModule3", WEATHER_STATION,
            List.of(MeasureCapability.class, ChannelHelperCapability.class),
            List.of(RainChannelHelper.class, BatteryChannelHelper.class, MeasuresChannelHelper.class,
                    TimestampExtChannelHelper.class, SignalChannelHelper.class)),

    INDOOR(FeatureArea.WEATHER, "NAModule4", WEATHER_STATION,
            List.of(MeasureCapability.class, ChannelHelperCapability.class),
            List.of(HumidityChannelHelper.class, TemperatureExtChannelHelper.class, AirQualityChannelHelper.class,
                    BatteryChannelHelper.class, MeasuresChannelHelper.class, TimestampExtChannelHelper.class,
                    SignalChannelHelper.class)),

    HOME_COACH(FeatureArea.AIR_CARE, "NHC", ACCOUNT,
            List.of(DeviceCapability.class, AirCareCapability.class, MeasureCapability.class,
                    ChannelHelperCapability.class),
            List.of(NoiseChannelHelper.class, HumidityChannelHelper.class, AirQualityExtChannelHelper.class,
                    TemperatureChannelHelper.class, PressureChannelHelper.class, TimestampExtChannelHelper.class,
                    SignalChannelHelper.class, MeasuresChannelHelper.class, LocationChannelHelper.class)),

    PLUG(FeatureArea.ENERGY, "NAPlug", HOME, List.of(ChannelHelperCapability.class),
            List.of(SignalChannelHelper.class)),

    VALVE(FeatureArea.ENERGY, "NRV", PLUG, List.of(ChannelHelperCapability.class),
            List.of(BatteryExtChannelHelper.class, SignalChannelHelper.class)),

    THERMOSTAT(FeatureArea.ENERGY, "NATherm1", PLUG, List.of(ChannelHelperCapability.class),
            List.of(Therm1ChannelHelper.class, BatteryExtChannelHelper.class, SignalChannelHelper.class)),

    ROOM(FeatureArea.ENERGY, "NARoom", HOME, List.of(RoomCapability.class, ChannelHelperCapability.class),
            List.of(RoomChannelHelper.class, SetpointChannelHelper.class));

    public static final EnumSet<ModuleType> AS_SET = EnumSet.allOf(ModuleType.class);

    private final @Nullable ModuleType bridgeType;
    public final List<String> groupTypes = new LinkedList<>();
    public final List<String> extensions = new LinkedList<>();
    public final List<Class<? extends ChannelHelper>> channelHelpers;
    public final List<Class<? extends Capability>> capabilities;
    public final ThingTypeUID thingTypeUID;
    public final FeatureArea feature;
    public final String apiName;

    ModuleType(FeatureArea feature, String apiName, @Nullable ModuleType bridge,
            List<Class<? extends Capability>> capabilities, List<Class<? extends ChannelHelper>> helpers) {
        this.channelHelpers = helpers;
        this.bridgeType = bridge;
        this.feature = feature;
        this.capabilities = capabilities;
        this.apiName = apiName;
        thingTypeUID = new ThingTypeUID(BINDING_ID, name().toLowerCase().replace("_", "-"));
        try {
            for (Class<? extends ChannelHelper> helperClass : helpers) {
                ChannelHelper helper = helperClass.getConstructor().newInstance();
                groupTypes.addAll(helper.getChannelGroupTypes());
                extensions.addAll(helper.getExtensibleChannels());
            }
        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean isLogical() {
        return !channelHelpers.contains(SignalChannelHelper.class);
    }

    public boolean isABridge() {
        for (ModuleType mt : ModuleType.values()) {
            if (this.equals(mt.bridgeType)) {
                return true;
            }
        }
        return false;
    }

    public int[] getSignalLevels() {
        if (!isLogical()) {
            return (channelHelpers.contains(BatteryChannelHelper.class)
                    || channelHelpers.contains(BatteryExtChannelHelper.class)) ? RADIO_SIGNAL_LEVELS
                            : WIFI_SIGNAL_LEVELS;
        }
        throw new IllegalArgumentException(
                "This should not be called for module type : " + name() + ", please file a bug report.");
    }

    public ModuleType getBridge() {
        ModuleType bridge = bridgeType;
        return bridge != null ? bridge : ModuleType.UNKNOWN;
    }

    public URI getConfigDescription() {
        return URI.create(BINDING_ID + ":"
                + (equals(ACCOUNT) ? "api_bridge"
                        : equals(HOME) ? "home"
                                : (isLogical() ? "virtual"
                                        : ModuleType.UNKNOWN.equals(getBridge()) ? "configurable" : "device")));
    }

    public static ModuleType from(ThingTypeUID thingTypeUID) {
        return ModuleType.AS_SET.stream().filter(mt -> mt.thingTypeUID.equals(thingTypeUID)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException());
    }

    public static ModuleType from(String apiName) {
        return ModuleType.AS_SET.stream().filter(mt -> apiName.equals(mt.apiName)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException());
    }
}
