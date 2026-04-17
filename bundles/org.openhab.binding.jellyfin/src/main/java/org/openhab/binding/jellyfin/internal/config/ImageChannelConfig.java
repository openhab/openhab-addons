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
package org.openhab.binding.jellyfin.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Holds configuration for a single dynamic image download channel.
 *
 * @param imageType The Jellyfin image type name (e.g. {@code "Primary"}, {@code "Backdrop"}).
 * @param channelId The openHAB channel ID (e.g. {@code "playing-item-image-primary"}).
 * @param width The maximum download width in pixels.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public record ImageChannelConfig(String imageType, String channelId, int width) {
}
