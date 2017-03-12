/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.type;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigDescription;

public class HomematicConfigDescriptionProviderImpl implements HomematicConfigDescriptionProvider {
    private Map<URI, ConfigDescription> configDescriptionsByURI = new HashMap<URI, ConfigDescription>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        return configDescriptionsByURI.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigDescription getConfigDescription(URI uri, Locale locale) {
        return configDescriptionsByURI.get(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addConfigDescription(ConfigDescription configDescription) {
        configDescriptionsByURI.put(configDescription.getURI(), configDescription);
    }

}
