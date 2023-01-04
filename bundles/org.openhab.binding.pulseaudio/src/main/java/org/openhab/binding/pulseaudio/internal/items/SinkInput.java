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
package org.openhab.binding.pulseaudio.internal.items;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A SinkInput is an audio stream which can be routed to a {@link Sink}
 *
 * @author Tobias Bräutigam - Initial contribution
 */
@NonNullByDefault
public class SinkInput extends AbstractAudioDeviceConfig {

    @Nullable
    private Sink sink;

    public SinkInput(int id, String name, String description, Map<String, String> properties, @Nullable Module module) {
        super(id, name, description, properties, module);
    }

    public @Nullable Sink getSink() {
        return sink;
    }

    public void setSink(@Nullable Sink sink) {
        this.sink = sink;
    }
}
