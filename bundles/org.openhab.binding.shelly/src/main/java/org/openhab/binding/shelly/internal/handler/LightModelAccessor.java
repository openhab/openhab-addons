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
package org.openhab.binding.shelly.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link LightModelAccessor} interface provides a means of accessing a set of one
 * or more {@link ShellyLightModel}'s.
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public interface LightModelAccessor {

    /**
     * Acquire a lock on the light models for the duration of the returned
     * {@link LightModels} instance. The lock is released when the instance is closed.
     * 
     * @return a {@link LightModels} instance that provides access to the light models.
     */
    LightModels acquire();

    interface LightModels extends AutoCloseable {

        /**
         * Get the light model for the given API light index.
         * 
         * @param apiLightIndex the index of light within the device API.
         * @return the light model, or null if not found.
         */
        @Nullable
        ShellyLightModel getByApiLightIndex(int apiLightIndex);

        /**
         * Get the light model for the given channel group suffix.
         * 
         * @param channelGroupSuffix the suffix of the channel group within openHAB.
         * @return the light model, or null if not found.
         */
        @Nullable
        ShellyLightModel getByChannelGroupSuffix(int channelGroupSuffix);

        /**
         * Releases the lock and pushes dirty model state to channels.
         *
         * Equivalent to {@link #close()} but returns whether anything was updated.
         * 
         * @param forceUpdate if true, will push all model state to channels even if not dirty
         */
        boolean release(boolean forceUpdate);

        @Override
        default void close() {
            release(false);
        }
    }
}
