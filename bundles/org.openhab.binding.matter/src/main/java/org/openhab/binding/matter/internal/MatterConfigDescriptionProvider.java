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
package org.openhab.binding.matter.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionProvider;
import org.osgi.service.component.annotations.Component;

/**
 * Extends the ConfigDescriptionProvider to dynamically add ConfigDescriptions.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(service = { MatterConfigDescriptionProvider.class, ConfigDescriptionProvider.class })
@NonNullByDefault
public class MatterConfigDescriptionProvider implements ConfigDescriptionProvider {

    private Map<URI, ConfigDescription> configDescriptionsByURI = new HashMap<>();

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(@Nullable Locale locale) {
        return new ArrayList<ConfigDescription>(configDescriptionsByURI.values());
    }

    @Override
    @Nullable
    public ConfigDescription getConfigDescription(URI uri, @Nullable Locale locale) {
        return configDescriptionsByURI.get(uri);
    }

    public void addConfigDescription(ConfigDescription configDescription) {
        configDescriptionsByURI.put(configDescription.getUID(), configDescription);
    }
}
