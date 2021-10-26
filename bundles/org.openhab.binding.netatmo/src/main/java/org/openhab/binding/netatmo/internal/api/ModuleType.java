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
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.NAModule;
import org.openhab.binding.netatmo.internal.api.dto.NARoom;
import org.openhab.binding.netatmo.internal.api.dto.NAThermostat;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.dto.NAWelcome;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.BatteryChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.CameraChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.Co2ChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HomeCoachChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HomeEnergyChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HomeSecurityChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HumidityChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.MeasuresChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.NoiseChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PersonChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PresenceChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PressureChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.RainChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.RoomChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.RoomSetpointChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.SignalChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.TemperatureChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.Therm1ChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.TimestampChannelHelper;
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
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;

/**
 * This enum all handled Netatmo modules and devices along with their capabilities
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum ModuleType {
    UNKNOWN(null, RefreshPolicy.NONE, FeatureArea.NONE, null, List.of(), List.of(), null),
    NAHome(HomeHandler.class, RefreshPolicy.CONFIG, FeatureArea.NONE, null, List.of(),
            List.of(HomeSecurityChannelHelper.class, HomeEnergyChannelHelper.class), null),

    // Security Features
    NAPerson(PersonHandler.class, RefreshPolicy.PARENT, FeatureArea.SECURITY, NAHome, List.of(),
            List.of(PersonChannelHelper.class), null),
    NACamera(CameraHandler.class, RefreshPolicy.PARENT, FeatureArea.SECURITY, NAHome, List.of(),
            List.of(CameraChannelHelper.class), NAWelcome.class),
    NOC(PresenceHandler.class, RefreshPolicy.PARENT, FeatureArea.SECURITY, NAHome, List.of(),
            List.of(CameraChannelHelper.class, PresenceChannelHelper.class), NAWelcome.class),
    NDB(PresenceHandler.class, RefreshPolicy.PARENT, FeatureArea.SECURITY, NAHome, List.of(),
            List.of(CameraChannelHelper.class), NAWelcome.class),

    // Weather Features
    NAMain(MainHandler.class, RefreshPolicy.AUTO, FeatureArea.WEATHER, null, List.of("measure", "measure-timestamp"),
            List.of(PressureChannelHelper.class, NoiseChannelHelper.class, HumidityChannelHelper.class,
                    TemperatureChannelHelper.class, Co2ChannelHelper.class, TimestampChannelHelper.class,
                    MeasuresChannelHelper.class, SignalChannelHelper.class),
            NAThing.class),
    NAModule1(DeviceWithMeasureHandler.class, RefreshPolicy.PARENT, FeatureArea.WEATHER, NAMain,
            List.of("measure", "measure-timestamp"),
            List.of(HumidityChannelHelper.class, TemperatureChannelHelper.class, BatteryChannelHelper.class,
                    MeasuresChannelHelper.class, TimestampChannelHelper.class, SignalChannelHelper.class),
            NAModule.class),
    NAModule2(DeviceWithMeasureHandler.class, RefreshPolicy.PARENT, FeatureArea.WEATHER, NAMain, List.of(),
            List.of(WindChannelHelper.class, BatteryChannelHelper.class, TimestampChannelHelper.class,
                    SignalChannelHelper.class),
            NAModule.class),
    NAModule3(DeviceWithMeasureHandler.class, RefreshPolicy.PARENT, FeatureArea.WEATHER, NAMain, List.of("sum-rain"),
            List.of(RainChannelHelper.class, BatteryChannelHelper.class, MeasuresChannelHelper.class,
                    TimestampChannelHelper.class, SignalChannelHelper.class),
            NAModule.class),
    NAModule4(DeviceWithMeasureHandler.class, RefreshPolicy.PARENT, FeatureArea.WEATHER, NAMain,
            List.of("measure", "measure-timestamp"),
            List.of(HumidityChannelHelper.class, TemperatureChannelHelper.class, Co2ChannelHelper.class,
                    BatteryChannelHelper.class, MeasuresChannelHelper.class, TimestampChannelHelper.class,
                    SignalChannelHelper.class),
            NAModule.class),

    // Aircare Features
    NHC(HomeCoachHandler.class, RefreshPolicy.AUTO, FeatureArea.AIR_CARE, null, List.of(),
            List.of(NoiseChannelHelper.class, HumidityChannelHelper.class, PressureChannelHelper.class,
                    TemperatureChannelHelper.class, Co2ChannelHelper.class, HomeCoachChannelHelper.class,
                    TimestampChannelHelper.class, MeasuresChannelHelper.class, SignalChannelHelper.class),
            NAThing.class),

    // Energy Features
    NAPlug(DeviceHandler.class, RefreshPolicy.PARENT, FeatureArea.ENERGY, NAHome, List.of(),
            List.of(SignalChannelHelper.class), NAModule.class),
    NATherm1(DeviceHandler.class, RefreshPolicy.CONFIG, FeatureArea.ENERGY, NAHome, List.of(),
            List.of(Therm1ChannelHelper.class, BatteryChannelHelper.class, SignalChannelHelper.class),
            NAThermostat.class),
    NARoom(RoomHandler.class, RefreshPolicy.PARENT, FeatureArea.ENERGY, NAHome, List.of(),
            List.of(RoomChannelHelper.class, RoomSetpointChannelHelper.class), NARoom.class),
    NRV(DeviceHandler.class, RefreshPolicy.CONFIG, FeatureArea.ENERGY, NAHome, List.of(),
            List.of(BatteryChannelHelper.class, SignalChannelHelper.class), NAModule.class),

    // Left for future implementation
    // NACamDoorTag : self explaining
    // NSD : smoke detector
    // NIS : indoor siren
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
    private final List<Class<? extends AbstractChannelHelper>> channelHelpers;
    private final @Nullable ModuleType bridgeType;
    private final @Nullable Class<?> handlerClass;
    private final @Nullable Class<?> dto;
    private final ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, name());
    private final RefreshPolicy refreshPeriod;
    private final FeatureArea features;

    ModuleType(@Nullable Class<?> handlerClass, RefreshPolicy refreshPeriod, FeatureArea features,
            @Nullable ModuleType bridge, List<String> extensions,
            List<Class<? extends AbstractChannelHelper>> setOfHelpers, @Nullable Class<?> dto) {
        this.handlerClass = handlerClass;
        this.refreshPeriod = refreshPeriod;
        this.extensions = extensions;
        this.channelHelpers = setOfHelpers;
        this.bridgeType = bridge;
        this.dto = dto;
        this.features = features;
        List<String> groupsFromHelpers = new LinkedList<>();
        setOfHelpers.forEach(helperClass -> {
            try {
                Constructor<? extends AbstractChannelHelper> constructor = helperClass.getConstructor();
                AbstractChannelHelper helper = constructor.newInstance();
                groupsFromHelpers.addAll(helper.getChannelGroups());
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
        });
        this.groups = groupsFromHelpers;
    }

    public boolean matches(ThingTypeUID otherThingTypeUID) {
        return thingTypeUID.equals(otherThingTypeUID);
    }

    public boolean isPhysicalEquipment() {
        return groups.contains(GROUP_SIGNAL);
    }

    public int[] getSignalLevels() {
        if (!isPhysicalEquipment()) {
            throw new IllegalArgumentException(
                    "This should not be called for module type : " + name() + ", file a bug.");
        }
        return groups.contains(GROUP_BATTERY) ? RADIO_SIGNAL_LEVELS : WIFI_SIGNAL_LEVELS;
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
        ModuleType localBridge = bridgeType;
        return localBridge == null || localBridge == ModuleType.UNKNOWN ? null : localBridge.thingTypeUID;
    }

    public ModuleType getBridgeType() {
        ModuleType localBridge = bridgeType;
        return localBridge == null ? ModuleType.UNKNOWN : localBridge;
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

    public static boolean isModuleTypeImplemented(String name) {
        return asSet.stream().anyMatch(mt -> mt.toString().equals(name));
    }

    public FeatureArea getFeatures() {
        return features;
    }
}
