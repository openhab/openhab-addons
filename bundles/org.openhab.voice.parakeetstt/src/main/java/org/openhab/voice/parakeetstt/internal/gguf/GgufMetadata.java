/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.voice.parakeetstt.internal.gguf;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link GgufMetadata} record represents extracted key-value header metadata from a GGUF model file.
 *
 * @see <a href="https://github.com/mudler/parakeet.cpp/blob/master/docs/bundle.md">parakeet.cpp: Bundle Docs</a>
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public record GgufMetadata(@Nullable String name, @Nullable String architecture, List<String> languages,
        @Nullable String parakeetArch) {

    public GgufMetadata {
        languages = List.copyOf(languages);
    }

    /**
     * Determines whether this model is exclusively a Voice Activity Detection (VAD) model.
     *
     * @return true if the architecture indicates a standalone VAD model
     */
    public boolean isVadOnly() {
        return "silero_vad".equalsIgnoreCase(architecture)
                || ("parakeet".equalsIgnoreCase(architecture) && "vad".equalsIgnoreCase(parakeetArch));
    }

    /**
     * Determines whether this model is an ASR model, which might include a VAD head.
     *
     * @return true if the architecture indicates an ASR model
     */
    public boolean isAsr() {
        return ("parakeet".equalsIgnoreCase(architecture) && !"vad".equalsIgnoreCase(parakeetArch))
                || "canary".equalsIgnoreCase(architecture);
    }
}
