/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwavejs.internal.type;

import java.net.URI;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionProvider;

/**
 * The {@code ZwaveJSConfigDescriptionProvider} interface extends the {@link ConfigDescriptionProvider}
 * and provides methods to add and retrieve configuration descriptions specific to Z-Wave JS.
 * 
 * @see ConfigDescriptionProvider
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public interface ZwaveJSConfigDescriptionProvider extends ConfigDescriptionProvider {

    /*
     * Adds a configuration description to the provider.
     *
     * @param configDescription the configuration description to be added
     */
    void addConfigDescription(ConfigDescription configDescription);

    /*
     * Provides a {@link ConfigDescription} for the given URI.
     *
     * @param uri uri of the config description
     * 
     * @param locale locale
     *
     * @return config description or null if no config description could be found
     */
    @Override
    @Nullable
    ConfigDescription getConfigDescription(URI uri, @Nullable Locale locale);
}
