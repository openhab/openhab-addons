/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Hue API scan result object.
 * Enpoint: /api/{username}/lights/new
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueChangeScheduleEntry {
    public @Nullable String name;
    public @Nullable String description;

    public @Nullable String localtime;
    // Either "enabled" or "disabled"
    public @Nullable String status;
    public @Nullable Boolean autodelete;
    public @Nullable Boolean recycle;

    public @Nullable HueCommand command;
}
