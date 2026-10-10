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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.api.dto.ServiceCall;
import org.openhab.binding.hasslink.internal.entity.ChannelSpec;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.entity.ParsedData;
import org.openhab.binding.hasslink.internal.entity.util.ChannelSpecsBuilder;
import org.openhab.binding.hasslink.internal.entity.util.CommandMapper;
import org.openhab.binding.hasslink.internal.entity.util.ImageUtils;
import org.openhab.binding.hasslink.internal.entity.util.StateMapBuilder;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;

/**
 * The {@link CameraEntity} class represents a camera entity type in the Home Assistant binding.
 * Exposes camera operational state (String), power toggle (Switch), and snapshot images (Image).
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class CameraEntity implements EntityType {

    private static final long ON_OFF = 1L;
    private static final long STREAM = 2L;

    @Override
    public String getType() {
        return "camera";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        ChannelSpecsBuilder builder = ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.STRING) //
                .addIfSupported(ON_OFF, "power", ItemType.SWITCH, ChannelKind.STATE, "Camera Control", null, Set.of(),
                        null) //
                .add("image", ItemType.IMAGE);

        if (entityState.isSupportedFeature(STREAM)) {
            builder.add("stream_url", ItemType.STRING);
        }

        return builder.build();
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {

        StateMapBuilder builder = StateMapBuilder.create(entityState, context) //
                .putPrimaryStringState() //
                .putOnOffValue("power", !"idle".equalsIgnoreCase(entityState.state()));

        HassLinkBridgeHandler bridgeHandler = context.bridgeHandler();
        if (entityState.isSupportedFeature(STREAM) && context.isLinked("stream_url") && bridgeHandler != null) {
            String accessToken = entityState.getAttributeAsString("access_token");
            String streamPath = "/api/camera_proxy_stream/" + entityState.entityId()
                    + (accessToken == null || accessToken.isBlank() ? "" : "?token=" + accessToken);

            String fullStreamUrl = bridgeHandler.getRestBaseUri() + streamPath;
            builder.putStringValue("stream_url", fullStreamUrl);
        }

        Map<String, ParsedData> states = builder.build();

        if (context.isLinked("image")) {
            String imageSource = ImageUtils.extractImageSource(entityState);
            ImageUtils.processImageChannel(entityState, "image", imageSource, states, context);
        }

        return states;
    }

    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String attribute, Command command,
            @Nullable EntityState entityState, EntityContext context) {
        return switch (attribute) {
            case "power" -> CommandMapper.onOff(command, getType(), entityId);
            default -> Optional.empty();
        };
    }
}
