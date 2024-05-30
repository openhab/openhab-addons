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
import org.openhab.binding.huesync.internal.handler.HueSyncHandler;
import org.openhab.binding.huesync.internal.log.HueSyncLogFactory;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Root object for execution resource
 * 
 * @author Patrik Gfeller - Initial Contribution
 * 
 */
@NonNullByDefault
public class HueSyncExecutionDto {
    private static final Logger logger = HueSyncLogFactory.getLogger(HueSyncHandler.class);

    public static final List<String> KNOWN_MODES = Collections
            .unmodifiableList(Arrays.asList("powersave", "passthrough", "video", "game", "music", "ambient"));

    private @Nullable String mode;

    /**
     * 
     * @return powersave, passthrough, video, game, music, ambient
     */
    @JsonProperty("mode")
    public @Nullable String getMode() {
        return this.mode;
    }

    /**
     * 
     * @apiNote More modes can be added in the future, so clients must gracefully
     *          handle modes they don’t recognize. If an unknown mode is received, a
     *          warning will be logged and mode will fallback to "passthrough"
     * 
     * @param mode powersave, passthrough, video, game, music, ambient
     */
    public void setMode(String mode) {
        if (!HueSyncExecutionDto.KNOWN_MODES.contains(mode)) {
            logger.warn(
                    "device mode [{}] is not known by this version of the binding ➡️ please open an issue to notify the maintainer(s). Fallback will be used. ",
                    mode);
        }

        this.mode = HueSyncExecutionDto.KNOWN_MODES.contains(mode) ? mode : "unknown";
    }

    public boolean syncActive;
    public boolean hdmiActive;

    public @Nullable String hdmiSource;
    public @Nullable String hueTarget;
    public @Nullable String lastSyncMode;
    public @Nullable String preset;

    public int brightness;

    public @Nullable HueSyncExecutionDtoVideo video;
    public @Nullable HueSyncExecutionDtoGame game;
    public @Nullable HueSyncExecutionDtoMusic music;
}
