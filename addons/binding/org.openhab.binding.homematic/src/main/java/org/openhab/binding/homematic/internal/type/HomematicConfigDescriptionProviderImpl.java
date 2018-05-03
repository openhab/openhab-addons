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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.openhab.binding.homematic.type.HomematicThingTypeExcluder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Michael Reitler - Added HomematicThingTypeExcluder
 */
@Component(service = { HomematicConfigDescriptionProvider.class, ConfigDescriptionProvider.class }, immediate = true)
public class HomematicConfigDescriptionProviderImpl implements HomematicConfigDescriptionProvider {
    private Map<URI, ConfigDescription> configDescriptionsByURI = new HashMap<URI, ConfigDescription>();
    protected List<HomematicThingTypeExcluder> homematicThingTypeExcluders = new CopyOnWriteArrayList<>();
    
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addHomematicThingTypeExcluder(HomematicThingTypeExcluder homematicThingTypeExcluder){
        if(homematicThingTypeExcluders != null){
            homematicThingTypeExcluders.add(homematicThingTypeExcluder);
        }
    }
     
    protected void removeHomematicThingTypeExcluder(HomematicThingTypeExcluder homematicThingTypeExcluder){
        if(homematicThingTypeExcluders != null){
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
