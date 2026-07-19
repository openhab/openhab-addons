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
package org.openhab.binding.atv.internal.client.protocols.companion;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.TouchGestures;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;
import org.openhab.binding.atv.internal.client.dto.InputAction;
import org.openhab.binding.atv.internal.client.dto.TouchAction;

/**
 * Implementation of the touch gesture API for Companion.
 *
 * <p>
 * Coordinates are on the virtual 1000x1000 touchpad registered with {@code _touchStart}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CompanionTouchGestures implements TouchGestures, CapabilitySource {

    private final CompanionApi api;

    /**
     * Creates a new instance.
     *
     * @param api Companion API
     */
    public CompanionTouchGestures(CompanionApi api) {
        this.api = api;
    }

    @Override
    public CompletableFuture<Void> swipe(int startX, int startY, int endX, int endY, int durationMs) {
        return api.swipe(startX, startY, endX, endY, durationMs);
    }

    @Override
    public CompletableFuture<Void> action(int x, int y, TouchAction mode) {
        return api.action(x, y, mode);
    }

    @Override
    public CompletableFuture<Void> click(InputAction action) {
        return api.click(action);
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.TOUCH_SWIPE, Capability.TOUCH_ACTION, Capability.TOUCH_CLICK);
    }
}
