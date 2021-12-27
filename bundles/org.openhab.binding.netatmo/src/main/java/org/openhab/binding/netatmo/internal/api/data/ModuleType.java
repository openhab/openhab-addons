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
package org.openhab.binding.netatmo.internal.api.data;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.BINDING_ID;
import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.*;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.NAModule;
import org.openhab.binding.netatmo.internal.api.dto.NARoom;
import org.openhab.binding.netatmo.internal.api.dto.NAThermostat;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.dto.NAWelcome;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.AirQualityChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.AirQualityExtChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.BatteryChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.BatteryExtChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.CameraChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HomeEnergyChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HomeSecurityChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HumidityChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.LocationChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.MeasuresChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.NoiseChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PersonChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PresenceChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PressureChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PressureExtChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.RainChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.RoomChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.RoomSetpointChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.SignalChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.TemperatureChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.TemperatureExtChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.Therm1ChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.TimestampExtChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.WindChannelHelper;
import org.openhab.binding.netatmo.internal.handler.CameraHandler;
import org.openhab.binding.netatmo.internal.handler.DeviceHandler;
import org.openhab.binding.netatmo.internal.handler.DeviceWithMeasureHandler;
import org.openhab.binding.netatmo.internal.handler.HomeCoachHandler;
import org.openhab.binding.netatmo.internal.handler.HomeHandler;
import org.openhab.binding.netatmo.internal.handler.MainHandler;
import org.openhab.binding.netatmo.internal.handler.PersonHandler;
import org.openhab.binding.netatmo.internal.handler.PresenceHandler;
import org.openhab.binding.netatmo.internal.handler.RoomHandler;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;

