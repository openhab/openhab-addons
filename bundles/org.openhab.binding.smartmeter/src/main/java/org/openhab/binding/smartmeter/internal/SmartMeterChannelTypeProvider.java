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
package org.openhab.binding.smartmeter.internal;

import java.net.URI;
import java.util.Locale;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartmeter.SmartMeterBindingConstants;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.binding.AbstractStorageBasedTypeProvider;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.util.UnitUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * A {@link ChannelTypeProvider} that listens for changes to the {@link MeterDevice} and updates the
 * {@link ChannelType}s according to all available OBIS values.
 * It creates one {@link ChannelType} per available OBIS value.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = { ChannelTypeProvider.class, SmartMeterChannelTypeProvider.class })
public class SmartMeterChannelTypeProvider extends AbstractStorageBasedTypeProvider implements MeterValueListener {

    @Activate
    public SmartMeterChannelTypeProvider(@Reference StorageService storageService) {
        super(storageService);
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return getChannelTypes(locale).stream().filter(t -> t.getUID().equals(channelTypeUID)).findFirst().orElse(null);
    }

    @Override
    public void errorOccurred(Throwable e) {
        // Nothing to do if there is a reading error...
    }

    @Override
    public <Q extends Quantity<Q>> void valueChanged(MeterValue<Q> value) {
        ChannelType channelType = getChannelType(value.getUnit(), value.getObisCode());
        if (getChannelType(channelType.getUID(), null) == null) {
            putChannelType(channelType);
        }
    }

    private ChannelType getChannelType(@Nullable Unit<?> unit, String obis) {
        String obisChannelId = SmartMeterBindingConstants.getObisChannelId(obis);
        StateChannelTypeBuilder stateChannelTypeBuilder;
        if (unit != null) {
            String dimension = UnitUtils.getDimensionName(unit);
            stateChannelTypeBuilder = ChannelTypeBuilder
                    .state(new ChannelTypeUID(SmartMeterBindingConstants.BINDING_ID, obisChannelId), obis,
                            CoreItemFactory.NUMBER + ":" + dimension)
                    .withStateDescriptionFragment(StateDescriptionFragmentBuilder.create().withReadOnly(true)
                            .withPattern("%.2f %unit%").build())
                    .withConfigDescriptionURI(URI.create(SmartMeterBindingConstants.CHANNEL_TYPE_METERREADER_OBIS));
        } else {
            stateChannelTypeBuilder = ChannelTypeBuilder
                    .state(new ChannelTypeUID(SmartMeterBindingConstants.BINDING_ID, obisChannelId), obis,
                            CoreItemFactory.STRING)
                    .withStateDescriptionFragment(StateDescriptionFragmentBuilder.create().withReadOnly(true).build());
        }
        return stateChannelTypeBuilder.build();
    }

    @Override
    public <Q extends Quantity<Q>> void valueRemoved(MeterValue<Q> value) {
        String obisChannelId = SmartMeterBindingConstants.getObisChannelId(value.getObisCode());
        removeChannelType(new ChannelTypeUID(SmartMeterBindingConstants.BINDING_ID, obisChannelId));
    }

    /**
     * Gets the {@link ChannelTypeUID} for the given OBIS code.
     *
     * @param obis The obis code.
     * @return The {@link ChannelTypeUID} or null.
     */
    public @Nullable ChannelTypeUID getChannelTypeIdForObis(String obis) {
        String id = SmartMeterBindingConstants.getObisChannelId(obis);
        return getChannelTypes(null).stream().map(t -> t.getUID()).filter(uid -> id.equals(uid.getId())).findFirst()
                .orElse(null);
    }
}
