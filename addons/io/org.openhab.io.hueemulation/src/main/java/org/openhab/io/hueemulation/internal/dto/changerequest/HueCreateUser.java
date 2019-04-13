/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
 * Hue API create user object
 *
 * @author Dan Cunningham - Initial contribution
 * @author David Graeff - Rewritten
 */
@NonNullByDefault
public class HueCreateUser {
    /** The device label/name */
    public String devicetype = "";
    /** Caller suggested API key ("username"). Usually empty to generate one. Newer hue bridges always generate one. */
    public @Nullable String username;
}
