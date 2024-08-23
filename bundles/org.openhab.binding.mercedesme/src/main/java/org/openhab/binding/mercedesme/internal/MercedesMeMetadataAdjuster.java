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
                case Constants.GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + "mileage":
                case Constants.GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + "range-electric":
                case Constants.GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + "radius-electric":
                case Constants.GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + "range-fuel":
                case Constants.GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + "radius-fuel":
                case Constants.GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + "range-hybrid":
                case Constants.GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + "radius-hybrid":
                case Constants.GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + "home-distance":
                case Constants.GROUP_TRIP + ChannelUID.CHANNEL_GROUP_SEPARATOR + "distance":
                case Constants.GROUP_TRIP + ChannelUID.CHANNEL_GROUP_SEPARATOR + "distance-reset":
                    if (metadataRegistry.get(key) == null) {
                        Unit<Length> lengthUnit = unitProvider.getUnit(Length.class);
                        if (ImperialUnits.FOOT.equals(lengthUnit)) {
                            metadataRegistry.add(new Metadata(key, ImperialUnits.MILE.getSymbol(), null));
                        } else if (SIUnits.METRE.equals(lengthUnit)) {
                            metadataRegistry.add(new Metadata(key, Constants.KILOMETRE_UNIT.toString(), null));
                        }
                    }
                    break;
                case Constants.GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + "soc":
                case Constants.GROUP_CHARGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + "max-soc":
                case Constants.GROUP_RANGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + "fuel-level":
                    if (metadataRegistry.get(key) == null) {
                        metadataRegistry.add(new Metadata(key, Units.PERCENT.getSymbol(), null));
                    }
                    break;
                case Constants.GROUP_TIRES + ChannelUID.CHANNEL_GROUP_SEPARATOR + "pressure-front-left":
                case Constants.GROUP_TIRES + ChannelUID.CHANNEL_GROUP_SEPARATOR + "pressure-front-right":
                case Constants.GROUP_TIRES + ChannelUID.CHANNEL_GROUP_SEPARATOR + "pressure-rear-left":
                case Constants.GROUP_TIRES + ChannelUID.CHANNEL_GROUP_SEPARATOR + "pressure-rear-right":
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
