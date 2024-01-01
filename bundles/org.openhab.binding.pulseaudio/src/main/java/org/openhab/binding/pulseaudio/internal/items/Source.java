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
package org.openhab.binding.pulseaudio.internal.items;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A Source is a device which is the source of an audio stream (recording
 * device) For example microphones or line-in jacks.
 *
 * @author Tobias Bräutigam - Initial contribution
 */
@NonNullByDefault
public class Source extends AbstractAudioDeviceConfig {

    @Nullable
    protected Sink monitorOf;

    public Source(int id, String name, String description, Map<String, String> properties, @Nullable Module module) {
        super(id, name, description, properties, module);
    }

    public @Nullable Sink getMonitorOf() {
        return monitorOf;
    }

    public void setMonitorOf(@Nullable Sink sink) {
        this.monitorOf = sink;
    }
}
