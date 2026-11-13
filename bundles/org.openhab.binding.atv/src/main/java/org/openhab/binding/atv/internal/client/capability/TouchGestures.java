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
package org.openhab.binding.atv.internal.client.capability;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.InputAction;
import org.openhab.binding.atv.internal.client.dto.TouchAction;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * API for touch gestures.
 *
 * <p>
 * Coordinates are in the range [0,1000].
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface TouchGestures {

    /**
     * Generates a swipe gesture from start to end coordinates in a given time.
     *
     * @param startX start x coordinate (in range [0,1000])
     * @param startY start y coordinate (in range [0,1000])
     * @param endX end x coordinate (in range [0,1000])
     * @param endY end y coordinate (in range [0,1000])
     * @param durationMs time in milliseconds to reach the end coordinates
     * @return future completing when gesture has been sent
     */
    default CompletableFuture<Void> swipe(int startX, int startY, int endX, int endY, int durationMs) {
        return CompletableFuture.failedFuture(new NotSupportedError("swipe is not supported"));
    }

    /**
     * Generates a touch event to the given coordinates.
     *
     * @param x x coordinate (in range [0,1000])
     * @param y y coordinate (in range [0,1000])
     * @param mode touch mode (press, hold or release)
     * @return future completing when event has been sent
     */
    default CompletableFuture<Void> action(int x, int y, TouchAction mode) {
        return CompletableFuture.failedFuture(new NotSupportedError("action is not supported"));
    }

    /**
     * Sends a touch click.
     *
     * @param action click mode (single tap, double tap or hold)
     * @return future completing when click has been sent
     */
    default CompletableFuture<Void> click(InputAction action) {
        return CompletableFuture.failedFuture(new NotSupportedError("click is not supported"));
    }
}
