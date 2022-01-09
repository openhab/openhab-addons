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
package org.openhab.io.hueemulation.internal.dto.changerequest;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Hue API scan result object.
 * Enpoint: /api/{username}/scenes/ab341ef24/lights/1/state
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueChangeSceneEntry {
    public @Nullable String name;
    public @Nullable String description;

    public boolean storelightstate = false;

    public @Nullable List<String> lights;

    public @Nullable Map<String, HueStateChange> lightstates;
}
