/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hasslink.internal.entity.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.ChannelSpec;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.entity.ParsedData;
import org.openhab.binding.hasslink.internal.entity.util.ChannelSpecsBuilder;
import org.openhab.binding.hasslink.internal.entity.util.ImageUtils;

/**
 * The {@link ImageEntity} class represents an {@code image} entity type in the Home Assistant binding.
 * Maps Home Assistant image data (inline base64 data URIs or fetched remote URLs) to an openHAB Image channel.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class ImageEntity implements EntityType {

    @Override
    public String getType() {
        return "image";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.IMAGE) //
                .build();
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        Map<String, ParsedData> states = new HashMap<>();

        if (context.isLinked(EntityType.PRIMARY_ATTR)) {
            String imageSource = ImageUtils.extractImageSource(entityState);
            ImageUtils.processImageChannel(entityState, EntityType.PRIMARY_ATTR, imageSource, states, context);
        }

        return states;
    }
}
