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
package org.openhab.binding.solarman.internal.typeprovider;

import static org.openhab.binding.solarman.internal.typeprovider.ChannelUtils.getItemType;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarman.internal.DefinitionParser;
import org.openhab.binding.solarman.internal.defmodel.InverterDefinition;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Catalin Sanda - Initial contribution
 */
@Component(service = { ChannelTypeProvider.class, SolarmanChannelTypeProvider.class })
@NonNullByDefault
public class SolarmanChannelTypeProvider implements ChannelTypeProvider {
    private final Logger logger = LoggerFactory.getLogger(SolarmanChannelTypeProvider.class);
    private static final DefinitionParser DEFINITION_PARSER = new DefinitionParser();
    private static final Pattern INVERTER_DEFINITION_PATTERN = Pattern.compile("/definitions/([^.]+)\\.yaml");
    private final Map<ChannelTypeUID, ChannelType> channelTypeMap = new ConcurrentHashMap<>();

    @Activate
    public SolarmanChannelTypeProvider(BundleContext bundleContext) {
        Collections.list(bundleContext.getBundle().findEntries("/definitions", "*", false)).stream().map(URL::getFile)
                .map(this::extractInverterDefinitionId).filter(Optional::isPresent).map(Optional::get)
                .map(this::parseInverterDefinition).forEach(channelTypeMap::putAll);
    }

    private Map<ChannelTypeUID, ChannelType> parseInverterDefinition(String inverterDefinitionId) {
        InverterDefinition inverterDefinition = DEFINITION_PARSER.parseDefinition(inverterDefinitionId);

        if (inverterDefinition == null) {
            logger.warn("Unable to parse inverter definition");
            return Collections.emptyMap();
        }

        return inverterDefinition.getParameters().stream()
                .flatMap(parameter -> parameter.getItems().stream().map(item -> {
                    ChannelTypeUID channelTypeUID = ChannelUtils.computeChannelTypeId(inverterDefinitionId,
                            parameter.getGroup(), item.getName());
                    return new AbstractMap.SimpleEntry<>(channelTypeUID, buildChannelType(channelTypeUID, item));
                })).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Optional<String> extractInverterDefinitionId(String file) {
        return Stream.of(file).map(INVERTER_DEFINITION_PATTERN::matcher).filter(Matcher::matches)
                .map(matcher -> matcher.group(1)).findFirst();
    }

    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return List.copyOf(this.channelTypeMap.values());
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return this.channelTypeMap.get(channelTypeUID);
    }

    public ChannelType buildChannelType(ChannelTypeUID channelTypeUID, ParameterItem item) {
        String itemType = getItemType(item);

        StateDescriptionFragmentBuilder stateDescriptionFragmentBuilder = StateDescriptionFragmentBuilder.create()
                .withPattern(computePatternForItem(item)).withReadOnly(true);

        StateChannelTypeBuilder stateChannelTypeBuilder = ChannelTypeBuilder
                .state(channelTypeUID, item.getName(), itemType)
                .withConfigDescriptionURI(URI.create("channel-type-config:solarman:dynamic-channel"))
                .withDescription(String.format("%s %s", item.getName(), buildRegisterDescription(item)))
                .withStateDescriptionFragment(stateDescriptionFragmentBuilder.build());

        return stateChannelTypeBuilder.build();
    }

    private String computePatternForItem(ParameterItem item) {
        long decimalPoints = 0;

        BigDecimal scale = Objects.requireNonNullElse(item.getScale(), BigDecimal.ONE);
        if (scale.compareTo(BigDecimal.ONE) < 0) {
            decimalPoints = Math.abs(Math.round(Math.log10(scale.doubleValue())));
        }

        String uom = item.getUom();
        String pattern = (decimalPoints > 0) ? "%." + decimalPoints + "f" : "%d";
        return pattern + (uom != null && !uom.isBlank() ? " %unit%" : "");
    }

    private String buildRegisterDescription(ParameterItem item) {
        return String.format("[%s]", item.getRegisters().stream().map(register -> String.format("0x%04X", register))
                .collect(Collectors.joining(",")));
    }
}
