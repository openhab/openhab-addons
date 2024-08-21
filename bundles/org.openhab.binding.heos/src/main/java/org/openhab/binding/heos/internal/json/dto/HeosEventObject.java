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
package org.openhab.binding.heos.internal.json.dto;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class for HEOS event objects
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class HeosEventObject extends HeosObject {
    public final @Nullable HeosEvent command;

    public HeosEventObject(@Nullable HeosEvent command, String rawCommand, Map<String, String> attributes) {
        super(rawCommand, attributes);
        this.command = command;
    }

    @Override
    public String toString() {
        return "HeosEventObject{" + super.toString() + ", command=" + command + '}';
    }
}
