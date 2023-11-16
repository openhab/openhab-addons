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
package org.openhab.binding.spotify.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.spotify.internal.handler.SpotifyBridgeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * Spotify Rule Actions.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@ThingActionsScope(name = "spotify")
@NonNullByDefault
public class SpotifyActions implements ThingActions, ThingHandlerService {

    private @Nullable ThingHandler handler;

    /**
     * Play a context uri (track or other) on the current active device (if null is passed for deviceID) or the given
     * device at the given offset and/or position in milliseconds.
     *
     * @param contextUri context uri (track or other)
     * @param deviceId Id of the device to play on, or current device if given null
     * @param offset Offset in the list, default 0.
     * @param positionMs position in the track in milliseconds, default 0,
     */
    @RuleAction(label = "@text/actions.play.label", description = "@text/actions.play.description")
    public void play(
            @ActionInput(name = "contextUri", label = "@text/actions.play.context_uri.label", description = "@text/actions.play.context_uri.description", type = "java.lang.String", required = true) String contextUri,
            @ActionInput(name = "deviceId", label = "@text/actions.play.device_id.label", description = "@text/actions.play.device_id.description", type = "java.lang.String", defaultValue = "") @Nullable String deviceId,
            @ActionInput(name = "offset", label = "@text/actions.play.offset.label", description = "@text/actions.play.offset.description", type = "java.lang.Integer", defaultValue = "0") final int offset,
            @ActionInput(name = "positionMs", label = "@text/actions.play.positions_ms.label", description = "@text/actions.play.positions_ms.description", type = "java.lang.Integer", defaultValue = "0") final int positionMs) {
        ((SpotifyBridgeHandler) getThingHandler()).getSpotifyApi().playTrack(deviceId == null ? "" : deviceId,
                contextUri, offset, positionMs);
    }

    /**
     * Play a context uri (track or other) on the current active device.
     *
     * @param actions Spotify Actions object.
     * @param contextUri context uri (track or other)
     */
    public static void play(ThingActions actions, String contextUri) {
        ((SpotifyActions) actions).play(contextUri, null, 0, 0);
    }

    /**
     * Play a context uri (track or other) on the current active device at the given offset and/or position in
     * milliseconds.
     *
     * @param actions Spotify Actions object.
     * @param contextUri context uri (track or other)
     * @param offset Offset in the list, default 0.
     * @param positionMs position in the track in milliseconds, default 0,
     */
    public static void play(ThingActions actions, String contextUri, final int offset, final int positionMs) {
        ((SpotifyActions) actions).play(contextUri, null, positionMs, positionMs);
    }

    /**
     * Play a context uri (track or other) on the given device.
     *
     * @param actions Spotify Actions object.
     * @param contextUri context uri (track or other)
     * @param deviceId Id of the device to play on, or current device if given null
     */
    public static void play(ThingActions actions, String contextUri, @Nullable String deviceId) {
        ((SpotifyActions) actions).play(contextUri, deviceId, 0, 0);
    }

    /**
     * Play a context uri (track or other) on the current active device (if null is passed for deviceID) or the given
     * device at the given offset and/or position in milliseconds.
     *
     * @param actions Spotify Actions object.
     * @param contextUri context uri (track or other)
     * @param deviceId Id of the device to play on, or current device if given null
     * @param offset Offset in the list, default 0.
     * @param positionMs position in the track in milliseconds, default 0,
     */
    public static void play(ThingActions actions, String contextUri, @Nullable String deviceId, final int offset,
            final int positionMs) {
        ((SpotifyActions) actions).play(contextUri, deviceId, positionMs, positionMs);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
