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
package org.openhab.binding.mqtt.generic.internal.convention.homeassistant;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelStateTransformation;

/**
 * An internal Transformer to replace outgoing messages
 *
 * @author JOchen Klein - Initial contribution
 */
@NonNullByDefault
public class MapChannelStateTransformation implements ChannelStateTransformation {

    private final Map<String, String> map;

    public MapChannelStateTransformation(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public @NonNull String processValue(@NonNull String value) {
        return map.getOrDefault(value, value);
    }
}
