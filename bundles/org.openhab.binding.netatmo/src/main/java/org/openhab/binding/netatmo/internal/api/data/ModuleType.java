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
import org.openhab.binding.netatmo.internal.handler.capability.EventCapability;
import org.openhab.binding.netatmo.internal.handler.capability.HomeCapability;
import org.openhab.binding.netatmo.internal.handler.capability.ModuleCapability;
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
import org.openhab.binding.netatmo.internal.handler.channelhelper.EventChannelHelper;
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
import org.openhab.binding.netatmo.internal.handler.channelhelper.TemperatureChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.TemperatureExtChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.Therm1ChannelHelper;
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
    UNKNOWN(FeatureArea.NONE, null, List.of(), List.of()),
    NAHome(FeatureArea.NONE, null, // Home hosting security or energy devices ==========================================
            List.of(HomeSecurityChannelHelper.class, HomeEnergyChannelHelper.class),
            List.of(ModuleCapability.class, EventCapability.class, HomeCapability.class)),
    NAPerson(FeatureArea.SECURITY, NAHome, // Person identified by security modules ====================================
            List.of(PersonChannelHelper.class, EventPersonChannelHelper.class),
            List.of(EventCapability.class, PersonCapability.class)),
    NACamera(FeatureArea.SECURITY, NAHome, // Welcome Camera ===========================================================
            List.of(CameraChannelHelper.class, SignalChannelHelper.class, EventChannelHelper.class),
            List.of(EventCapability.class, CameraCapability.class)),
    NOC(FeatureArea.SECURITY, NAHome, // Netatmo Presence Camera =======================================================
            List.of(CameraChannelHelper.class, PresenceChannelHelper.class, SignalChannelHelper.class,
                    EventChannelHelper.class),
            List.of(EventCapability.class, PresenceCapability.class)),
    NDB(FeatureArea.SECURITY, NAHome, // Netatmo Doorbell ==============================================================
            List.of(CameraChannelHelper.class, SignalChannelHelper.class, EventChannelHelper.class), List.of()),
    NAMain(FeatureArea.WEATHER, null, // Main Weather Station ==========================================================
            List.of(PressureExtChannelHelper.class, NoiseChannelHelper.class, HumidityChannelHelper.class,
                    TemperatureExtChannelHelper.class, AirQualityChannelHelper.class, LocationChannelHelper.class,
                    TimestampExtChannelHelper.class, MeasuresChannelHelper.class, SignalChannelHelper.class),
            List.of(ModuleCapability.class, WeatherCapability.class)),
    NAModule1(FeatureArea.WEATHER, NAMain, // External Temperature & Humidity sensor ===================================
            List.of(HumidityChannelHelper.class, TemperatureExtChannelHelper.class, BatteryChannelHelper.class,
                    MeasuresChannelHelper.class, TimestampExtChannelHelper.class, SignalChannelHelper.class),
            List.of(ModuleCapability.class)),
    NAModule2(FeatureArea.WEATHER, NAMain, // Wind sensor ==============================================================
            List.of(WindChannelHelper.class, BatteryChannelHelper.class, TimestampExtChannelHelper.class,
                    SignalChannelHelper.class),
            List.of(ModuleCapability.class)),
    NAModule3(FeatureArea.WEATHER, NAMain, // Rain sensor ==============================================================
            List.of(RainChannelHelper.class, BatteryChannelHelper.class, MeasuresChannelHelper.class,
                    TimestampExtChannelHelper.class, SignalChannelHelper.class),
            List.of(ModuleCapability.class)),
    NAModule4(FeatureArea.WEATHER, NAMain, // Additional indoor sensor =================================================
            List.of(HumidityChannelHelper.class, TemperatureExtChannelHelper.class, AirQualityChannelHelper.class,
                    BatteryChannelHelper.class, MeasuresChannelHelper.class, TimestampExtChannelHelper.class,
                    SignalChannelHelper.class),
            List.of(ModuleCapability.class)),
    NHC(FeatureArea.AIR_CARE, null, // Healty Home Coach ===============================================================
            List.of(NoiseChannelHelper.class, HumidityChannelHelper.class, AirQualityExtChannelHelper.class,
                    TemperatureChannelHelper.class, PressureChannelHelper.class, TimestampExtChannelHelper.class,
                    SignalChannelHelper.class, MeasuresChannelHelper.class, LocationChannelHelper.class),
            List.of(ModuleCapability.class, AirCareCapability.class)),
    NAPlug(FeatureArea.ENERGY, NAHome, // Boiler relay =================================================================
            List.of(SignalChannelHelper.class), List.of(ModuleCapability.class)),
    NATherm1(FeatureArea.ENERGY, NAHome, // Thermostat module ==========================================================
            List.of(Therm1ChannelHelper.class, BatteryExtChannelHelper.class, SignalChannelHelper.class),
            List.of(ModuleCapability.class)),
    NARoom(FeatureArea.ENERGY, NAHome, // Room holding energy modules ==================================================
            List.of(RoomChannelHelper.class, SetpointChannelHelper.class), List.of(RoomCapability.class)),
    NRV(FeatureArea.ENERGY, NAHome, // Valve ===========================================================================
            List.of(BatteryExtChannelHelper.class, SignalChannelHelper.class), List.of(ModuleCapability.class));

    public static final EnumSet<ModuleType> AS_SET = EnumSet.allOf(ModuleType.class);

    public final List<String> groups = new LinkedList<>();
    public final List<String> extensions = new LinkedList<>();
    public final List<Class<? extends ChannelHelper>> channelHelpers;
    public final List<Class<? extends Capability>> capabilities;
    private final @Nullable ModuleType bridgeType;
    public final ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, name());
    public final FeatureArea feature;

    ModuleType(FeatureArea feature, @Nullable ModuleType bridge, List<Class<? extends ChannelHelper>> helpers,
            List<Class<? extends Capability>> capabilities) {
        this.channelHelpers = helpers;
        this.bridgeType = bridge;
        this.feature = feature;
        this.capabilities = capabilities;
        try {
            for (Class<? extends ChannelHelper> helperClass : helpers) {
                ChannelHelper helper = helperClass.getConstructor().newInstance();
                groups.addAll(helper.getChannelGroupTypes());
                extensions.addAll(helper.getMeasureChannels());
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
        throw new IllegalArgumentException("This should not be called for module type : " + name() + ", file a bug.");
    }

    public ModuleType getBridge() {
        ModuleType bridge = bridgeType;
        return bridge != null ? bridge : ModuleType.UNKNOWN;
    }

    public URI getConfigDescription() {
        return URI.create(BINDING_ID + ":"
                + (isLogical() ? "virtual" : ModuleType.UNKNOWN.equals(getBridge()) ? "configurable" : "device"));
    }
}
