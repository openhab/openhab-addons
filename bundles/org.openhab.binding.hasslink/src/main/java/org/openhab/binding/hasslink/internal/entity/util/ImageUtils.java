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
package org.openhab.binding.hasslink.internal.entity.util;

import java.util.Map;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.ParsedData;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.RawType;

/**
 * Utility helper for parsing inline image data URIs or fetching remote images asynchronously.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public final class ImageUtils {

    private ImageUtils() {
        // Utility class
    }

    /**
     * Processes an image URL or data URI and places the parsed state into {@code targetStates}
     * or triggers an asynchronous image fetch via {@link HassLinkBridgeHandler}.
     * <p>
     * Guarded by {@link EntityContext#isLinked(String)} to avoid triggering unneeded background HTTP fetches
     * or allocations for unlinked image channels.
     *
     * @param entityState the Home Assistant entity state
     * @param attribute the image target attribute or channel name
     * @param imageSource raw image path, URL, or data URI
     * @param targetStates destination map for synchronously parsed states
     * @param context execution context supplying bridge handler reference, link predicate, and async consumer
     */
    public static void processImageChannel(EntityState entityState, String attribute, @Nullable String imageSource,
            Map<String, ParsedData> targetStates, EntityContext context) {

        if (imageSource == null || imageSource.isBlank() || !context.isLinked(attribute)) {
            return;
        }

        if (imageSource.startsWith("data:image/")) {
            try {
                targetStates.put(attribute, ParsedData.of(RawType.valueOf(imageSource)));
            } catch (IllegalArgumentException e) {
                // Invalid data URI structure
            }
        } else {
            HassLinkBridgeHandler bridgeHandler = context.bridgeHandler();
            BiConsumer<String, ParsedData> asyncStateConsumer = context.asyncStateConsumer();

            if (bridgeHandler != null && asyncStateConsumer != null) {
                String lastUpdated = entityState.getAttributeAsString("image_last_updated");

                bridgeHandler.fetchImage(imageSource, lastUpdated,
                        rawType -> asyncStateConsumer.accept(attribute, ParsedData.of(rawType)));
            }
        }
    }

    /**
     * Extracts an image URL or inline data URI from an entity state or its common attributes.
     *
     * @param entityState the Home Assistant entity state
     * @return the image URL, path, or data URI; or null if none is present
     */
    public static @Nullable String extractImageSource(EntityState entityState) {
        String state = entityState.state();
        if (isImageSource(state)) {
            return state;
        }

        String urlAttr = entityState.getAttributeAsString("url");
        if (isImageSource(urlAttr)) {
            return urlAttr;
        }

        String entityPicture = entityState.getAttributeAsString("entity_picture");
        if (isImageSource(entityPicture)) {
            return entityPicture;
        }

        return null;
    }

    /**
     * Checks whether a string represents a valid image source (URL, relative path, or Data URI).
     *
     * @param value the string to check
     * @return true if the string is an image source
     */
    public static boolean isImageSource(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.startsWith("/") //
                || value.startsWith("http://") //
                || value.startsWith("https://") //
                || value.startsWith("data:image/");
    }
}
