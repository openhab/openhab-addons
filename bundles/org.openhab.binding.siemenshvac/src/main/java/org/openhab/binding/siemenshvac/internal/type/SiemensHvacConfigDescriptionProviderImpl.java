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
package org.openhab.binding.siemenshvac.internal.type;

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
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
@Component(service = { SiemensHvacConfigDescriptionProvider.class, ConfigDescriptionProvider.class })
public class SiemensHvacConfigDescriptionProviderImpl implements SiemensHvacConfigDescriptionProvider {
    private Map<URI, ConfigDescription> configDescriptionsByURI = new HashMap<>();

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(@Nullable Locale locale) {
        Collection<ConfigDescription> result = new ArrayList<>();
        for (URI configDescriptionURI : configDescriptionsByURI.keySet()) {
            ConfigDescription desc = configDescriptionsByURI.get(configDescriptionURI);
            if (desc != null) {
                result.add(desc);
            }
        }
        return result;
    }

    @Override
    public @Nullable ConfigDescription getConfigDescription(URI uri, @Nullable Locale locale) {
        return configDescriptionsByURI.get(uri);
    }

    @Nullable
    @Override
    public ConfigDescription getInternalConfigDescription(URI uri) {
        return configDescriptionsByURI.get(uri);
    }

    @Override
    public void addConfigDescription(ConfigDescription configDescription) {
        configDescriptionsByURI.put(configDescription.getUID(), configDescription);
    }

    @Override
    public void invalidate() {
        configDescriptionsByURI.clear();
    }
}
