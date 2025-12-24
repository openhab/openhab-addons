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
package org.openhab.binding.matter.internal.bridge.devices;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.RelativeHumidityMeasurementCluster;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;

/**
 * The {@link HumiditySensorDevice} is a device that represents a Humidity Sensor.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class HumiditySensorDevice extends BaseDevice {
    private static final BigDecimal HUMIDITY_MULTIPLIER = new BigDecimal(100);

    public HumiditySensorDevice(MetadataRegistry metadataRegistry, MatterBridgeClient client, GenericItem item) {
        super(metadataRegistry, client, item);
    }

    @Override
    public String deviceType() {
        return "HumiditySensor";
    }

    @Override
    public void handleMatterEvent(String clusterName, String attributeName, Object data) {
    }

    @Override
    protected MatterDeviceOptions activate() {
        primaryItem.addStateChangeListener(this);
        MetaDataMapping primaryMetadata = metaDataMapping(primaryItem);
        Map<String, Object> attributeMap = primaryMetadata.getAttributeOptions();
        attributeMap.put(
                RelativeHumidityMeasurementCluster.CLUSTER_PREFIX + "."
                        + RelativeHumidityMeasurementCluster.ATTRIBUTE_MEASURED_VALUE,
                toMatterValue(primaryItem.getState()));
        return new MatterDeviceOptions(attributeMap, primaryMetadata.label);
    }

    @Override
    public void dispose() {
        primaryItem.removeStateChangeListener(this);
    }

    @Override
    public void updateState(Item item, State state) {
        setEndpointState(RelativeHumidityMeasurementCluster.CLUSTER_PREFIX,
                RelativeHumidityMeasurementCluster.ATTRIBUTE_MEASURED_VALUE, toMatterValue(state));
    }

    public static int toMatterValue(@Nullable State humidity) {
        BigDecimal value = new BigDecimal(0);
        if (humidity instanceof QuantityType quantityType) {
            value = quantityType.toBigDecimal();
        }
        if (humidity instanceof Number number) {
            value = BigDecimal.valueOf(number.doubleValue());
        }
        return value.setScale(2, RoundingMode.CEILING).multiply(HUMIDITY_MULTIPLIER).intValue();
    }
}
