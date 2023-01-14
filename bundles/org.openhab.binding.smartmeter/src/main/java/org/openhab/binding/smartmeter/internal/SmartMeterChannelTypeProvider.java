/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartmeter.SmartMeterBindingConstants;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.util.UnitUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ChannelTypeProvider} that listens for changes to the {@link MeterDevice} and updates the
 * {@link ChannelType}s according to all available OBIS values.
 * It creates one {@link ChannelType} per available OBIS value.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@Component(service = { ChannelTypeProvider.class, SmartMeterChannelTypeProvider.class })
public class SmartMeterChannelTypeProvider implements ChannelTypeProvider, MeterValueListener {

    private final Logger logger = LoggerFactory.getLogger(SmartMeterChannelTypeProvider.class);

    private final Map<String, ChannelType> obisChannelMap = new ConcurrentHashMap<>();

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return obisChannelMap.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return obisChannelMap.values().stream().filter(channelType -> channelType.getUID().equals(channelTypeUID))
                .findFirst().orElse(null);
    }

    @Override
    public void errorOccurred(Throwable e) {
        // Nothing to do if there is a reading error...
    }

    @Override
    public <Q extends @NonNull Quantity<Q>> void valueChanged(MeterValue<Q> value) {
        if (!obisChannelMap.containsKey(value.getObisCode())) {
            logger.debug("Creating ChannelType for OBIS {}", value.getObisCode());
            obisChannelMap.put(value.getObisCode(), getChannelType(value.getUnit(), value.getObisCode()));
        }
    }

    private ChannelType getChannelType(Unit<?> unit, String obis) {
        String obisChannelId = SmartMeterBindingConstants.getObisChannelId(obis);
        StateChannelTypeBuilder stateDescriptionBuilder;
        if (unit != null) {
            String dimension = UnitUtils.getDimensionName(unit);
            stateDescriptionBuilder = ChannelTypeBuilder
                    .state(new ChannelTypeUID(SmartMeterBindingConstants.BINDING_ID, obisChannelId), obis,
                            CoreItemFactory.NUMBER + ":" + dimension)
                    .withStateDescriptionFragment(StateDescriptionFragmentBuilder.create().withReadOnly(true)
                            .withPattern("%.2f %unit%").build())
                    .withConfigDescriptionURI(URI.create(SmartMeterBindingConstants.CHANNEL_TYPE_METERREADER_OBIS));
        } else {
            stateDescriptionBuilder = ChannelTypeBuilder
                    .state(new ChannelTypeUID(SmartMeterBindingConstants.BINDING_ID, obisChannelId), obis,
                            CoreItemFactory.STRING)
                    .withStateDescriptionFragment(StateDescriptionFragmentBuilder.create().withReadOnly(true).build());
        }
        return stateDescriptionBuilder.build();
    }

    @Override
    public <Q extends @NonNull Quantity<Q>> void valueRemoved(MeterValue<Q> value) {
        obisChannelMap.remove(value.getObisCode());
    }

    /**
     * Gets the {@link ChannelTypeUID} for the given OBIS code.
     *
     * @param obis The obis code.
     * @return The {@link ChannelTypeUID} or null.
     */
    public ChannelTypeUID getChannelTypeIdForObis(String obis) {
        ChannelType channeltype = obisChannelMap.get(obis);
        return channeltype != null ? channeltype.getUID() : null;
    }
}
