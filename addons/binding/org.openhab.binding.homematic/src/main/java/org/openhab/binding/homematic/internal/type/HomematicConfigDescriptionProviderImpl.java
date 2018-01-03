/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.type;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author Gerhard Riegler - Initial contribution
 */
@Component(service = { HomematicConfigDescriptionProvider.class, ConfigDescriptionProvider.class }, immediate = true)
public class HomematicConfigDescriptionProviderImpl implements HomematicConfigDescriptionProvider {
    private Map<URI, ConfigDescription> configDescriptionsByURI = new HashMap<URI, ConfigDescription>();

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        return configDescriptionsByURI.values();
    }

    @Override
    public ConfigDescription getConfigDescription(URI uri, Locale locale) {
        return configDescriptionsByURI.get(uri);
    }

    @Override
    public void addConfigDescription(ConfigDescription configDescription) {
        configDescriptionsByURI.put(configDescription.getUID(), configDescription);
    }

}
