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
package org.openhab.binding.mercedesme.internal.utils;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MetadataAdjuster} changes Metadata for channels not providing the system default unit
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MetadataAdjuster implements RegistryChangeListener<ItemChannelLink> {
    private final Logger logger = LoggerFactory.getLogger(MetadataAdjuster.class);
    private final MetadataRegistry metadataRegistry;
    private final ItemChannelLinkRegistry channelLinkRegistry;
    private final ThingUID thingUID;

    public MetadataAdjuster(ThingUID tuid, MetadataRegistry mdr, ItemChannelLinkRegistry iclr) {
        metadataRegistry = mdr;
        channelLinkRegistry = iclr;
        thingUID = tuid;
        channelLinkRegistry.addRegistryChangeListener(this);
    }

    /**
     * Adjust Units to binding defaults
     */
    @Override
    public void added(ItemChannelLink element) {
        ChannelUID cuid = element.getLinkedUID();
        String itemName = element.getItemName();
        if (thingUID.equals(cuid.getThingUID())) {
            MetadataKey key = new MetadataKey("unit", itemName);
            switch (cuid.getId()) {
                case Constants.GROUP_RANGE + "#mileage":
                case Constants.GROUP_RANGE + "#range-electric":
                case Constants.GROUP_RANGE + "#radius-electric":
                case Constants.GROUP_RANGE + "#range-fuel":
                case Constants.GROUP_RANGE + "#radius-fuel":
                case Constants.GROUP_RANGE + "#range-hybrid":
                case Constants.GROUP_RANGE + "#radius-hybrid":
                case Constants.GROUP_RANGE + "#home-distance":
                case Constants.GROUP_TRIP + "#distance":
                case Constants.GROUP_TRIP + "#distance-reset":
                    if (metadataRegistry.get(key) == null) {
                        logger.info("{} not found", key);
                        if (Locale.US.getCountry().equals(Utils.getCountry())) {
                            metadataRegistry.add(new Metadata(key, ImperialUnits.MILE.getSymbol(), null));
                        } else {
                            logger.info("Set {} to {}", key, Constants.KILOMETRE_UNIT.toString());
                            metadataRegistry.add(new Metadata(key, Constants.KILOMETRE_UNIT.toString(), null));
                        }
                    } else {
                        logger.info("{} already set: {}", key, metadataRegistry.get(key));
                    }
                    break;
                case Constants.GROUP_RANGE + "#soc":
                case Constants.GROUP_RANGE + "#fuel-level":
                    if (metadataRegistry.get(key) == null) {
                        logger.info("{} not found", key);
                        metadataRegistry.add(new Metadata(key, Units.PERCENT.getSymbol(), null));
                    } else {
                        logger.info("{} already set: {}", key, metadataRegistry.get(key));
                    }
                    break;
                case Constants.GROUP_TIRES + "#pressure-front-left":
                case Constants.GROUP_TIRES + "#pressure-front-right":
                case Constants.GROUP_TIRES + "#pressure-rear-left":
                case Constants.GROUP_TIRES + "#pressure-rear-right":
                    if (metadataRegistry.get(key) == null) {
                        logger.info("{} not found", key);
                        if (Locale.US.getCountry().equals(Utils.getCountry())) {
                            metadataRegistry
                                    .add(new Metadata(key, ImperialUnits.POUND_FORCE_SQUARE_INCH.getSymbol(), null));
                        } else {
                            metadataRegistry.add(new Metadata(key, Units.BAR.getSymbol(), null));
                        }
                    } else {
                        logger.info("{} already set: {}", key, metadataRegistry.get(key));
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
