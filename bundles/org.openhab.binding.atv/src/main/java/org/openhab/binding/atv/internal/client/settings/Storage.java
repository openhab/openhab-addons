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
package org.openhab.binding.atv.internal.client.settings;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.conf.AtvConfig;
import org.openhab.binding.atv.internal.client.exceptions.DeviceIdMissingError;

/**
 * Base interface for storage modules persisting device settings.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface Storage {

    /**
     * Returns settings for all known devices.
     *
     * @return settings for all devices
     */
    List<Settings> settings();

    /**
     * Saves settings to the active storage.
     *
     * @return future completing when settings have been saved
     */
    CompletableFuture<Void> save();

    /**
     * Loads settings from the active storage.
     *
     * @return future completing when settings have been loaded
     */
    CompletableFuture<Void> load();

    /**
     * Returns settings for a specific configuration (device).
     *
     * <p>
     * If no settings exist for the given configuration, new settings are created automatically and returned. If
     * the configuration does not contain any valid identifiers, the future completes exceptionally with
     * {@link DeviceIdMissingError}.
     *
     * @param config configuration to get settings for
     * @return future completing with device settings
     */
    CompletableFuture<Settings> getSettings(AtvConfig config);

    /**
     * Removes settings from storage.
     *
     * @param settings settings to remove
     * @return future completing with {@code true} if settings were removed, otherwise {@code false}
     */
    CompletableFuture<Boolean> removeSettings(Settings settings);

    /**
     * Updates settings based on a configuration. This method extracts settings (identifiers, credentials and
     * passwords) from a configuration and writes them back to the storage.
     *
     * @param config configuration to extract settings from
     * @return future completing when settings have been updated
     */
    CompletableFuture<Void> updateSettings(AtvConfig config);
}
