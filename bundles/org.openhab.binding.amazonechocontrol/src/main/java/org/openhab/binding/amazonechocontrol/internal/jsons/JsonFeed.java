/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonFeed} encapsulate the GSON data of feed
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonFeed {
    public @Nullable Object feedId;
    public @Nullable String name;
    public @Nullable String skillId;
    public @Nullable String imageUrl;

    public JsonFeed(@Nullable Object feedId, @Nullable String skillId) {
        this.feedId = feedId;
        this.skillId = skillId;
    }
}
