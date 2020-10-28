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
package org.openhab.binding.bluetooth.generic.internal;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParser;
import org.sputnikdev.bluetooth.gattparser.spec.Field;

/**
 * {@link CharacteristicChannelTypeProvider} that provides channel types for dynamically discovered characteristics.
 *
 * @author Vlad Kolotov - Original author
 * @author Connor Petty - Modified for openHAB use.
 */
@NonNullByDefault
@Component(service = ChannelTypeProvider.class)
public class CharacteristicChannelTypeProvider implements ChannelTypeProvider {

    private Logger logger = LoggerFactory.getLogger(CharacteristicChannelTypeProvider.class);

    private final Map<ChannelTypeUID, @Nullable ChannelType> cache = new ConcurrentHashMap<>();

    private final BluetoothGattParser gattParser;

    @Activate
    public CharacteristicChannelTypeProvider(@Reference GattParserFactory gattParserFactory) {
        this.gattParser = gattParserFactory.getParser();
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return Collections.emptyList();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        if (CharacteristicChannelType.isValidUID(channelTypeUID)) {
            return cache.computeIfAbsent(channelTypeUID, this::buildChannelType);
        }
        return null;
    }

    /**
     * Builds a new channel type for a channel type UID.
     * See
     * {@link org.sputnikdev.esh.binding.bluetooth.handler.BluetoothChannelBuilder#buildChannels(URL, List, boolean, boolean)}
     *
     * @param channelTypeUID channel type UID
     * @return new channel type
     */
    private @Nullable ChannelType buildChannelType(ChannelTypeUID channelTypeUID) {
        // characteristic-advncd-readable-00002a04-0000-1000-8000-00805f9b34fb-Battery_Level
        CharacteristicChannelType type = CharacteristicChannelType.fromChannelTypeUID(gattParser, channelTypeUID);
        if (type != null) {
            List<StateOption> options = getStateOptions(type.field);
            String itemType = BluetoothChannelUtils.getItemType(type.field);

            if (itemType == null) {
                throw new IllegalStateException("Unknown field format type: " + type.field.getUnit());
            }

            if (itemType.equals("Switch") || itemType.equals("Binary")) {
                options = Collections.emptyList();
            }

            String format = getFormat(type.field);
            String unit = getUnit(type.field);
            String pattern = format + " " + (unit != null ? unit : "");

            StateDescriptionFragmentBuilder stateDescBuilder = StateDescriptionFragmentBuilder.create()
                    .withPattern(pattern)//
                    .withReadOnly(type.readOnly)//
                    .withOptions(options);

            BigDecimal min = toBigDecimal(type.field.getMinimum());
            BigDecimal max = toBigDecimal(type.field.getMaximum());
            if (min != null) {
                stateDescBuilder = stateDescBuilder.withMinimum(min);
            }
            if (max != null) {
                stateDescBuilder = stateDescBuilder.withMaximum(max);
            }
            return ChannelTypeBuilder.state(channelTypeUID, type.field.getName(), itemType)//
                    .isAdvanced(type.advanced)//
                    .withDescription(type.field.getInformativeText())//
                    .withStateDescriptionFragment(stateDescBuilder.build()).build();
        }
        return null;
    }

    private static List<StateOption> getStateOptions(Field field) {
        return hasEnums(field)
                ? field.getEnumerations().getEnumerations().stream()
                        .map(enumeration -> new StateOption(String.valueOf(enumeration.getKey()),
                                enumeration.getValue()))
                        .collect(Collectors.toList())
                : Collections.emptyList();
    }

    private static @Nullable BigDecimal toBigDecimal(@Nullable Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    private static boolean hasEnums(Field field) {
        return field.getEnumerations() != null && field.getEnumerations().getEnumerations() != null;
    }

    private static String getFormat(Field field) {
        String format = "%s";
        Integer decimalExponent = field.getDecimalExponent();
        if (field.getFormat().isReal() && decimalExponent != null && decimalExponent < 0) {
            format = "%." + Math.abs(decimalExponent) + "f";
        }
        return format;
    }

    private static @Nullable String getUnit(Field field) {
        String gattUnit = field.getUnit();
        if (gattUnit != null) {
            BluetoothUnit unit = BluetoothUnit.findByType(gattUnit);
            if (unit != null) {
                return unit.getUnit().getSymbol();
            }
        }
        return null;
    }
}
