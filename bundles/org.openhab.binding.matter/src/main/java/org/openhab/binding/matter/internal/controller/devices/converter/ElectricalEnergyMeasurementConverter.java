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

import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ELECTRICALENERGYMEASUREMENT_ENERGYMEASUREMENT_ENERGY;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYEXPORTED_ENERGY;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYIMPORTED_ENERGY;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYEXPORTED_ENERGY;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYIMPORTED_ENERGY;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYEXPORTED_ENERGY;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYIMPORTED_ENERGY;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYEXPORTED_ENERGY;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYIMPORTED_ENERGY;
import static org.openhab.binding.matter.internal.MatterBindingConstants.ITEM_TYPE_NUMBER_ENERGY;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ElectricalEnergyMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.StateDescription;

/**
 * The {@link TemperatureMeasurementConverter}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ElectricalEnergyMeasurementConverter extends GenericConverter<ElectricalEnergyMeasurementCluster> {

    public ElectricalEnergyMeasurementConverter(ElectricalEnergyMeasurementCluster cluster,
            MatterBaseThingHandler handler, int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID thingUID) {
        Map<Channel, @Nullable StateDescription> map = new HashMap<>();
        if (initializingCluster.featureMap.cumulativeEnergy) {
            if (initializingCluster.featureMap.exportedEnergy) {
                Channel exportedEnergyChannel = ChannelBuilder
                        .create(new ChannelUID(thingUID,
                                CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYEXPORTED_ENERGY),
                                ITEM_TYPE_NUMBER_ENERGY)
                        .withType(CHANNEL_ELECTRICALENERGYMEASUREMENT_ENERGYMEASUREMENT_ENERGY)
                        .withLabel(
                                formatLabel(CHANNEL_LABEL_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYEXPORTED_ENERGY))
                        .build();
                map.put(exportedEnergyChannel, null);
            }

            if (initializingCluster.featureMap.importedEnergy) {
                Channel importedEnergyChannel = ChannelBuilder
                        .create(new ChannelUID(thingUID,
                                CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYIMPORTED_ENERGY),
                                ITEM_TYPE_NUMBER_ENERGY)
                        .withType(CHANNEL_ELECTRICALENERGYMEASUREMENT_ENERGYMEASUREMENT_ENERGY)
                        .withLabel(
                                formatLabel(CHANNEL_LABEL_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYIMPORTED_ENERGY))
                        .build();
                map.put(importedEnergyChannel, null);
            }
        }
        if (initializingCluster.featureMap.periodicEnergy) {
            if (initializingCluster.featureMap.exportedEnergy) {
                Channel exportedEnergyChannel = ChannelBuilder
                        .create(new ChannelUID(thingUID,
                                CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYEXPORTED_ENERGY),
                                ITEM_TYPE_NUMBER_ENERGY)
                        .withType(CHANNEL_ELECTRICALENERGYMEASUREMENT_ENERGYMEASUREMENT_ENERGY)
                        .withLabel(formatLabel(CHANNEL_LABEL_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYEXPORTED_ENERGY))
                        .build();
                map.put(exportedEnergyChannel, null);
            }
            if (initializingCluster.featureMap.importedEnergy) {
                Channel importedEnergyChannel = ChannelBuilder
                        .create(new ChannelUID(thingUID,
                                CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYIMPORTED_ENERGY),
                                ITEM_TYPE_NUMBER_ENERGY)
                        .withType(CHANNEL_ELECTRICALENERGYMEASUREMENT_ENERGYMEASUREMENT_ENERGY)
                        .withLabel(formatLabel(CHANNEL_LABEL_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYIMPORTED_ENERGY))
                        .build();
                map.put(importedEnergyChannel, null);
            }
        }
        return map;
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        if (message.value instanceof ElectricalEnergyMeasurementCluster.EnergyMeasurementStruct energyMeasurement) {
            switch (message.path.attributeName) {
                case ElectricalEnergyMeasurementCluster.ATTRIBUTE_CUMULATIVE_ENERGY_IMPORTED: {
                    if (energyMeasurement.energy != null) {
                        updateState(CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYIMPORTED_ENERGY,
                                activePowerToWatts(energyMeasurement.energy));
                    }
                }
                    break;
                case ElectricalEnergyMeasurementCluster.ATTRIBUTE_CUMULATIVE_ENERGY_EXPORTED: {
                    if (energyMeasurement.energy != null) {
                        updateState(CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYIMPORTED_ENERGY,
                                activePowerToWatts(energyMeasurement.energy));
                    }
                }
                    break;
                case ElectricalEnergyMeasurementCluster.ATTRIBUTE_PERIODIC_ENERGY_IMPORTED: {
                    if (energyMeasurement.energy != null) {
                        updateState(CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYIMPORTED_ENERGY,
                                activePowerToWatts(energyMeasurement.energy));
                    }
                }
                    break;
            }
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        if (initializingCluster.cumulativeEnergyImported != null
                && initializingCluster.cumulativeEnergyImported.energy != null) {
            updateState(CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYIMPORTED_ENERGY,
                    activePowerToWatts(initializingCluster.cumulativeEnergyImported.energy));
        }
        if (initializingCluster.cumulativeEnergyExported != null
                && initializingCluster.cumulativeEnergyExported.energy != null) {
            updateState(CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYIMPORTED_ENERGY,
                    activePowerToWatts(initializingCluster.cumulativeEnergyExported.energy));
        }
        if (initializingCluster.periodicEnergyImported != null
                && initializingCluster.periodicEnergyImported.energy != null) {
            updateState(CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYIMPORTED_ENERGY,
                    activePowerToWatts(initializingCluster.periodicEnergyImported.energy));
        }
        if (initializingCluster.periodicEnergyExported != null
                && initializingCluster.periodicEnergyExported.energy != null) {
            updateState(CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYIMPORTED_ENERGY,
                    activePowerToWatts(initializingCluster.periodicEnergyExported.energy));
        }
    }

    private QuantityType<Energy> activePowerToWatts(Number number) {
        return new QuantityType<Energy>(new BigDecimal(number.intValue() / 1000), Units.WATT_HOUR);
    }
}
