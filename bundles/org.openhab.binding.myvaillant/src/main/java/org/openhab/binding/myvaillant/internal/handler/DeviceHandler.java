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
package org.openhab.binding.myvaillant.internal.handler;

import static org.openhab.binding.myvaillant.internal.MyVaillantBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myvaillant.internal.api.EnergyDataType;
import org.openhab.binding.myvaillant.internal.api.JsonUtil;
import org.openhab.binding.myvaillant.internal.api.MyVaillantApi;
import org.openhab.binding.myvaillant.internal.api.MyVaillantDevice;
import org.openhab.binding.myvaillant.internal.api.SystemData;
import org.openhab.binding.myvaillant.internal.config.DeviceConfiguration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;

/**
 * A device of a system, e.g. the heat pump or the electric backup heater of the hydraulic station.
 *
 * <p>
 * Like the Home Assistant integration creates one sensor per energy value, a channel with today's value is created for
 * each energy value (operation mode and value type) the device reports. Only linked energy channels are updated, each
 * costs one request per update.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public class DeviceHandler extends ComponentHandler {

    private static final String PROPERTY_OPERATION_MODE = "operationMode";
    private static final String PROPERTY_VALUE_TYPE = "valueType";
    private static final ChannelTypeUID ENERGY_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_ENERGY_TODAY);

    private String deviceUuid = "";

    public DeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected @Nullable String readConfiguration() {
        deviceUuid = getConfigAs(DeviceConfiguration.class).deviceUuid.trim();
        return deviceUuid.isEmpty() ? "@text/offline.conf-error.no-device-uuid" : null;
    }

    /** ID of the device in the myVAILLANT cloud */
    public String getDeviceUuid() {
        return deviceUuid;
    }

    @Override
    protected boolean isPresent(SystemData systemData) {
        return systemData.getDevice(deviceUuid) != null;
    }

    @Override
    protected void updateChannels(SystemData systemData) {
        MyVaillantDevice device = systemData.getDevice(deviceUuid);
        if (device == null) {
            return;
        }
        addEnergyChannels(device);
        updateDeviceProperties(device);

        updateState(CHANNEL_CURRENT_POWER, States.quantity(device.getCurrentPower(), Units.WATT));
        Integer operationTimeMinutes = device.getOperationTimeMinutes();
        updateState(CHANNEL_OPERATION_TIME, States.quantity(
                operationTimeMinutes == null ? null : Math.round(operationTimeMinutes / 6.0) / 10.0, Units.HOUR));
        updateState(CHANNEL_ON_OFF_CYCLES, States.decimal(device.getOnOffCycles()));
        updateState(CHANNEL_WATER_PRESSURE, States.quantity(device.getWaterPressure(), Units.BAR));

        SystemHandler systemHandler = getSystemHandler();
        Map<EnergyDataType, Double> energy = systemHandler == null ? null
                : systemHandler.getEnergyToday().get(deviceUuid);
        if (energy != null) {
            updateEnergy(energy);
        }
    }

    private void addEnergyChannels(MyVaillantDevice device) {
        List<Channel> missing = new ArrayList<>();
        for (EnergyDataType type : device.getEnergyDataTypes()) {
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), type.getChannelId());
            if (getThing().getChannel(channelUID) == null) {
                missing.add(ChannelBuilder.create(channelUID, "Number:Energy").withType(ENERGY_CHANNEL_TYPE)
                        .withLabel(type.getLabel()).withProperties(Map.of(PROPERTY_OPERATION_MODE, type.operationMode(),
                                PROPERTY_VALUE_TYPE, type.valueType()))
                        .build());
            }
        }
        if (!missing.isEmpty()) {
            ThingBuilder builder = editThing();
            missing.forEach(builder::withChannel);
            updateThing(builder.build());
        }
    }

    private void updateDeviceProperties(MyVaillantDevice device) {
        Map<String, String> properties = new HashMap<>(editProperties());
        properties.put("name", device.getName());
        properties.put("type", device.type());
        putProperty(properties, "deviceType", device.getDeviceType());
        putProperty(properties, "productName", JsonUtil.getString(device.json(), "product_name"));
        putProperty(properties, "ebusId", JsonUtil.getString(device.json(), "ebus_id"));
        putProperty(properties, "articleNumber", JsonUtil.getString(device.json(), "article_number"));
        putProperty(properties, Thing.PROPERTY_SERIAL_NUMBER,
                JsonUtil.getString(device.json(), "device_serial_number"));
        if (!properties.equals(editProperties())) {
            updateProperties(properties);
        }
    }

    private static void putProperty(Map<String, String> properties, String name, @Nullable String value) {
        if (value != null) {
            properties.put(name, value);
        }
    }

    /** Energy values of linked channels, the heating efficiency needs heating consumption and heat */
    Set<EnergyDataType> getRequiredEnergyDataTypes() {
        Set<EnergyDataType> types = new LinkedHashSet<>();
        for (Channel channel : getThing().getChannels()) {
            EnergyDataType type = getEnergyDataType(channel);
            if (type != null && isLinked(channel.getUID())) {
                types.add(type);
            }
        }
        if (isLinked(CHANNEL_HEATING_ENERGY_EFFICIENCY)) {
            types.add(new EnergyDataType(OPERATION_MODE_HEATING, VALUE_TYPE_CONSUMED_ELECTRICAL_ENERGY));
            types.add(new EnergyDataType(OPERATION_MODE_HEATING, VALUE_TYPE_HEAT_GENERATED));
        }
        return types;
    }

    private static @Nullable EnergyDataType getEnergyDataType(Channel channel) {
        if (!ENERGY_CHANNEL_TYPE.equals(channel.getChannelTypeUID())) {
            return null;
        }
        String operationMode = channel.getProperties().get(PROPERTY_OPERATION_MODE);
        String valueType = channel.getProperties().get(PROPERTY_VALUE_TYPE);
        return operationMode == null || valueType == null ? null : new EnergyDataType(operationMode, valueType);
    }

    /** Today's energy values in Wh, called by the system bridge */
    void updateEnergy(Map<EnergyDataType, Double> values) {
        for (Channel channel : getThing().getChannels()) {
            EnergyDataType type = getEnergyDataType(channel);
            Double value = type == null ? null : values.get(type);
            if (value != null) {
                updateState(channel.getUID(), new QuantityType<>(value, Units.WATT_HOUR));
            }
        }
        if (values.keySet().stream().anyMatch(SystemHandler::isHeatingEfficiencyType)) {
            updateState(CHANNEL_HEATING_ENERGY_EFFICIENCY, SystemHandler.getHeatingEfficiency(List.of(values)));
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        Channel channel = getThing().getChannel(channelUID);
        if (channel != null && (getEnergyDataType(channel) != null
                || CHANNEL_HEATING_ENERGY_EFFICIENCY.equals(channelUID.getId()))) {
            SystemHandler systemHandler = getSystemHandler();
            if (systemHandler != null) {
                systemHandler.scheduleEnergyRefresh();
            }
        }
    }

    @Override
    protected boolean sendCommand(String channelId, Command command, MyVaillantApi api, SystemData systemData,
            SystemHandler systemHandler) {
        return false;
    }
}
