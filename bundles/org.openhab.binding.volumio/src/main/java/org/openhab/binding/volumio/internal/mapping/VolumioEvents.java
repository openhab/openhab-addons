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
package org.openhab.binding.volumio.internal.mapping;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Patrick Sernetz - Initial Contribution
 * @author Michael Loercher - Adaption for openHAB 3
 */
@NonNullByDefault
public class VolumioEvents {

    /**
     * Pushes the current state of Volumio2. For example
     * track, artist, title, volume, ...
     *
     */
    public static final String PUSH_STATE = "pushState";
}
