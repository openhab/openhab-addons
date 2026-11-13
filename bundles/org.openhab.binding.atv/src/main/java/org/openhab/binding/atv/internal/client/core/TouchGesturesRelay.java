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
package org.openhab.binding.atv.internal.client.core;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.TouchGestures;
import org.openhab.binding.atv.internal.client.dto.InputAction;
import org.openhab.binding.atv.internal.client.dto.TouchAction;

/**
 * Relay implementation for touch gestures handling.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class TouchGesturesRelay extends BaseRelay<TouchGestures> implements TouchGestures {

    /**
     * Creates a new relay touch gestures instance.
     *
     * @param guard device guard blocking calls after close
     */
    public TouchGesturesRelay(Guard guard) {
        super(new Relayer<>(TouchGestures.class, AppleTVRelay.DEFAULT_PRIORITIES), guard);
    }

    @Override
    public CompletableFuture<Void> swipe(int startX, int startY, int endX, int endY, int durationMs) {
        guard.requireNotBlocked("swipe");
        return relayAsync(Capability.TOUCH_SWIPE, touch -> touch.swipe(startX, startY, endX, endY, durationMs));
    }

    @Override
    public CompletableFuture<Void> action(int x, int y, TouchAction mode) {
        guard.requireNotBlocked("action");
        return relayAsync(Capability.TOUCH_ACTION, touch -> touch.action(x, y, mode));
    }

    @Override
    public CompletableFuture<Void> click(InputAction action) {
        guard.requireNotBlocked("click");
        return relayAsync(Capability.TOUCH_CLICK, touch -> touch.click(action));
    }
}
