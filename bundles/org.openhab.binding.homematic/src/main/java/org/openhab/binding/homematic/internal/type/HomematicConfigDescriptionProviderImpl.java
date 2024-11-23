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
package org.openhab.binding.homematic.internal.type;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homematic.type.HomematicThingTypeExcluder;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Michael Reitler - Added HomematicThingTypeExcluder
 */
@Component(service = { HomematicConfigDescriptionProvider.class, ConfigDescriptionProvider.class })
public class HomematicConfigDescriptionProviderImpl implements HomematicConfigDescriptionProvider {
    private Map<URI, ConfigDescription> configDescriptionsByURI = new HashMap<>();
    protected List<HomematicThingTypeExcluder> homematicThingTypeExcluders = new CopyOnWriteArrayList<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addHomematicThingTypeExcluder(HomematicThingTypeExcluder homematicThingTypeExcluder) {
        if (homematicThingTypeExcluders != null) {
            homematicThingTypeExcluders.add(homematicThingTypeExcluder);
        }
    }

    protected void removeHomematicThingTypeExcluder(HomematicThingTypeExcluder homematicThingTypeExcluder) {
        if (homematicThingTypeExcluders != null) {
            homematicThingTypeExcluders.remove(homematicThingTypeExcluder);
        }
    }

    private boolean isConfigDescriptionExcluded(URI configDescriptionURI) {
        // delegate to excluders
        for (HomematicThingTypeExcluder excluder : homematicThingTypeExcluders) {
            if (excluder.isConfigDescriptionExcluded(configDescriptionURI)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        Collection<ConfigDescription> result = new ArrayList<>();
        for (URI configDescriptionURI : configDescriptionsByURI.keySet()) {
            if (!isConfigDescriptionExcluded(configDescriptionURI)) {
                result.add(configDescriptionsByURI.get(configDescriptionURI));
            }
        }
        return result;
    }

    @Override
    public ConfigDescription getConfigDescription(URI uri, @Nullable Locale locale) {
        return isConfigDescriptionExcluded(uri) ? null : configDescriptionsByURI.get(uri);
    }

    @Override
    public ConfigDescription getInternalConfigDescription(URI uri) {
        return configDescriptionsByURI.get(uri);
    }

    @Override
    public void addConfigDescription(ConfigDescription configDescription) {
        configDescriptionsByURI.put(configDescription.getUID(), configDescription);
    }
}
