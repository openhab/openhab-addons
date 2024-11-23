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
 * A SourceOutput is the audio stream which is produced by a (@link Source}
 *
 * @author Tobias Bräutigam - Initial contribution
 */
@NonNullByDefault
public class SourceOutput extends AbstractAudioDeviceConfig {

    @Nullable
    private Source source;

    public SourceOutput(int id, String name, String description, Map<String, String> properties,
            @Nullable Module module) {
        super(id, name, description, properties, module);
    }

    public @Nullable Source getSource() {
        return source;
    }

    public void setSource(@Nullable Source source) {
        this.source = source;
    }
}
