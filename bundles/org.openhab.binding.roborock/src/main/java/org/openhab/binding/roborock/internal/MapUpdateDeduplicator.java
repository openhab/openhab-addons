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
package org.openhab.binding.roborock.internal;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Tracks the last published rendered map image and suppresses duplicate updates.
 *
 * @author reyhard - Initial contribution
 */
@NonNullByDefault
final class MapUpdateDeduplicator {

    private byte[] lastPublishedMapPng = new byte[0];

    synchronized boolean shouldPublish(byte[] renderedMapPng) {
        if (Arrays.equals(lastPublishedMapPng, renderedMapPng)) {
            return false;
        }
        lastPublishedMapPng = Arrays.copyOf(renderedMapPng, renderedMapPng.length);
        return true;
    }

    synchronized void reset() {
        lastPublishedMapPng = new byte[0];
    }
}
