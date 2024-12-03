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
package org.openhab.binding.huesync.internal.api.dto.execution;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Root object for execution resource
 * 
 * @author Patrik Gfeller - Initial Contribution
 * 
 */
@NonNullByDefault
public class HueSyncExecution {
    private final Logger logger = LoggerFactory.getLogger(HueSyncExecution.class);

    public static final List<String> KNOWN_MODES = Collections
            .unmodifiableList(Arrays.asList("powersave", "passthrough", "video", "game", "music"));

    private @Nullable String mode;

    /**
     * 
     * @return powersave, passthrough, video, game, music
     */
    @JsonProperty("mode")
    public @Nullable String getMode() {
        return this.mode;
    }

    /**
     * 
     * @apiNote More modes can be added in the future, so clients must gracefully
     *          handle modes they don’t recognize. If an unknown mode is received, a
     *          warning will be logged and mode will fallback to "unknown"
     * 
     * @param mode powersave, passthrough, video, game, music
     */
    public void setMode(String mode) {
        if (!HueSyncExecution.KNOWN_MODES.contains(mode)) {
            logger.warn(
                    "device mode [{}] is not known by this version of the binding. Please open an issue to notify the maintainer(s). Fallback will be used. ",
                    mode);
        }

        this.mode = HueSyncExecution.KNOWN_MODES.contains(mode) ? mode : "unknown";
    }

    /**
     * Reports `false` in case of `powersave` or `passthrough` mode, and `true` in case of `video`, `game`, or `music`
     * mode.
     * When changed from false to true, it will start syncing in last used mode for current source.
     * When changed from true to false, will set passthrough mode.
     */
    public boolean syncActive;
    /**
     * Reports `false` in case of `powersave mode`, and true in case of `passthrough`, `video`, `game`, `music` mode.
     * When changed from false to true, it will set passthrough mode. When changed from `true` to `false`, will set
     * powersave mode.
     */
    public boolean hdmiActive;

    /**
     * Currently selected hdmi input: `input1`, `input2`, `input3,` `input4`
     */
    public @Nullable String hdmiSource;

    public @Nullable String hueTarget;
    public @Nullable String lastSyncMode;
    public @Nullable String preset;

    /**
     * brightness:
     * - Get, Put
     * - number, uint
     * - 0 ... 200 (100 = no brightness reduction/boost compared to input, 0 = max reduction, 200 = max boost)
     */
    public int brightness;

    public @Nullable HueSyncExecutionVideo video;
    public @Nullable HueSyncExecutionGame game;
    public @Nullable HueSyncExecutionMusic music;
}
