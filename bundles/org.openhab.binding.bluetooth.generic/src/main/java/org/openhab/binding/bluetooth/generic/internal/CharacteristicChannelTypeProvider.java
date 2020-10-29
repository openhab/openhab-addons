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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.gattparser.spec.Enumerations;
import org.sputnikdev.bluetooth.gattparser.spec.Field;

/**
 * {@link CharacteristicChannelTypeProvider} that provides channel types for dynamically discovered characteristics.
 *
 * @author Vlad Kolotov - Original author
 * @author Connor Petty - Modified for openHAB use.
 */
@NonNullByDefault
public class CharacteristicChannelTypeProvider implements ChannelTypeProvider, ThingHandlerService {

    private static final String CHANNEL_TYPE_NAME_PATTERN = "characteristic-%s-%s-%s-%s";

    private final Logger logger = LoggerFactory.getLogger(CharacteristicChannelTypeProvider.class);

    private final Map<ChannelTypeUID, @Nullable ChannelType> cache = new ConcurrentHashMap<>();

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof GenericBluetoothHandler) {
            ((GenericBluetoothHandler) handler).setChannelTypeProvider(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return null;
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return Collections.emptyList();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return cache.get(channelTypeUID);
    }

    public ChannelTypeUID registerChannelType(String characteristicUUID, boolean advanced, boolean readOnly,
            Field field) {
        // characteristic-advncd-readable-00002a04-0000-1000-8000-00805f9b34fb-Battery_Level
        String channelType = String.format(CHANNEL_TYPE_NAME_PATTERN, advanced ? "advncd" : "simple",
                readOnly ? "readable" : "writable", characteristicUUID, BluetoothChannelUtils.encodeFieldID(field));

        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BluetoothBindingConstants.BINDING_ID, channelType);
        cache.computeIfAbsent(channelTypeUID, uid -> buildChannelType(uid, advanced, readOnly, field));
        return channelTypeUID;
    }

    public void clearRegistry() {
        cache.clear();
    }

    /**
     * Builds a new channel type for a channel type UID.
     * See
     * {@link org.sputnikdev.esh.binding.bluetooth.handler.BluetoothChannelBuilder#buildChannels(URL, List, boolean, boolean)}
     *
     * @param channelTypeUID channel type UID
     * @return new channel type
     */
    private ChannelType buildChannelType(ChannelTypeUID channelTypeUID, boolean advanced, boolean readOnly,
            Field field) {
        List<StateOption> options = getStateOptions(field);
        String itemType = BluetoothChannelUtils.getItemType(field);

        if (itemType == null) {
            throw new IllegalStateException("Unknown field format type: " + field.getUnit());
        }

        if (itemType.equals("Switch")) {
            options = Collections.emptyList();
        }

        StateDescriptionFragmentBuilder stateDescBuilder = StateDescriptionFragmentBuilder.create()//
                .withPattern(getPattern(field))//
                .withReadOnly(readOnly)//
                .withOptions(options);

        BigDecimal min = toBigDecimal(field.getMinimum());
        BigDecimal max = toBigDecimal(field.getMaximum());
        if (min != null) {
            stateDescBuilder = stateDescBuilder.withMinimum(min);
        }
        if (max != null) {
            stateDescBuilder = stateDescBuilder.withMaximum(max);
        }
        return ChannelTypeBuilder.state(channelTypeUID, field.getName(), itemType)//
                .isAdvanced(advanced)//
                .withDescription(field.getInformativeText())//
                .withStateDescriptionFragment(stateDescBuilder.build()).build();
    }

    private static String getPattern(Field field) {
        String format = getFormat(field);
        String unit = getUnit(field);
        StringBuilder pattern = new StringBuilder();
        pattern.append(format);
        if (unit != null) {
            pattern.append(" ").append(unit);
        }
        return pattern.toString();
    }

    private static List<StateOption> getStateOptions(Field field) {
        return Optional.ofNullable(field.getEnumerations())//
                .map(Enumerations::getEnumerations)//
                .stream()//
                .flatMap(List::stream)
                .map(enumeration -> new StateOption(String.valueOf(enumeration.getKey()), enumeration.getValue()))
                .collect(Collectors.toList());
    }

    private static @Nullable BigDecimal toBigDecimal(@Nullable Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
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
