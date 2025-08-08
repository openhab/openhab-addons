/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.openhab.binding.matter.internal.MatterBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThermostatCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.binding.matter.internal.util.ValueUtils;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;

/**
 * A converter for translating {@link ThermostatCluster} events and attributes to openHAB channels and back again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ThermostatConverter extends GenericConverter<ThermostatCluster> {

    public ThermostatConverter(ThermostatCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Map<Channel, @Nullable StateDescription> channels = new HashMap<>();

        Channel channel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_THERMOSTAT_SYSTEMMODE), CoreItemFactory.NUMBER)
                .withType(CHANNEL_THERMOSTAT_SYSTEMMODE).build();

        List<StateOption> modeOptions = new ArrayList<>();

        modeOptions.add(new StateOption(ThermostatCluster.SystemModeEnum.OFF.value.toString(),
                ThermostatCluster.SystemModeEnum.OFF.label));
        if (initializingCluster.featureMap.autoMode) {
            modeOptions.add(new StateOption(ThermostatCluster.SystemModeEnum.AUTO.value.toString(),
                    ThermostatCluster.SystemModeEnum.AUTO.label));
        }
        if (initializingCluster.featureMap.cooling) {
            modeOptions.add(new StateOption(ThermostatCluster.SystemModeEnum.COOL.value.toString(),
                    ThermostatCluster.SystemModeEnum.COOL.label));
            modeOptions.add(new StateOption(ThermostatCluster.SystemModeEnum.PRECOOLING.value.toString(),
                    ThermostatCluster.SystemModeEnum.PRECOOLING.label));
        }
        if (initializingCluster.featureMap.heating) {
            modeOptions.add(new StateOption(ThermostatCluster.SystemModeEnum.HEAT.value.toString(),
                    ThermostatCluster.SystemModeEnum.HEAT.label));
            modeOptions.add(new StateOption(ThermostatCluster.SystemModeEnum.EMERGENCY_HEAT.value.toString(),
                    ThermostatCluster.SystemModeEnum.EMERGENCY_HEAT.label));
        }
        modeOptions.add(new StateOption(ThermostatCluster.SystemModeEnum.FAN_ONLY.value.toString(),
                ThermostatCluster.SystemModeEnum.FAN_ONLY.label));
        modeOptions.add(new StateOption(ThermostatCluster.SystemModeEnum.DRY.value.toString(),
                ThermostatCluster.SystemModeEnum.DRY.label));
        modeOptions.add(new StateOption(ThermostatCluster.SystemModeEnum.SLEEP.value.toString(),
                ThermostatCluster.SystemModeEnum.SLEEP.label));

        StateDescription stateDescriptionMode = StateDescriptionFragmentBuilder.create().withPattern("%d")
                .withOptions(modeOptions).build().toStateDescription();
        channels.put(channel, stateDescriptionMode);

        if (!initializingCluster.featureMap.localTemperatureNotExposed) {
            Channel tempChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_THERMOSTAT_LOCALTEMPERATURE),
                            "Number:Temperature")
                    .withType(CHANNEL_THERMOSTAT_LOCALTEMPERATURE).build();

            channels.put(tempChannel, null);
        }
        if (initializingCluster.outdoorTemperature != null) {
            Channel tempChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_THERMOSTAT_OUTDOORTEMPERATURE),
                            "Number:Temperature")
                    .withType(CHANNEL_THERMOSTAT_OUTDOORTEMPERATURE).build();
            channels.put(tempChannel, null);
        }
        if (initializingCluster.featureMap.heating) {
            Channel tempChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_THERMOSTAT_OCCUPIEDHEATING),
                            "Number:Temperature")
                    .withType(CHANNEL_THERMOSTAT_OCCUPIEDHEATING).build();
            StateDescription stateDescription = StateDescriptionFragmentBuilder.create()
                    .withMinimum(
                            ValueUtils.valueToTemperature(initializingCluster.absMinHeatSetpointLimit).toBigDecimal())
                    .withMaximum(
                            ValueUtils.valueToTemperature(initializingCluster.absMaxHeatSetpointLimit).toBigDecimal())
                    .withStep(BigDecimal.valueOf(1)).withPattern("%.1f %unit%").withReadOnly(false).build()
                    .toStateDescription();
            channels.put(tempChannel, stateDescription);
        }
        if (initializingCluster.featureMap.cooling) {
            Channel tempChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_THERMOSTAT_OCCUPIEDCOOLING),
                            "Number:Temperature")
                    .withType(CHANNEL_THERMOSTAT_OCCUPIEDCOOLING).build();
            StateDescription stateDescription = StateDescriptionFragmentBuilder.create()
                    .withMinimum(
                            ValueUtils.valueToTemperature(initializingCluster.absMinCoolSetpointLimit).toBigDecimal())
                    .withMaximum(
                            ValueUtils.valueToTemperature(initializingCluster.absMaxCoolSetpointLimit).toBigDecimal())
                    .withStep(BigDecimal.valueOf(1)).withPattern("%.1f %unit%").withReadOnly(false).build()
                    .toStateDescription();
            channels.put(tempChannel, stateDescription);
        }
        if (initializingCluster.featureMap.occupancy) {
            if (initializingCluster.featureMap.heating) {
                Channel tempChannel = ChannelBuilder
                        .create(new ChannelUID(channelGroupUID, CHANNEL_ID_THERMOSTAT_UNOCCUPIEDHEATING),
                                "Number:Temperature")
                        .withType(CHANNEL_THERMOSTAT_UNOCCUPIEDHEATING).build();
                StateDescription stateDescription = StateDescriptionFragmentBuilder.create()
                        .withMinimum(ValueUtils.valueToTemperature(initializingCluster.absMinHeatSetpointLimit)
                                .toBigDecimal())
                        .withMaximum(ValueUtils.valueToTemperature(initializingCluster.absMaxHeatSetpointLimit)
                                .toBigDecimal())
                        .withStep(BigDecimal.valueOf(1)).withPattern("%.1f %unit%").withReadOnly(false).build()
                        .toStateDescription();
                channels.put(tempChannel, stateDescription);
            }
            if (initializingCluster.featureMap.cooling) {
                Channel tempChannel = ChannelBuilder
                        .create(new ChannelUID(channelGroupUID, CHANNEL_ID_THERMOSTAT_UNOCCUPIEDCOOLING),
                                "Number:Temperature")
                        .withType(CHANNEL_THERMOSTAT_UNOCCUPIEDCOOLING).build();
                StateDescription stateDescription = StateDescriptionFragmentBuilder.create()
                        .withMinimum(ValueUtils.valueToTemperature(initializingCluster.absMinCoolSetpointLimit)
                                .toBigDecimal())
                        .withMaximum(ValueUtils.valueToTemperature(initializingCluster.absMaxCoolSetpointLimit)
                                .toBigDecimal())
                        .withStep(BigDecimal.valueOf(1)).withPattern("%.1f %unit%").withReadOnly(false).build()
                        .toStateDescription();
                channels.put(tempChannel, stateDescription);
            }
        }
        if (initializingCluster.thermostatRunningMode != null) {
            Channel tempChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_THERMOSTAT_RUNNINGMODE), CoreItemFactory.NUMBER)
                    .withType(CHANNEL_THERMOSTAT_RUNNINGMODE).build();
            List<StateOption> options = new ArrayList<>();
            options.add(new StateOption(ThermostatCluster.ThermostatRunningModeEnum.OFF.value.toString(),
                    ThermostatCluster.ThermostatRunningModeEnum.OFF.label));
            options.add(new StateOption(ThermostatCluster.ThermostatRunningModeEnum.HEAT.value.toString(),
                    ThermostatCluster.ThermostatRunningModeEnum.HEAT.label));
            options.add(new StateOption(ThermostatCluster.ThermostatRunningModeEnum.COOL.value.toString(),
                    ThermostatCluster.ThermostatRunningModeEnum.COOL.label));
            StateDescription stateDescription = StateDescriptionFragmentBuilder.create().withOptions(options).build()
                    .toStateDescription();
            channels.put(tempChannel, stateDescription);
        }

        return channels;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            String id = channelUID.getIdWithoutGroup();
            if (id.equals(CHANNEL_ID_THERMOSTAT_SYSTEMMODE)) {
                handler.writeAttribute(endpointNumber, ThermostatCluster.CLUSTER_NAME, "systemMode",
                        command.toString());
                return;
            }
            if (id.equals(CHANNEL_ID_THERMOSTAT_OCCUPIEDHEATING)) {
                handler.writeAttribute(endpointNumber, ThermostatCluster.CLUSTER_NAME, "occupiedHeatingSetpoint",
                        String.valueOf(ValueUtils.temperatureToValue(command)));
                return;
            }
            if (id.equals(CHANNEL_ID_THERMOSTAT_OCCUPIEDCOOLING)) {
                handler.writeAttribute(endpointNumber, ThermostatCluster.CLUSTER_NAME, "occupiedCoolingSetpoint",
                        String.valueOf(ValueUtils.temperatureToValue(command)));
                return;
            }
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        logger.debug("OnEvent: {}", message.path.attributeName);
        Integer numberValue = message.value instanceof Number number ? number.intValue() : 0;
        switch (message.path.attributeName) {
            case ThermostatCluster.ATTRIBUTE_SYSTEM_MODE:
                if (message.value instanceof ThermostatCluster.SystemModeEnum systemModeEnum) {
                    updateState(CHANNEL_ID_THERMOSTAT_SYSTEMMODE, new DecimalType(systemModeEnum.value));
                }
                break;
            case ThermostatCluster.ATTRIBUTE_OCCUPIED_HEATING_SETPOINT:
                updateState(CHANNEL_ID_THERMOSTAT_OCCUPIEDHEATING, ValueUtils.valueToTemperature(numberValue));
                break;
            case ThermostatCluster.ATTRIBUTE_OCCUPIED_COOLING_SETPOINT:
                updateState(CHANNEL_ID_THERMOSTAT_OCCUPIEDCOOLING, ValueUtils.valueToTemperature(numberValue));
                break;
            case ThermostatCluster.ATTRIBUTE_UNOCCUPIED_HEATING_SETPOINT:
                updateState(CHANNEL_ID_THERMOSTAT_UNOCCUPIEDHEATING, ValueUtils.valueToTemperature(numberValue));
                break;
            case ThermostatCluster.ATTRIBUTE_UNOCCUPIED_COOLING_SETPOINT:
                updateState(CHANNEL_ID_THERMOSTAT_UNOCCUPIEDCOOLING, ValueUtils.valueToTemperature(numberValue));
                break;
            case ThermostatCluster.ATTRIBUTE_LOCAL_TEMPERATURE:
                updateState(CHANNEL_ID_THERMOSTAT_LOCALTEMPERATURE, ValueUtils.valueToTemperature(numberValue));
                break;
            case ThermostatCluster.ATTRIBUTE_OUTDOOR_TEMPERATURE:
                updateState(CHANNEL_ID_THERMOSTAT_OUTDOORTEMPERATURE, ValueUtils.valueToTemperature(numberValue));
                break;
            case ThermostatCluster.ATTRIBUTE_THERMOSTAT_RUNNING_MODE:
                updateState(CHANNEL_ID_THERMOSTAT_RUNNINGMODE, new DecimalType(numberValue));
                break;
            default:
                logger.debug("Unknown attribute {}", message.path.attributeName);
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_THERMOSTAT_LOCALTEMPERATURE,
                initializingCluster.localTemperature != null
                        ? ValueUtils.valueToTemperature(initializingCluster.localTemperature)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THERMOSTAT_OUTDOORTEMPERATURE,
                initializingCluster.outdoorTemperature != null
                        ? ValueUtils.valueToTemperature(initializingCluster.outdoorTemperature)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THERMOSTAT_SYSTEMMODE,
                initializingCluster.systemMode != null ? new DecimalType(initializingCluster.systemMode.value)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THERMOSTAT_OCCUPIEDHEATING,
                initializingCluster.occupiedHeatingSetpoint != null
                        ? ValueUtils.valueToTemperature(initializingCluster.occupiedHeatingSetpoint)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THERMOSTAT_OCCUPIEDCOOLING,
                initializingCluster.occupiedCoolingSetpoint != null
                        ? ValueUtils.valueToTemperature(initializingCluster.occupiedCoolingSetpoint)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THERMOSTAT_UNOCCUPIEDHEATING,
                initializingCluster.unoccupiedHeatingSetpoint != null
                        ? ValueUtils.valueToTemperature(initializingCluster.unoccupiedHeatingSetpoint)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THERMOSTAT_UNOCCUPIEDCOOLING,
                initializingCluster.unoccupiedCoolingSetpoint != null
                        ? ValueUtils.valueToTemperature(initializingCluster.unoccupiedCoolingSetpoint)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THERMOSTAT_RUNNINGMODE,
                initializingCluster.thermostatRunningMode != null
                        ? new DecimalType(initializingCluster.thermostatRunningMode.value)
                        : UnDefType.NULL);
    }
}
