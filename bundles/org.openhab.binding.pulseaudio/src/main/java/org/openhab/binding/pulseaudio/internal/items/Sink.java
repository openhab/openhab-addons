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
package org.openhab.binding.pulseaudio.internal.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * On a Pulseaudio server Sinks are the devices the audio streams are routed to
 * (playback devices) it can be a single item or a group of other Sinks that are
 * combined to one playback device
 *
 * @author Tobias Bräutigam - Initial contribution
 */
@NonNullByDefault
public class Sink extends AbstractAudioDeviceConfig {

    protected List<String> combinedSinkNames;
    protected List<Sink> combinedSinks;

    public Sink(int id, String name, String description, Map<String, String> properties, @Nullable Module module) {
        super(id, name, description, properties, module);
        combinedSinkNames = new ArrayList<>();
        combinedSinks = new ArrayList<>();
    }

    public void addCombinedSinkName(String name) {
        this.combinedSinkNames.add(name);
    }

    public boolean isCombinedSink() {
        return !combinedSinkNames.isEmpty();
    }

    public List<String> getCombinedSinkNames() {
        return combinedSinkNames;
    }

    public List<Sink> getCombinedSinks() {
        return combinedSinks;
    }

    public void setCombinedSinks(List<Sink> combinedSinks) {
        this.combinedSinks = combinedSinks;
    }

    public void addCombinedSink(@Nullable Sink sink) {
        if (sink != null) {
            this.combinedSinks.add(sink);
        }
    }
}
