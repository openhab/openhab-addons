/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    public @Nullable List<@Nullable Object> @Nullable [] supportedTriggers;
    public @Nullable String icon;
    public @Nullable List<@Nullable String> supportedProperties;
    public @Nullable String id;
    public @Nullable String availability;
    public @Nullable String description;
}
