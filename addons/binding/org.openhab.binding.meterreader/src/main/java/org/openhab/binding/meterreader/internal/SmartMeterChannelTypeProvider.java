/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal;

import java.net.URI;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.StateChannelTypeBuilder;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.util.UnitUtils;
import org.openhab.binding.meterreader.MeterReaderBindingConstants;
import org.osgi.service.component.annotations.Component;

/**
 * A {@link ChannelTypeProvider} that listens for changes to the {@link MeterDevice} and updates the
 * {@link ChannelType}s according to all available OBIS values.
 * It creates one {@link ChannelType} per available OBIS value.
 *
 * @author MatthiasS
 *
 */
@Component(service = { ChannelTypeProvider.class, SmartMeterChannelTypeProvider.class })
public class SmartMeterChannelTypeProvider implements ChannelTypeProvider, MeterValueListener {

    private Map<String, ChannelType> obisChannelMap = new ConcurrentHashMap<>();

    @Override
    public @NonNull Collection<@NonNull ChannelType> getChannelTypes(@Nullable Locale locale) {
        return obisChannelMap.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return obisChannelMap.values().stream().filter(channelType -> channelType.getUID().equals(channelTypeUID))
                .findFirst().orElse(null);
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID,
            @Nullable Locale locale) {
        return null;
        // getChannelGroupTypes(locale).stream().filter(group -> group.getUID().equals(channelGroupTypeUID))
        // .findFirst().orElse(null);
    }

    @Override
    public @Nullable Collection<@NonNull ChannelGroupType> getChannelGroupTypes(@Nullable Locale locale) {
        // return getChannelTypes(null).stream().collect(Collectors.groupingBy(ChannelType::getItemType)).entrySet()
        // .stream().map(entry -> {
        // String typeExtension = ItemUtil.getItemTypeExtension(entry.getKey());
        // if (typeExtension == null) {
        // typeExtension = "others";
        // }
        // List<ChannelDefinition> channelDefs = entry.getValue().stream()
        // .map(channel -> new ChannelDefinition(channel.getUID().getId(), channel.getUID()))
        // .collect(Collectors.toList());
        // return ChannelGroupTypeBuilder
        // .instance(new ChannelGroupTypeUID(MeterReaderBindingConstants.BINDING_ID, typeExtension),
        // typeExtension)
        // .withChannelDefinitions(channelDefs).build();
        // }).collect(Collectors.toList());
        return null;
    }

    @Override
    public void errorOccoured(Throwable e) {
        // TODO Auto-generated method stub

    }

    @Override
    public <Q extends @NonNull Quantity<Q>> void valueChanged(MeterValue<Q> value) {
        if (!obisChannelMap.containsKey(value.getObisCode())) {
            obisChannelMap.put(value.getObisCode(), getChannelType(value.getUnit(), value.getObisCode()));
        }
    }

    private ChannelType getChannelType(Unit<?> unit, String obis) {

        String obisChannelId = MeterReaderBindingConstants.getObisChannelId(obis);
        StateChannelTypeBuilder stateDescriptionBuilder;
        if (unit != null) {
            String dimension = UnitUtils.getDimensionName(unit);
            stateDescriptionBuilder = ChannelTypeBuilder
                    .state(new ChannelTypeUID(MeterReaderBindingConstants.BINDING_ID, obisChannelId), obis,
                            CoreItemFactory.NUMBER + ":" + dimension)
                    .withStateDescription(StateDescriptionFragmentBuilder.create().withReadOnly(true)
                            .withPattern("%.2f %unit%").build().toStateDescription())
                    .withConfigDescriptionURI(URI.create(MeterReaderBindingConstants.CHANNEL_TYPE_METERREADER_OBIS));
        } else {
            stateDescriptionBuilder = ChannelTypeBuilder
                    .state(new ChannelTypeUID(MeterReaderBindingConstants.BINDING_ID, obisChannelId), obis,
                            CoreItemFactory.STRING)
                    .withStateDescription(
                            StateDescriptionFragmentBuilder.create().withReadOnly(true).build().toStateDescription());

        }
        return stateDescriptionBuilder.build();
    }

    @Override
    public <Q extends @NonNull Quantity<Q>> void valueRemoved(MeterValue<Q> value) {
        obisChannelMap.remove(value.getObisCode());
    }

    public ChannelTypeUID getChannelTypeIdForObis(String obis) {
        return obisChannelMap.get(obis).getUID();
    }

}
