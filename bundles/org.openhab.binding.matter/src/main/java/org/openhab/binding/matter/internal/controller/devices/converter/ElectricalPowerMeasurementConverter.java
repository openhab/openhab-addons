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
import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ElectricalPowerMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

/**
 * A converter for translating {@link ElectricalPowerMeasurementCluster} events and attributes to openHAB channels and
 * back again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ElectricalPowerMeasurementConverter extends GenericConverter<ElectricalPowerMeasurementCluster> {

    public ElectricalPowerMeasurementConverter(ElectricalPowerMeasurementCluster cluster,
            MatterBaseThingHandler handler, int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Map<Channel, @Nullable StateDescription> map = new HashMap<>();
        // Active Power is mandatory
        Channel activePowerChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_ACTIVEPOWER),
                        "Number:Power")
                .withType(CHANNEL_ELECTRICALPOWERMEASUREMENT_ACTIVEPOWER).build();
        map.put(activePowerChannel, null);

        // optional cluster if not null
        if (initializingCluster.activeCurrent != null) {
            Channel activeCurrentChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_ACTIVECURRENT),
                            "Number:ElectricCurrent")
                    .withType(CHANNEL_ELECTRICALPOWERMEASUREMENT_ACTIVECURRENT).build();
            map.put(activeCurrentChannel, null);
        }

        // optional cluster if not null
        if (initializingCluster.voltage != null) {
            Channel voltageChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_VOLTAGE),
                            "Number:ElectricPotential")
                    .withType(CHANNEL_ELECTRICALPOWERMEASUREMENT_VOLTAGE).build();
            map.put(voltageChannel, null);
        }
        return map;
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case ElectricalPowerMeasurementCluster.ATTRIBUTE_ACTIVE_POWER:
                updateState(CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_ACTIVEPOWER,
                        message.value instanceof Number number ? activePowerToWatts(number) : UnDefType.UNDEF);
                break;
            case ElectricalPowerMeasurementCluster.ATTRIBUTE_ACTIVE_CURRENT:
                updateState(CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_ACTIVECURRENT,
                        message.value instanceof Number number ? activeCurrentToAmps(number) : UnDefType.UNDEF);
                break;
            case ElectricalPowerMeasurementCluster.ATTRIBUTE_VOLTAGE:
                updateState(CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_VOLTAGE,
                        message.value instanceof Number number ? milliVoltsToVolts(number) : UnDefType.UNDEF);
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_ACTIVEPOWER,
                initializingCluster.activePower != null ? activePowerToWatts(initializingCluster.activePower)
                        : UnDefType.NULL);
        // optional cluster
        if (initializingCluster.activeCurrent != null) {
            updateState(CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_ACTIVECURRENT,
                    initializingCluster.activeCurrent != null ? activeCurrentToAmps(initializingCluster.activeCurrent)
                            : UnDefType.NULL);
        }
        if (initializingCluster.voltage != null) {
            updateState(CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_VOLTAGE,
                    initializingCluster.voltage != null ? milliVoltsToVolts(initializingCluster.voltage)
                            : UnDefType.NULL);
        }
    }

    private QuantityType<Power> activePowerToWatts(Number number) {
        return new QuantityType<Power>(new BigDecimal(number.intValue() / 1000), Units.WATT);
    }

    private QuantityType<ElectricCurrent> activeCurrentToAmps(Number number) {
        return new QuantityType<ElectricCurrent>(new BigDecimal(number.intValue() / 1000), Units.AMPERE);
    }

    private QuantityType<ElectricPotential> milliVoltsToVolts(Number number) {
        return new QuantityType<ElectricPotential>(new BigDecimal(number.intValue() / 1000), Units.VOLT);
    }
}
