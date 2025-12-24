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
package org.openhab.binding.unifiprotect.internal.media;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifiprotect.internal.handler.UnifiProtectCameraHandler;
import org.openhab.core.thing.ThingUID;

/**
 * Interface for the UnifiMediaService.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface UnifiMediaService {

    /**
     * Register a camera handler and its associated streams.
     * 
     * @param handler The camera handler to register.
     * @param streams The streams associated with the camera handler.
     */
    void registerHandler(UnifiProtectCameraHandler handler, Map<String, List<URI>> streams);

    /**
     * Unregister a camera handler and its associated streams.
     * 
     * @param handler The camera handler to unregister.
     */
    void unregisterHandler(UnifiProtectCameraHandler handler);

    /**
     * Get a camera handler by its thing UID.
     * 
     * @param thingUID The thing UID of the camera handler to get.
     * @return The camera handler, or null if not found.
     */
    @Nullable
    UnifiProtectCameraHandler getHandler(ThingUID thingUID);

    /**
     * Check if the media service is healthy.
     * 
     * @return true if the media service is healthy, false otherwise.
     */
    boolean isHealthy();

    /**
     * Get the base path for the media streams.
     * 
     * @return The base path for the media streams.
     */
    String getPlayBasePath();

    /**
     * Get the base path for serving images.
     * 
     * @return The base path for serving images.
     */
    String getImageBasePath();

    /**
     * Get the base URL for a go2rtc stream.
     * 
     * @param streamId The ID of the stream to get the base URL for.
     * @return The base URL for the go2rtc stream, or null if not found.
     */
    @Nullable
    String getGo2RtcBaseForStream(String streamId);
}
