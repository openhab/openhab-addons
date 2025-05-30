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
package org.openhab.binding.tacmi.internal.json.obj;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;

/**
 * Class holding the IO JSON element
 *
 * @author Moritz 'Morty' Strübe - Initial contribution
 */
@NonNullByDefault
public class IO {
    public Integer number = 0;
    public String ad = "";
    public Value value = new Value();

    public @Nullable String getType() {
        return value.getType();
    }

    public State getState() {
        return value.getState();
    }

    public String getDesc() {
        return value.getDesc();
    }

    public @Nullable ChannelTypeUID getChannelType() {
        return value.getChannelType();
    }
}
