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
package org.openhab.binding.modbus.internal;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusChannelTypeProviderTest {

    @Test
    public void extensibleChannelTypesHaveEagerConfigurationSchemas() throws Exception {
        var factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        var builder = factory.newDocumentBuilder();
        try (var bridgeStream = requireNonNull(getClass().getResourceAsStream("/OH-INF/thing/bridge-poller2.xml"));
                var channelsStream = requireNonNull(
                        getClass().getResourceAsStream("/OH-INF/thing/poller2-channels.xml"))) {
            var bridge = builder.parse(bridgeStream);
            var channels = builder.parse(channelsStream);
            var definitions = channels.getElementsByTagName("channel-type");
            var ids = new java.util.HashSet<String>();
            for (int index = 0; index < definitions.getLength(); index++) {
                var type = (org.w3c.dom.Element) definitions.item(index);
                ids.add(type.getAttribute("id"));
                assertEquals(1, type.getElementsByTagName("config-description-ref").getLength());
            }
            String extensible = ((org.w3c.dom.Element) bridge.getElementsByTagName("bridge-type").item(0))
                    .getAttribute("extensible");
            assertEquals(java.util.Set.of(extensible.split(",")), ids);
            try (var configStream = requireNonNull(
                    getClass().getResourceAsStream("/OH-INF/config/poller2-channel.xml"))) {
                var config = builder.parse(configStream);
                assertEquals("channel-type:modbus:poller2-value",
                        ((org.w3c.dom.Element) config.getElementsByTagName("config-description").item(0))
                                .getAttribute("uri"));
                var parameters = config.getElementsByTagName("parameter");
                var required = new java.util.HashSet<String>();
                for (int index = 0; index < parameters.getLength(); index++) {
                    var parameter = (org.w3c.dom.Element) parameters.item(index);
                    if ("true".equals(parameter.getAttribute("required"))) {
                        required.add(parameter.getAttribute("name"));
                    }
                }
                assertEquals(java.util.Set.of("address", "valueType"), required);
            }
        }
    }

    @Test
    public void reusesOneStableGeneratedChannelTypeForEachAcceptedItemType() {
        ModbusChannelTypeProvider provider = new ModbusChannelTypeProvider();

        ChannelTypeUID numberUID = requireNonNull(provider.getGeneratedChannelTypeUID("Number"));
        ChannelTypeUID temperatureUID = requireNonNull(provider.getGeneratedChannelTypeUID("Number:Temperature"));
        ChannelTypeUID reusedNumberUID = requireNonNull(provider.getGeneratedChannelTypeUID("Number"));

        assertEquals(new ChannelTypeUID("modbus", "poller2-generated-number"), numberUID);
        assertEquals(new ChannelTypeUID("modbus", "poller2-generated-number-temperature"), temperatureUID);
        assertEquals(numberUID, reusedNumberUID);
        assertEquals(2, provider.getChannelTypes(null).size());

        ChannelType numberType = requireNonNull(provider.getChannelType(numberUID, null));
        assertSame(numberType, provider.getChannelType(reusedNumberUID, null));
        assertEquals("Number", numberType.getItemType());
    }
}