/**
 * This enum all handled Netatmo modules and devices along with their capabilities
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum ModuleType {
    UNKNOWN(null, RefreshPolicy.NONE, FeatureArea.NONE, null, null, List.of()),
    NAHome(HomeHandler.class, RefreshPolicy.CONFIG, FeatureArea.NONE, null, null,
            List.of(HomeSecurityChannelHelper.class, HomeEnergyChannelHelper.class)),

    // Security Features
    NAPerson(PersonHandler.class, RefreshPolicy.PARENT, FeatureArea.SECURITY, NAHome, null,
            List.of(PersonChannelHelper.class)),
    NACamera(CameraHandler.class, RefreshPolicy.PARENT, FeatureArea.SECURITY, NAHome, NAWelcome.class,
            List.of(CameraChannelHelper.class, SignalChannelHelper.class)),
    NOC(PresenceHandler.class, RefreshPolicy.PARENT, FeatureArea.SECURITY, NAHome, NAWelcome.class,
            List.of(CameraChannelHelper.class, PresenceChannelHelper.class, SignalChannelHelper.class)),
    NDB(PresenceHandler.class, RefreshPolicy.PARENT, FeatureArea.SECURITY, NAHome, NAWelcome.class,
            List.of(CameraChannelHelper.class, SignalChannelHelper.class)),

    // Weather Features
    NAMain(MainHandler.class, RefreshPolicy.AUTO, FeatureArea.WEATHER, null, NAThing.class,
            List.of(PressureExtChannelHelper.class, NoiseChannelHelper.class, HumidityChannelHelper.class,
                    TemperatureExtChannelHelper.class, AirQualityChannelHelper.class, LocationChannelHelper.class,
                    TimestampExtChannelHelper.class, MeasuresChannelHelper.class, SignalChannelHelper.class)),
    NAModule1(DeviceWithMeasureHandler.class, RefreshPolicy.PARENT, FeatureArea.WEATHER, NAMain, NAModule.class,
            List.of(HumidityChannelHelper.class, TemperatureExtChannelHelper.class, BatteryChannelHelper.class,
                    MeasuresChannelHelper.class, TimestampExtChannelHelper.class, SignalChannelHelper.class)),
    NAModule2(DeviceWithMeasureHandler.class, RefreshPolicy.PARENT, FeatureArea.WEATHER, NAMain, NAModule.class,
            List.of(WindChannelHelper.class, BatteryChannelHelper.class, TimestampExtChannelHelper.class,
                    SignalChannelHelper.class)),
    NAModule3(DeviceWithMeasureHandler.class, RefreshPolicy.PARENT, FeatureArea.WEATHER, NAMain, NAModule.class,
            List.of(RainChannelHelper.class, BatteryChannelHelper.class, MeasuresChannelHelper.class,
                    TimestampExtChannelHelper.class, SignalChannelHelper.class)),
    NAModule4(DeviceWithMeasureHandler.class, RefreshPolicy.PARENT, FeatureArea.WEATHER, NAMain, NAModule.class,
            List.of(HumidityChannelHelper.class, TemperatureExtChannelHelper.class, AirQualityChannelHelper.class,
                    BatteryChannelHelper.class, MeasuresChannelHelper.class, TimestampExtChannelHelper.class,
                    SignalChannelHelper.class)),

    // Aircare Features
    NHC(HomeCoachHandler.class, RefreshPolicy.AUTO, FeatureArea.AIR_CARE, null, NAThing.class,
            List.of(NoiseChannelHelper.class, HumidityChannelHelper.class, AirQualityExtChannelHelper.class,
                    TemperatureChannelHelper.class, PressureChannelHelper.class, TimestampExtChannelHelper.class,
                    SignalChannelHelper.class, MeasuresChannelHelper.class, LocationChannelHelper.class)),

    // Energy Features
    NAPlug(DeviceHandler.class, RefreshPolicy.PARENT, FeatureArea.ENERGY, NAHome, NAModule.class,
            List.of(SignalChannelHelper.class)),
    NATherm1(DeviceHandler.class, RefreshPolicy.CONFIG, FeatureArea.ENERGY, NAHome, NAThermostat.class,
            List.of(Therm1ChannelHelper.class, BatteryExtChannelHelper.class, SignalChannelHelper.class)),
    NARoom(RoomHandler.class, RefreshPolicy.PARENT, FeatureArea.ENERGY, NAHome, NARoom.class,
            List.of(RoomChannelHelper.class, RoomSetpointChannelHelper.class)),
    NRV(DeviceHandler.class, RefreshPolicy.CONFIG, FeatureArea.ENERGY, NAHome, NAModule.class,
            List.of(BatteryExtChannelHelper.class, SignalChannelHelper.class));

    public enum RefreshPolicy {
        AUTO,
        PARENT,
        CONFIG,
        NONE;
    }

    public static final EnumSet<ModuleType> AS_SET = EnumSet.allOf(ModuleType.class);

    public final List<String> groups = new LinkedList<>();
    public final List<String> extensions = new LinkedList<>();
    public final List<Class<? extends AbstractChannelHelper>> channelHelpers;
    public final @Nullable ModuleType bridgeType;
    public final @Nullable Class<?> handlerClass;
    public final @Nullable Class<?> dto;
    public final ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, name());
    public final RefreshPolicy refreshPeriod;
    public final FeatureArea features;

    ModuleType(@Nullable Class<?> handlerClass, RefreshPolicy refreshPeriod, FeatureArea features,
            @Nullable ModuleType bridge, @Nullable Class<?> dto, List<Class<? extends AbstractChannelHelper>> helpers) {
        this.handlerClass = handlerClass;
        this.refreshPeriod = refreshPeriod;
        this.channelHelpers = helpers;
        this.bridgeType = bridge;
        this.dto = dto;
        this.features = features;

        try {
            for (Class<? extends AbstractChannelHelper> helperClass : helpers) {
                AbstractChannelHelper helper = helperClass.getConstructor().newInstance();
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

    public @Nullable ThingTypeUID getBridgeUID() {
        return getBridge().thingTypeUID;
    }

    public Constructor<?> getHandlerConstructor() throws NoSuchMethodException, SecurityException {
        Class<?> handler = handlerClass;
        if (handler != null) {
            return handler.getConstructor(Bridge.class, List.class, ApiBridge.class, NetatmoDescriptionProvider.class);
        }
        throw new IllegalArgumentException("This should not be called for module type : " + name() + ", file a bug.");
    }

    public URI getConfigDescription() {
        return URI.create(BINDING_ID + ":"
                + (isLogical() ? "virtual" : refreshPeriod == RefreshPolicy.CONFIG ? "configurable" : "device"));
    }
}
