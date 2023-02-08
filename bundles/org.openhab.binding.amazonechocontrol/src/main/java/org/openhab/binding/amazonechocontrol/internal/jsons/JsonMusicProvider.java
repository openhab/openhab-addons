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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonMusicProvider} encapsulate the GSON returned for a music provider
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonMusicProvider {
    public @Nullable String displayName;
    public List<Object> @Nullable [] supportedTriggers;
    public @Nullable String icon;
    public @Nullable List<String> supportedProperties;
    public @Nullable String id;
    public @Nullable String availability;
    public @Nullable String description;
}
