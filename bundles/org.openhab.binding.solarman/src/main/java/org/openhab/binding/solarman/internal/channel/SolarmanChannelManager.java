/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.solarman.internal.channel;

import static org.openhab.binding.solarman.internal.SolarmanBindingConstants.DYNAMIC_CHANNEL;
import static org.openhab.binding.solarman.internal.typeprovider.ChannelUtils.escapeName;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarman.internal.defmodel.InverterDefinition;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.binding.solarman.internal.typeprovider.ChannelUtils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Catalin Sanda - Initial contribution
 */
@NonNullByDefault
public class SolarmanChannelManager {
    private final ObjectMapper objectMapper;

    public SolarmanChannelManager() {
        objectMapper = new ObjectMapper();
    }

    public Map<ParameterItem, Channel> generateItemChannelMap(Thing thing, InverterDefinition inverterDefinition) {
        return inverterDefinition.getParameters().stream().flatMap(parameter -> {
            String groupName = escapeName(parameter.getGroup());

            return parameter.getItems().stream().map(item -> {
                String channelId = groupName + "-" + escapeName(item.getName());

                Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), channelId))
                        .withType(ChannelUtils.computeChannelTypeId(inverterDefinition.getInverterDefinitionId(),
                                groupName, item.getName()))
                        .withLabel(item.getName()).withKind(ChannelKind.STATE)
                        .withAcceptedItemType(ChannelUtils.getItemType(item))
                        .withProperties(Map.of(DYNAMIC_CHANNEL, Boolean.TRUE.toString()))
                        .withConfiguration(buildConfigurationFromItem(item)).build();
                return new AbstractMap.SimpleEntry<>(item, channel);
            });
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Configuration buildConfigurationFromItem(ParameterItem item) {
        Configuration configuration = new Configuration();

        BaseChannelConfig baseChannelConfig = new BaseChannelConfig();

        BigDecimal offset = item.getOffset();
        if (offset != null) {
            baseChannelConfig.offset = offset;
        }

        BigDecimal scale = item.getScale();
        if (scale != null) {
            baseChannelConfig.scale = scale;
        }

        if (item.hasLookup() || Boolean.TRUE.equals(item.getIsstr())) {
            // Set 5 for Text (String), when isstr is true or Lookup is present
            baseChannelConfig.rule = 5;
        } else {
            baseChannelConfig.rule = item.getRule();
        }

        baseChannelConfig.registers = convertRegisters(item.getRegisters());
        baseChannelConfig.uom = item.getUom();

        Map<String, Object> configurationMap = objectMapper.convertValue(baseChannelConfig, new TypeReference<>() {
        });

        configurationMap.forEach(configuration::put);

        return configuration;
    }

    private String convertRegisters(List<Integer> registers) {
        return "["
                + registers.stream().map(register -> String.format("0x%04X", register)).collect(Collectors.joining(","))
                + "]";
    }
}
