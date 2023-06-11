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
package org.openhab.binding.tradfri.internal.model;

import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.PercentType;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * The {@link TradfriBlindData} class is a Java wrapper for the raw JSON data about the blinds state.
 *
 * @author Manuel Raffel - Initial contribution
 */
@NonNullByDefault
public class TradfriBlindData extends TradfriWirelessDeviceData {
    public TradfriBlindData() {
        super(BLINDS);
    }

    public TradfriBlindData(JsonElement json) {
        super(BLINDS, json);
    }

    public TradfriBlindData setPosition(PercentType position) {
        attributes.add(POSITION, new JsonPrimitive(position.intValue()));
        return this;
    }

    public TradfriBlindData stop() {
        attributes.add(STOP_TRIGGER, new JsonPrimitive(0));
        return this;
    }

    public @Nullable PercentType getPosition() {
        PercentType result = null;

        JsonElement position = attributes.get(POSITION);
        if (position != null) {
            int percent = position.getAsInt();
            percent = Math.max(percent, 0);
            percent = Math.min(100, percent);
            result = new PercentType(percent);
        }

        return result;
    }

    public String getJsonString() {
        return root.toString();
    }
}
