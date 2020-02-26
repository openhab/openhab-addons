/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal;

import static org.mockito.Mockito.when;

import java.io.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.openhab.binding.hive.internal.client.*;
import org.openhab.binding.hive.internal.client.dto.*;
import org.openhab.binding.hive.internal.client.feature.Feature;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class TestUtil {
    public static final String UUID_CAFEBABE = "cafebabe-cafe-babe-cafe-babecafebabe";
    public static final String UUID_DEADBEEF = "deadbeef-dead-beef-dead-beefdeadbeef";

    public static final NodeId NODE_ID_CAFEBABE = new NodeId(UUID.fromString(UUID_CAFEBABE));
    public static final NodeId NODE_ID_DEADBEEF = new NodeId(UUID.fromString(UUID_DEADBEEF));

    private TestUtil() {
        throw new AssertionError();
    }

    /**
     * A helper method to get a resource (e.g. a json file) as a String.
     */
    public static String getResourceAsString(final String resource) throws IOException {
        try (final InputStream inputStream = TestUtil.class.getResourceAsStream(resource); final Writer writer = new StringWriter()) {
            final Reader reader = new BufferedReader(new InputStreamReader(inputStream));

            char[] buffer = new char[1024];
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }

            return writer.toString();
        }
    }

    public static Thing createHiveNodeThing(final ThingTypeUID thingTypeUID) {
        final Configuration config = new Configuration();
        config.put("nodeId", UUID.randomUUID().toString());

        return ThingBuilder.create(thingTypeUID, "dummy-thing")
                .withLabel("dummy-thing")
                .withConfiguration(config)
                .build();
    }

    public static <T> SettableFeatureAttribute<T> createSimpleFeatureAttribute(final T value) {
        final Instant time = Instant.now();
        return DefaultFeatureAttribute.<T>builder()
                .displayValue(value)
                .reportedValue(value)
                .reportChangedTime(time)
                .reportReceivedTime(time)
                .build();
    }

    public static <T> FeatureAttributeDto<T> createSimpleFeatureAttributeDto(final T value) {
        final FeatureAttributeDto<T> featureAttributeDto = new FeatureAttributeDto<>();
        featureAttributeDto.reportedValue = value;
        featureAttributeDto.displayValue = value;
        featureAttributeDto.reportChangedTime = new HiveApiInstant(Instant.now());
        featureAttributeDto.reportReceivedTime = new HiveApiInstant(Instant.now());

        return featureAttributeDto;
    }

    public static NodeDto createSimpleNodeDto(final NodeId nodeId) {
        final NodeDto nodeDto = new NodeDto();
        nodeDto.id = nodeId;
        nodeDto.parentNodeId = nodeId;
        nodeDto.name = "My test node";
        nodeDto.nodeType = NodeType.THERMOSTAT;
        nodeDto.protocol = Protocol.ZIGBEE;

        nodeDto.features = new FeaturesDto();
        nodeDto.features.device_management_v1 = new DeviceManagementV1FeatureDto();
        nodeDto.features.device_management_v1.productType = createSimpleFeatureAttributeDto(ProductType.HEATING);

        return nodeDto;
    }

    public static Node getTestNodeWithFeatures(final Map<Class<? extends Feature>, Feature> features) {
        return Node.builder()
                .id(new NodeId(UUID.randomUUID()))
                .name("dummy-node")
                .nodeType(NodeType.THERMOSTAT)
                .productType(ProductType.HEATING)
                .protocol(Protocol.NONE)
                .parentNodeId(new NodeId(UUID.randomUUID()))
                .features(features)
                .build();
    }

    public static void initMockChannel(
            final Thing thingMock,
            final String channelId,
            final Channel channelMock,
            final ChannelUID channelUidMock
    ) {
        when(thingMock.getChannel(channelId)).thenReturn(channelMock);
        when(channelMock.getUID()).thenReturn(channelUidMock);
        when(channelUidMock.getId()).thenReturn(channelId);
    }
}
