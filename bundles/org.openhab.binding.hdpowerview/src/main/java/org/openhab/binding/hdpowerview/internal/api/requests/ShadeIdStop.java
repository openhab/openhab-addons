/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.api.requests;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The motion "stop" directive for a shade
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class ShadeIdStop {

    int id;
    public @Nullable String motion;

    public ShadeIdStop(int id) {
        this.id = id;
        this.motion = "stop";
    }
}
