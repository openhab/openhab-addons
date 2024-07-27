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
package org.openhab.binding.mercedesme.internal;

import static org.openhab.binding.mercedesme.internal.Constants.*;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;

/**
 * {@link MercedesMeMetadataAdjuster} changes Metadata for channels not providing the system default unit
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MercedesMeMetadataAdjuster implements RegistryChangeListener<ItemChannelLink> {
    private final MetadataRegistry metadataRegistry;
    private final ItemChannelLinkRegistry channelLinkRegistry;
    private final UnitProvider unitProvider;

    public MercedesMeMetadataAdjuster(MetadataRegistry mdr, ItemChannelLinkRegistry iclr, UnitProvider up) {
        metadataRegistry = mdr;
        channelLinkRegistry = iclr;
        unitProvider = up;
        channelLinkRegistry.addRegistryChangeListener(this);
    }

    /**
     * Adjust Units to binding defaults
     */
    @Override
    public void added(ItemChannelLink element) {
        ChannelUID cuid = element.getLinkedUID();
        String itemName = element.getItemName();
        if (Constants.BINDING_ID.equals(cuid.getBindingId())) {
            MetadataKey key = new MetadataKey("unit", itemName);
            switch (cuid.getId()) {
                case GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_MILEAGE:
                case GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_RANGE_ELECTRIC:
                case GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_RADIUS_ELECTRIC:
                case GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_RANGE_FUEL:
                case GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_RADIUS_FUEL:
                case GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_RANGE_HYBRID:
                case GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_RADIUS_HYBRID:
                case GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_HOME_DISTANCE:
                case GROUP_TRIP + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_DISTANCE:
                case GROUP_TRIP + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_DISTANCE_RESET:
                case GROUP_ECO + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_BONUS_RANGE:
                    if (metadataRegistry.get(key) == null) {
                        Unit<Length> lengthUnit = unitProvider.getUnit(Length.class);
                        if (ImperialUnits.FOOT.equals(lengthUnit)) {
                            metadataRegistry.add(new Metadata(key, ImperialUnits.MILE.getSymbol(), null));
                        } else if (SIUnits.METRE.equals(lengthUnit)) {
                            metadataRegistry.add(new Metadata(key, Constants.KILOMETRE_UNIT.toString(), null));
                        }
                    }
                    break;
                case GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_SOC:
                case GROUP_CHARGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_MAX_SOC:
                case GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_FUEL_LEVEL:
                case GROUP_ECO + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_ACCEL:
                case GROUP_ECO + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_COASTING:
                case GROUP_ECO + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_CONSTANT:
                    if (metadataRegistry.get(key) == null) {
                        metadataRegistry.add(new Metadata(key, Units.PERCENT.getSymbol(), null));
                    }
                    break;
                case GROUP_TIRES + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_PRESSURE_FRONT_LEFT:
                case GROUP_TIRES + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_PRESSURE_FRONT_RIGHT:
                case GROUP_TIRES + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_PRESSURE_REAR_LEFT:
                case GROUP_TIRES + ChannelUID.CHANNEL_GROUP_SEPARATOR + OH_CHANNEL_PRESSURE_REAR_RIGHT:
                    if (metadataRegistry.get(key) == null) {
                        Unit<Length> lengthUnit = unitProvider.getUnit(Length.class);
                        if (ImperialUnits.FOOT.equals(lengthUnit)) {
                            metadataRegistry
                                    .add(new Metadata(key, ImperialUnits.POUND_FORCE_SQUARE_INCH.getSymbol(), null));
                        } else if (SIUnits.METRE.equals(lengthUnit)) {
                            metadataRegistry.add(new Metadata(key, Units.BAR.getSymbol(), null));
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void removed(ItemChannelLink element) {
    }

    @Override
    public void updated(ItemChannelLink oldElement, ItemChannelLink element) {
    }
}
