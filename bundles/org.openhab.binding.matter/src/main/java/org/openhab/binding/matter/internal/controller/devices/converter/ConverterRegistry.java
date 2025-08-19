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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.AirQualityCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BooleanStateCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ColorControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ElectricalEnergyMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ElectricalPowerMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.FanControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.IlluminanceMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.LevelControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ModeSelectCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OccupancySensingCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OnOffCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.PowerSourceCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.RelativeHumidityMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SwitchCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.TemperatureMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThermostatCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadBorderRouterManagementCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadNetworkDiagnosticsCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.WiFiNetworkDiagnosticsCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.WindowCoveringCluster;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;

/**
 * A registry of converters for translating Matter clusters to openHAB channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ConverterRegistry {
    private static final Map<Integer, Class<? extends GenericConverter<? extends BaseCluster>>> CONVERTERS = new HashMap<>();

    static {
        ConverterRegistry.registerConverter(ColorControlCluster.CLUSTER_ID, ColorControlConverter.class);
        ConverterRegistry.registerConverter(LevelControlCluster.CLUSTER_ID, LevelControlConverter.class);
        ConverterRegistry.registerConverter(ModeSelectCluster.CLUSTER_ID, ModeSelectConverter.class);
        ConverterRegistry.registerConverter(OnOffCluster.CLUSTER_ID, OnOffConverter.class);
        ConverterRegistry.registerConverter(SwitchCluster.CLUSTER_ID, SwitchConverter.class);
        ConverterRegistry.registerConverter(ThermostatCluster.CLUSTER_ID, ThermostatConverter.class);
        ConverterRegistry.registerConverter(WindowCoveringCluster.CLUSTER_ID, WindowCoveringConverter.class);
        ConverterRegistry.registerConverter(PowerSourceCluster.CLUSTER_ID, PowerSourceConverter.class);
        ConverterRegistry.registerConverter(FanControlCluster.CLUSTER_ID, FanControlConverter.class);
        ConverterRegistry.registerConverter(RelativeHumidityMeasurementCluster.CLUSTER_ID,
                RelativeHumidityMeasurementConverter.class);
        ConverterRegistry.registerConverter(TemperatureMeasurementCluster.CLUSTER_ID,
                TemperatureMeasurementConverter.class);
        ConverterRegistry.registerConverter(OccupancySensingCluster.CLUSTER_ID, OccupancySensingConverter.class);
        ConverterRegistry.registerConverter(IlluminanceMeasurementCluster.CLUSTER_ID,
                IlluminanceMeasurementConverter.class);
        ConverterRegistry.registerConverter(BooleanStateCluster.CLUSTER_ID, BooleanStateConverter.class);
        ConverterRegistry.registerConverter(WiFiNetworkDiagnosticsCluster.CLUSTER_ID,
                WiFiNetworkDiagnosticsConverter.class);
        ConverterRegistry.registerConverter(DoorLockCluster.CLUSTER_ID, DoorLockConverter.class);
        ConverterRegistry.registerConverter(AirQualityCluster.CLUSTER_ID, AirQualityConverter.class);
        ConverterRegistry.registerConverter(ElectricalPowerMeasurementCluster.CLUSTER_ID,
                ElectricalPowerMeasurementConverter.class);
        ConverterRegistry.registerConverter(ElectricalEnergyMeasurementCluster.CLUSTER_ID,
                ElectricalEnergyMeasurementConverter.class);
        ConverterRegistry.registerConverter(ThreadNetworkDiagnosticsCluster.CLUSTER_ID,
                ThreadNetworkDiagnosticsConverter.class);
        ConverterRegistry.registerConverter(ThreadBorderRouterManagementCluster.CLUSTER_ID,
                ThreadBorderRouterManagementConverter.class);
        // Robotic Vacuum Cleaner converters
        ConverterRegistry.registerConverter(
                org.openhab.binding.matter.internal.client.dto.cluster.gen.RvcRunModeCluster.CLUSTER_ID,
                RvcRunModeConverter.class);
        ConverterRegistry.registerConverter(
                org.openhab.binding.matter.internal.client.dto.cluster.gen.RvcCleanModeCluster.CLUSTER_ID,
                RvcCleanModeConverter.class);
        ConverterRegistry.registerConverter(
                org.openhab.binding.matter.internal.client.dto.cluster.gen.RvcOperationalStateCluster.CLUSTER_ID,
                RvcOperationalStateConverter.class);
        ConverterRegistry.registerConverter(
                org.openhab.binding.matter.internal.client.dto.cluster.gen.ServiceAreaCluster.CLUSTER_ID,
                ServiceAreaConverter.class);
    }

    public static void registerConverter(Integer clusterId,
            Class<? extends GenericConverter<? extends BaseCluster>> converter) {
        CONVERTERS.put(clusterId, converter);
    }

    public static GenericConverter<? extends BaseCluster> createConverter(BaseCluster cluster,
            MatterBaseThingHandler handler, int endpointNumber, String labelPrefix)
            throws ConverterCreationException, NoConverterFoundException {
        Class<? extends GenericConverter<? extends BaseCluster>> clazz = CONVERTERS.get(cluster.id);
        if (clazz == null) {
            throw new NoConverterFoundException("No converter found for cluster " + cluster.id);
        }

        Class<?>[] constructorParameterTypes = new Class<?>[] { cluster.getClass(), MatterBaseThingHandler.class,
                int.class, String.class };
        Constructor<? extends GenericConverter<? extends BaseCluster>> constructor;
        try {
            constructor = clazz.getConstructor(constructorParameterTypes);
            return constructor.newInstance(cluster, handler, endpointNumber, labelPrefix);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            throw new ConverterCreationException("Error creating converter for cluster " + cluster.id, e);
        }
    }

    public static class ConverterCreationException extends Exception {
        private static final long serialVersionUID = 1L;

        public ConverterCreationException(String message, Throwable cause) {
            super(message, cause);
        }

        public ConverterCreationException(String message) {
            super(message);
        }
    }

    public static class NoConverterFoundException extends Exception {
        private static final long serialVersionUID = 1L;

        public NoConverterFoundException(String message) {
            super(message);
        }
    }
}
